package com.keystone.cold.viewmodel.tx;

import static com.keystone.coinlib.coins.BTC.Electrum.TxUtils.isMasterPublicKeyMatch;
import static com.keystone.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;
import static com.keystone.cold.viewmodel.ElectrumViewModel.adapt;

import android.app.Application;

import androidx.annotation.NonNull;

import com.googlecode.protobuf.format.JsonFormat;
import com.keystone.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.protobuf.TransactionProtoc;
import com.keystone.cold.viewmodel.exceptions.XpubNotMatchException;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

public class ElectrumTxViewModel extends KeystoneTxViewModel {
    public ElectrumTxViewModel(@NonNull Application application) {
        super(application);
    }

    public void parseTxnData(String txnData) {
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                String xpub = mRepository.loadCoinEntityByCoinCode(Coins.BTC.coinCode()).getExPub();
                ElectrumTx tx = ElectrumTx.parse(Hex.decode(txnData));
                if (!isMasterPublicKeyMatch(xpub, tx)) {
                    throw new XpubNotMatchException("xpub not match");
                }

                JSONObject signTx = parseElectrumTxHex(tx);
                parseTxData(signTx.toString());
            } catch (ElectrumTx.SerializationException | JSONException | DecoderException e) {
                e.printStackTrace();
                parseTxException.postValue(new InvalidTransactionException("invalid transaction"));
            } catch (XpubNotMatchException e) {
                e.printStackTrace();
                parseTxException.postValue(new XpubNotMatchException("invalid transaction"));
            }
        });
    }

    private JSONObject parseElectrumTxHex(ElectrumTx tx) throws JSONException {
        JSONObject btcTx = adapt(tx);
        TransactionProtoc.SignTransaction.Builder builder = TransactionProtoc.SignTransaction.newBuilder();
        builder.setCoinCode(Coins.BTC.coinCode())
                .setSignId(ELECTRUM_SIGN_ID)
                .setTimestamp(getUniversalSignIndex(getApplication()))
                .setDecimal(8);
        String signTransaction = new JsonFormat().printToString(builder.build());
        JSONObject signTx = new JSONObject(signTransaction);
        signTx.put("btcTx", btcTx);
        return signTx;
    }
}
