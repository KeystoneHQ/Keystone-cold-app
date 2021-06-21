package com.keystone.coinlib.coins.polkadot.pallets.proxy;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AddProxyParameter extends Parameter {
    private byte[] delegate;
    private byte proxyType;
    private long blockNumber;

    public AddProxyParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(delegate);
        scw.writeByte(proxyType);
        scw.writeUint32(blockNumber);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        delegate =  scr.readByteArray(32);
        proxyType = scr.readByte();
        blockNumber = scr.readUint32();
    }

    private String getProxyType() {
        if (network.SS58Prefix == 0x00) {
            //DOT
            switch (proxyType) {
                case 0x00:
                    return "Any";
                case 0x01:
                    return "NonTransfer";
                case 0x02:
                    return "Governance";
                case 0x03:
                    return "Staking";
                case 0x04:
                    return "NoUsed";
                case 0x05:
                    return "IdentityJudgement";
                case 0x06:
                    return "CancelProxy";
                default:
                    return "Unknown";
            }
        } else {
            //KSM
            switch (proxyType) {
                case 0x00:
                    return "Any";
                case 0x01:
                    return "NonTransfer";
                case 0x02:
                    return "Governance";
                case 0x03:
                    return "Staking";
                case 0x04:
                    return "IdentityJudgement";
                case 0x05:
                    return "CancelProxy";
                default:
                    return "Unknown";
            }
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Delegate", AddressCodec.encodeAddress(delegate, network.SS58Prefix))
                .put("ProxyType", getProxyType())
                .put("Delay", blockNumber);
    }
}
