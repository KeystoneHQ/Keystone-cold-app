package com.keystone.coinlib.coins.polkadot.DOT;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.ChainProperty;
import com.keystone.coinlib.coins.polkadot.TransactionEncoder;
import com.keystone.coinlib.coins.polkadot.TransactionEncoderBuilder;
import com.keystone.coinlib.interfaces.SignCallback;
import com.keystone.coinlib.interfaces.Signer;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

public class DotImpl implements Coin {
    private final String coinCode;
    private final ChainProperty chainProperty;

    public DotImpl(String coinCode) {
        this.coinCode = coinCode;
        chainProperty = ChainProperty.of(coinCode);
    }

    @Override
    public String coinCode() {
        return coinCode;
    }

    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        JSONObject metadata = tx.getMetaData();
        try {
            long amount = metadata.getLong("value");
            String dest = metadata.getString("dest");
            String blockHash = metadata.getString("blockHash");
            long nonce = metadata.getLong("nonce");
            long tip = metadata.optLong("tip");
            long transactionVersion = metadata.getLong("transactionVersion");
            long specVersion = metadata.getLong("specVersion");
            long validityPeriod = metadata.optLong("validityPeriod");
            int blockNumber = metadata.getInt("blockNumber");
            TransactionEncoderBuilder builder = new TransactionEncoderBuilder();

            TransactionEncoder txEncoder = builder.setChainProperty(chainProperty)
                    .setAmount(amount)
                    .setDest(dest)
                    .setBlockHash(blockHash)
                    .setNonce(nonce)
                    .setTip(tip)
                    .setTransactionVersion(transactionVersion)
                    .setSpecVersion(specVersion)
                    .setValidityPeriod(validityPeriod > 0 ? validityPeriod : 4096)
                    .setBlockNumber(blockNumber)
                    .setFrom(AddressCodec.encodeAddress(Hex.decode(signers[0].getPublicKey()), chainProperty.addressPrefix))
                    .createSubstrateTransactionInfo();

            byte[] transaction = txEncoder.encode();
            String result = signers[0].sign(Hex.toHexString(transaction));
            if (TextUtils.isEmpty(result)) {
                callback.onFail();
            } else {
                txEncoder.addSignature(result);
                byte[] signedTx = txEncoder.encode();
                String txId = "0x" + Hex.toHexString(AddressCodec.blake2b(signedTx, 256));
                callback.onSuccess(txId, "0x" + Hex.toHexString(signedTx));
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail();
        }

    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        return signer.sign(Hex.toHexString(message.getBytes()));
    }

    @Override
    public String generateAddress(@NonNull String publicKey) {
        return AddressCodec.encodeAddress(Hex.decode(publicKey), chainProperty.addressPrefix);
    }

    @Override
    public boolean isAddressValid(@NonNull String address) {
        try {
            AddressCodec.decodeAddress(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
