package com.keystone.coinlib.coins.polkadot.pallets.staking;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.keystone.coinlib.coins.polkadot.pallets.Parameter.readAccount;
import static com.keystone.coinlib.coins.polkadot.pallets.Parameter.writeAccount;

public class Payee {
    private final byte rewardType;
    private final byte[] rewardDestination;

    public static Payee readToPayee(ScaleCodecReader scr) {
        byte[] rewardDestinationPublicKey = {};
        byte rewardType = scr.readByte();
        switch (rewardType) {
            case 0x00:
            case 0x01:
            case 0x02:
                break;
            default:
                rewardDestinationPublicKey = scr.readByteArray(32);
        }
        return new Payee(rewardType, rewardDestinationPublicKey);
    }

    public Payee(byte rewardType, byte[] publicKey) {
        this.rewardDestination = publicKey;
        this.rewardType = rewardType;
    }

    private String getRewardType() {
        switch (rewardType) {
            case 0x00:
                return "Staked";
            case 0x01:
                return "Stash";
            case 0x02:
                return "Controller";
            case 0x03:
                return "Account";
            default:
                throw new Error("invalid reward type");
        }
    }

    private String getRewardAddress(Network network) {
        return AddressCodec.encodeAddress(rewardDestination, network.SS58Prefix);
    }

    public void writeTo(ScaleCodecWriter scw) throws IOException {
        scw.writeByte(rewardType);
        if (rewardType == 0x03) {
            scw.writeByteArray(rewardDestination);
        }
    }

    public void writeToJSON(Network network, JSONObject object) throws JSONException {
        object.put("Payee", this.getRewardType());
        if (this.rewardType == 0x03) {
            object.put("Address", this.getRewardAddress(network));
        }
    }
}
