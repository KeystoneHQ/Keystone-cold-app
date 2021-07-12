/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.ETH;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.utils.Coins;
import com.keystone.coinlib.v8.ScriptLoader;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpType;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.keystone.coinlib.Util.concat;
import static com.keystone.coinlib.v8.ScriptLoader.readAsset;
import static org.web3j.crypto.TransactionEncoder.asRlpValues;

public class EthImpl implements Coin {
    public static final String ABI_JSON_SDCARD_PATH = "contracts" + File.separator + "ethereum";

    private final int chainId;

    public EthImpl(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public String coinCode() {
        return Coins.ETH.coinCode();
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        JSONObject metaData = tx.getMetaData();
        try {
            RawTransaction transaction = createRawTransaction(metaData);
            byte[] signedTransaction = signTransaction(transaction, signers[0]);
            if (signedTransaction == null) {
                callback.onFail();
            } else {
                String txId = "0x" + Hex.toHexString(Hash.sha3(signedTransaction));
                String txHex = "0x" + Hex.toHexString(signedTransaction);
                callback.onSuccess(txId, txHex);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFail();
        }
    }


    protected RawTransaction createRawTransaction(JSONObject metaData) throws JSONException {
        String to = metaData.getString("to");
        BigInteger nonce = new BigInteger(String.valueOf(metaData.getInt("nonce")));
        BigInteger gasPrice = new BigInteger(metaData.getString("gasPrice"));
        BigInteger gasLimit = new BigInteger(metaData.getString("gasLimit"));
        BigInteger value = new BigInteger(metaData.getString("value"));
        String contractAddress = metaData.optString("contractAddress");
        String data = "";
        if (!TextUtils.isEmpty(contractAddress)) {
            data = FunctionEncoder.encode(transfer(to, value));
            to = contractAddress;
            value = BigInteger.ZERO;
        }
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
    }

    public static JSONObject decodeRawTransaction(String txHex, Callback callback) {
        JSONObject metaData = new JSONObject();
        try {
            RawTransaction rawTx = TransactionDecoder.decode(txHex);
            metaData.put("to", rawTx.getTo());
            metaData.put("nonce", rawTx.getNonce().toString());
            metaData.put("gasPrice", rawTx.getGasPrice().toString());
            metaData.put("gasLimit", rawTx.getGasLimit().toString());
            metaData.put("value", rawTx.getValue().toString());
            //decode data
            String contractName = null;
            String abi = null;
            AbiDecoder decoder = new AbiDecoder();

            JSONObject bundleMap = new JSONObject(readAsset("abi/abiMap.json"));
            String abiFile = bundleMap.optString(rawTx.getTo());

            if (!TextUtils.isEmpty(abiFile)) {
                abi = readAsset("abi/" + abiFile);
                contractName = abiFile.replace(".json", "");
            } else {
                abi = readAbiFromTFCard(rawTx.getTo(), callback);
                contractName = contractNameFromTFCard(rawTx.getTo());
                contractName = "SwTest";
            }

            if (TextUtils.isEmpty(abi)) {
                //try decode with erc20 abi
                abi = readAsset("abi/Erc20.json");
                contractName = "Erc20";
            }
            decoder.addAbi(abi);
            AbiDecoder.DecodedMethod method = decoder.decodeMethod(rawTx.getData());
            if (method != null) {
                JSONObject data = method.toJson();
                data.put("contract", contractName);
                metaData.put("data", data.toString());
            } else {
                metaData.put("data", rawTx.getData());
            }

            //decode chainId
            if (rawTx instanceof SignedRawTransaction) {
                Sign.SignatureData signatureData = ((SignedRawTransaction) rawTx).getSignatureData();
                byte[] v = signatureData.getV();
                metaData.put("chainId", new BigInteger(v).intValue());
            } else {
                metaData.put("chainId", 1);
            }
            metaData.put("signingData", txHex);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metaData;
    }

    private static String contractNameFromTFCard(String to) {
        String result = null;
        try {
            String contentFromSdCard = ScriptLoader.getContentFromSdCard(ABI_JSON_SDCARD_PATH, to);
            if (!TextUtils.isEmpty(contentFromSdCard)) {
                JSONObject sdCardJsonObject = new JSONObject(contentFromSdCard);
                result = sdCardJsonObject.optString("name");
                if (TextUtils.isEmpty(result)) {
                    result = "";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String readAbiFromTFCard(String to, Callback callback) {
        String result = null;
        try {
            String contentFromSdCard = ScriptLoader.getContentFromSdCard(ABI_JSON_SDCARD_PATH, to);
            if (!TextUtils.isEmpty(contentFromSdCard)) {
                JSONObject sdCardJsonObject = new JSONObject(contentFromSdCard);
                JSONObject output = sdCardJsonObject.getJSONObject("output");
                JSONArray abi = output.getJSONArray("abi");
                result = abi.toString();
                if (result != null && callback != null) {
                    callback.fromTFCard();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSignature(String signedHex) {
        SignedRawTransaction signedTx = (SignedRawTransaction) TransactionDecoder.decode(signedHex);
        Sign.SignatureData signatureData = signedTx.getSignatureData();
        byte[] signatureBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
        return Hex.toHexString(signatureBytes);
    }

    public SignTxResult signHex(String hex, Signer signer) {
        RawTransaction rawTx = TransactionDecoder.decode(hex);

        byte[] signed = signTransaction(rawTx, signer);
        if (signed != null) {
            String txId = "0x" + Hex.toHexString(Hash.sha3(signed));
            String txHex = "0x" + Hex.toHexString(signed);
            return new SignTxResult(txId, txHex);
        } else {
            return null;
        }
    }

    private static String getAbi(String contractAddress) {
        try {
            JSONObject bundleMap = new JSONObject(readAsset("abi/abiMap.json"));
            String abiFile = bundleMap.optString(contractAddress);
            if (!TextUtils.isEmpty(abiFile)) {
                return readAsset("abi/" + abiFile);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] signTransaction(RawTransaction transaction, Signer signer) {
        byte[] encodedTransaction = TransactionEncoder.encode(transaction, chainId);
        byte[] transactionHash = Hash.sha3(encodedTransaction);
        String signature = signer.sign(Hex.toHexString(transactionHash));
        Sign.SignatureData signatureData = getSignatureData(signature);
        return encodeSignedTransaction(transaction, signatureData);
    }

    public Sign.SignatureData getSignatureData(String signature) {
        if (TextUtils.isEmpty(signature)) return null;
        byte[] r = Hex.decode(signature.substring(0, 64));
        byte[] s = Hex.decode(signature.substring(64, 128));

        int recId = 0;
        try {
            recId = Integer.parseInt(signature.substring(128), 16);
        } catch (Exception ignore) {
        }
        int v = 27 + recId;
        if (chainId > 0) {
            v += chainId * 2 + 8;
        }
        return new Sign.SignatureData((byte) v, r, s);
    }

    private byte[] encodeSignedTransaction(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    public Function transfer(String to, BigInteger value) {
        return new Function(
                "transfer",
                Arrays.asList(new Address(to), new Uint256(value)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        try {
            byte[] messageHash = new StructuredDataEncoder(message).hashStructuredData();
            String signature = signer.sign(Hex.toHexString(messageHash));
            Sign.SignatureData signatureData = getSignatureData(signature);
            byte[] sigBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
            return Hex.toHexString(sigBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String signPersonalMessage(@NonNull String message, Signer signer) {
        String signature = signer.sign(Hex.toHexString(hashPersonalMessage(message)));
        Sign.SignatureData signatureData = getSignatureData(signature);
        byte[] sigBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
        return Hex.toHexString(sigBytes);
    }

    private byte[] hashPersonalMessage(String message) {
        String prefix = "\u0019Ethereum Signed Message:\n" + message.length();
        return Hash.sha3((prefix + message).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAddress(@NonNull String publicKey) {
        return null;
    }

    @Override
    public boolean isAddressValid(@NonNull String address) {
        return false;
    }

    public interface Callback {
        void fromTFCard();
    }
}
