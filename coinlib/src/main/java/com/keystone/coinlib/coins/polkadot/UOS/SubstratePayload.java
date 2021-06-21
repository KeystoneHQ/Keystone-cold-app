package com.keystone.coinlib.coins.polkadot.UOS;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.exception.InvalidUOSException;

import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

public class SubstratePayload {
    public final String rawData;
    public String curve;
    public byte[] accountPublicKey;
    public boolean isHash;
    public boolean isOversize;
    public String genesisHash;
    public Extrinsic extrinsic;
    private byte[] rawSigningData;

    public Network network;

    public SubstratePayload(String rawData) throws InvalidUOSException {
        this.rawData = rawData;
        read();
    }

    private void read() throws InvalidUOSException {
        ScaleCodecReader scaleCodecReader;
        try {
            scaleCodecReader = new ScaleCodecReader(Hex.decode(rawData));
        } catch (DecoderException e) {
            e.printStackTrace();
            throw new InvalidUOSException("invalid curve bytes");
        }
        byte firstByte = scaleCodecReader.readByte();
        byte secondByte = scaleCodecReader.readByte();
        switch (firstByte) {
            case 0x00:
                curve = "ed25519";
                break;
            case 0x01:
                curve = "sr25519";
                break;
            default:
                throw new InvalidUOSException("invalid curve bytes");
        }
        accountPublicKey = scaleCodecReader.readByteArray(32);
        String restString = scaleCodecReader.readRestString();
        String rawPayload = restString.substring(0, restString.length() - 64);
        genesisHash = restString.substring(restString.length() - 64);
        network = Network.of(genesisHash);

        switch (secondByte){
            case 0x00:
            case 0x02: {
                isHash = false;
                ScaleCodecReader tempReader = new ScaleCodecReader(Hex.decode(rawPayload));
                tempReader.readCompact();
                rawSigningData = tempReader.readRestBytes();
                isOversize = rawSigningData.length > 256;
                extrinsic = new Extrinsic(rawSigningData, network);
                break;
            }
            case 0x01: {
                isHash = true;
                rawSigningData = Hex.decode(rawPayload);
                break;
            }
            default: {
                throw new InvalidUOSException("invalid data type byte");
            }
        }
    }
    public byte[] getSigningPayload() {
        return isHash ? rawSigningData : isOversize ? AddressCodec.blake2b(rawSigningData, 256) : rawSigningData;
    }

    public String getAccount() {
        return AddressCodec.encodeAddress(accountPublicKey, network.SS58Prefix);
    }
}
