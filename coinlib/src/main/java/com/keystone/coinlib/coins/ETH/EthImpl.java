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

import static com.keystone.coinlib.Util.concat;
import static org.web3j.crypto.TransactionEncoder.asRlpValues;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.keystone.coinlib.abi.AbiLoadManager;
import com.keystone.coinlib.abi.Contract;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.SignTxResult;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;
import com.keystone.coinlib.selector.MethodSignature;
import com.keystone.coinlib.selector.SelectorLoadManager;
import com.keystone.coinlib.utils.Coins;

import org.bouncycastle.util.encoders.Hex;
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
import org.web3j.crypto.transaction.type.Transaction1559;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpType;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EthImpl implements Coin {
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

    public static JSONObject decodeTransaction(String txHex, Callback callback) {
        JSONObject metaData = new JSONObject();
        try {
            RawTransaction rawTx = TransactionDecoder.decode(txHex);
            metaData.put("gasPrice", rawTx.getGasPrice().toString());

            //decode chainId
            if (rawTx instanceof SignedRawTransaction) {
                Sign.SignatureData signatureData = ((SignedRawTransaction) rawTx).getSignatureData();
                byte[] v = signatureData.getV();
                metaData.put("chainId", new BigInteger(Hex.toHexString(v), 16).intValue());
            } else {
                metaData.put("chainId", 1);
            }
            setMetaDataFromRawTx(txHex, metaData, rawTx, callback);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metaData;
    }

    public static JSONObject decodeEIP1559Transaction(String txHex, Callback callback) {
        JSONObject metaData = new JSONObject();
        try {
            RawTransaction rawTx = Transaction1559Decoder.decode(txHex);
            Transaction1559 transaction1559 = (Transaction1559) rawTx.getTransaction();
            metaData.put("maxPriorityFeePerGas", transaction1559.getMaxPriorityFeePerGas());
            metaData.put("maxFeePerGas", transaction1559.getMaxFeePerGas());
            metaData.put("chainId", transaction1559.getChainId());
            setMetaDataFromRawTx(txHex, metaData, rawTx, callback);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return metaData;
    }

    private static void setMetaDataFromRawTx(String txHex, JSONObject metaData,
                                             RawTransaction rawTx, Callback callback) throws Exception {
        metaData.put("nonce", rawTx.getNonce().toString());
        metaData.put("gasLimit", rawTx.getGasLimit().toString());
        metaData.put("value", rawTx.getValue().toString());
        metaData.put("to", Eth.Deriver.toChecksumAddress(rawTx.getTo()));

        List<Contract> contracts = getContract(rawTx.getTo());
        Contract matchedContract = null;

        ABIReader abiReader = new ABIReader();
        for (Contract contract: contracts){
            ABIReader.DecodedFunctionCall call = abiReader.decodeCall(rawTx.getData(), contract, rawTx.getTo());
            if (call != null) {
                JSONObject data = call.toJson();
                data.put("contract", contract.getName());
                metaData.put("data", data.toString());
                metaData.put("contract", contract.getName());
                matchedContract = contract;
                break;
            }
        }
        if (matchedContract == null) {
            metaData.put("data", rawTx.getData());
            List<MethodSignature> methodSignatures = getSelector(rawTx.getData());
            StringBuilder methods = new StringBuilder();
            for (MethodSignature methodSignature: methodSignatures){
                methods.append(methodSignature.getMethodName()).append(" ");
            }
            metaData.put("selectorMethodName", methods.toString());
        }
        metaData.put("signingData", txHex);

        if (matchedContract != null && matchedContract.isFromTFCard() && callback != null) {
            callback.fromTFCard();
        }
    }

    protected static List<Contract> getContract(String address) {
        AbiLoadManager abiLoadManager = new AbiLoadManager(address);
        return abiLoadManager.loadAbi();
    }

    protected static List<MethodSignature> getSelector(String signature){
        SelectorLoadManager selectorLoadManager = new SelectorLoadManager(signature);
        return selectorLoadManager.loadSelector();
    }

    public SignTxResult signHex(String hex, Signer signer) {
        byte[] encodedTransaction = Hex.decode(hex);
        byte[] transactionHash = Hash.sha3(encodedTransaction);

        String signature = signer.sign(Hex.toHexString(transactionHash));

        Sign.SignatureData signatureData = getSignatureData(signature);

        RawTransaction rawTx = TransactionDecoder.decode(hex);
        byte[] signatureBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
        byte[] signed = encodeSignedTransaction(rawTx, signatureData);
        if (signed != null) {
            String txId = "0x" + Hex.toHexString(Hash.sha3(signed));
            String signedTxHex = "0x" + Hex.toHexString(signed);
            String signatureHex = Hex.toHexString(signatureBytes);
            return new SignTxResult(txId, signedTxHex, signatureHex);
        } else {
            return null;
        }
    }

    public SignTxResult signEIP1559Hex(String hex, Signer signer) {
        byte[] encodedTransaction = Hex.decode(hex);
        byte[] transactionHash = Hash.sha3(encodedTransaction);

        String signature = signer.sign(Hex.toHexString(transactionHash));

        Sign.SignatureData signatureData = getEIP1559SignatureData(signature);
        RawTransaction rawTx = Transaction1559Decoder.decode(hex);
        byte[] signed = encodeSignedTransaction(rawTx, signatureData);
        byte[] signatureBytes = concat(concat(signatureData.getR(), signatureData.getS()), new byte[]{(byte) Sign.getRecId(signatureData, ((Transaction1559) rawTx.getTransaction()).getChainId())});
        if (signed != null) {
            byte[] eip1559signed = new byte[signed.length + 1];
            System.arraycopy(signed, 0, eip1559signed, 1, signed.length);
            eip1559signed[0] = 2;
            String txId = "0x" + Hex.toHexString(Hash.sha3(eip1559signed));
            String signedTxHex = Hex.toHexString(eip1559signed);
            String signatureHex = Hex.toHexString(signatureBytes);
            return new SignTxResult(txId, signedTxHex, signatureHex);
        } else {
            return null;
        }
    }

    public byte[] signTransaction(RawTransaction transaction, Signer signer) {
        byte[] encodedTransaction = TransactionEncoder.encode(transaction, chainId);
        byte[] transactionHash = Hash.sha3(encodedTransaction);
        String signature = signer.sign(Hex.toHexString(transactionHash));
        Sign.SignatureData signatureData = getSignatureData(signature);
        return encodeSignedTransaction(transaction, signatureData);
    }

    public static String getSignature(String signedHex) {
        SignedRawTransaction signedTx = (SignedRawTransaction) TransactionDecoder.decode(signedHex);
        Sign.SignatureData signatureData = signedTx.getSignatureData();
        byte[] signatureBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
        return Hex.toHexString(signatureBytes);
    }

    public static String getEIP1559Signature(String signedHex) {
        Sign.SignatureData signatureData = Transaction1559Decoder.decodeSignature(signedHex);
        byte[] signatureBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
        return Hex.toHexString(signatureBytes);
    }

    public Sign.SignatureData getSignatureData(String signature) {
        return getSignatureData(signature, true);
    }

    public Sign.SignatureData getEIP1559SignatureData(String signature) {
        return getSignatureData(signature, false);
    }

    public Sign.SignatureData getSignatureData(String signature, boolean isEIP155) {
        if (TextUtils.isEmpty(signature)) return null;
        byte[] r = Hex.decode(signature.substring(0, 64));
        byte[] s = Hex.decode(signature.substring(64, 128));

        int recId = 0;
        try {
            recId = Integer.parseInt(signature.substring(128), 16);
        } catch (Exception ignore) {
        }
        int v = 27 + recId;
        if (chainId > 0 && isEIP155) {
            v += chainId * 2 + 8;
        }
        return new Sign.SignatureData(BigInteger.valueOf(v).toByteArray(), r, s);
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
        return signPersonalMessage(message, signer);
    }

    public String signEIP712TypedData(@NonNull String message, Signer signer) {
        try {
            byte[] messageHash = new StructuredDataEncoder(message).hashStructuredData();
            String signature = signer.sign(Hex.toHexString(messageHash));
            Sign.SignatureData signatureData = getSignatureData(signature, false);
            byte[] sigBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
            return Hex.toHexString(sigBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String signPersonalMessage(@NonNull String message, Signer signer) {
        String signature = signer.sign(Hex.toHexString(hashPersonalMessage(message)));
        Sign.SignatureData signatureData = getSignatureData(signature, false);
        byte[] sigBytes = concat(concat(signatureData.getR(), signatureData.getS()), signatureData.getV());
        return Hex.toHexString(sigBytes);
    }

    private byte[] hashPersonalMessage(String messageHex) {
        byte[] message = Hex.decode(messageHex);
        String prefix = "\u0019Ethereum Signed Message:\n" + message.length;
        byte[] rawData = concat(prefix.getBytes(StandardCharsets.UTF_8), message);
        return Hash.sha3(rawData);
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
