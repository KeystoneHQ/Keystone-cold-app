package com.keystone.coinlib.coins.polkadot.pallets.democracy;

import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.pallets.Utils;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class VoteParameter extends Parameter {
    private abstract static class Vote {
        protected abstract String getTypeName();

        protected abstract void write(ScaleCodecWriter scw) throws IOException;

        protected abstract void read(ScaleCodecReader scr);
    }

    private class StandardVote extends Vote {
        private boolean aye;
        private byte conviction;
        private BigInteger balance;

        public String getTypeName() {
            return "Standard";
        }

        @Override
        protected void write(ScaleCodecWriter scw) throws IOException {
            scw.writeByte(aye ? 0x80 | conviction : conviction);
            scw.writeUint128(balance);
        }

        @Override
        protected void read(ScaleCodecReader scr) {
            byte b = scr.readByte();
            aye = (b & 0x80) > 0;
            conviction = (byte) (b & 0x7f);
            balance = scr.readUint128();
        }

        @Override
        public String toString() {
            return "Aye: " +aye + "\n"
                    + "Conviction: " + Utils.transformConviction(conviction) + "\n"
                    + "Balance: " + Utils.getReadableBalanceString(network, balance);
         }
    }

    private class SplitVote extends Vote {
        private BigInteger aye;
        private BigInteger nay;

        public String getTypeName() {
            return "Split";
        }

        @Override
        protected void write(ScaleCodecWriter scw) throws IOException {
            scw.writeUint128(aye);
            scw.writeUint128(nay);
        }

        @Override
        protected void read(ScaleCodecReader scr) {
            aye = scr.readUint128();
            nay = scr.readUint128();
        }

        @Override
        public String toString() {
            return "Aye: " +Utils.getReadableBalanceString(network, aye) + "\n"
                    + "Nay: " + Utils.getReadableBalanceString(network, nay);
        }
    }

    private long refIndex;
    private byte voteType;
    private Vote vote;

    public VoteParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        refIndex = scr.readCompact().longValue();
        voteType = scr.readByte();
        if (voteType == 0x00) {
            vote = new StandardVote();
        } else {
            vote = new SplitVote();
        }
        vote.read(scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeLIntCompact(refIndex);
        scw.writeByte(voteType);
        vote.write(scw);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("RefIndex", refIndex)
                .put("VoteType", vote.getTypeName())
                .put("Vote", vote.toString());
    }
}
