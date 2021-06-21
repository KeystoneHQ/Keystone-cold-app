package com.keystone.coinlib.coins.polkadot.UOS;

import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.pallets.PalletFactory;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.keystone.coinlib.coins.polkadot.pallets.Parameter.writeAccount;

public class Extrinsic {
    private final byte[] rawSigningPayload;
    private final Network network;

    public Parameter palletParameter;
    private String era;
    private BigInteger nonce;
    private BigInteger tip;
    private long specVersion;
    private long transactionVersion;
    private String genesisHash;
    private String blockHash;

    public Extrinsic(byte[] rawSigningPayload, Network network) {
        this.rawSigningPayload = rawSigningPayload;
        this.network = network;
        read();
    }

    private void read() {
        ScaleCodecReader scr = new ScaleCodecReader(rawSigningPayload);
        int code = scr.readUint16BE();
        Pallet<? extends Parameter> pallet = PalletFactory.getPallet(code, network);
        if (pallet != null) {
            palletParameter = pallet.read(scr);
            era = scr.readString(2);
            nonce = scr.readCompact();
            tip = scr.readCompact();
            specVersion = scr.readUint32();
            transactionVersion = scr.readUint32();
            genesisHash = scr.readString(32);
            blockHash = scr.readString(32);
        }
    }

    public String getEra() {
        return era;
    }

    public String getNonce() {
        return nonce.toString();
    }

    public String getTip() {
        if (tip.equals(BigInteger.ZERO)) return "0";
        return new BigDecimal(tip)
                .divide(BigDecimal.TEN.pow(network.decimals), Math.min(network.decimals, 8), BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    public long getSpecVersion() {
        return specVersion;
    }

    public long getTransactionVersion() {
        return transactionVersion;
    }

    public String getGenesisHash() {
        return genesisHash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public byte[] getSignedTransaction(byte[] accountPublicKey, byte[] signature) throws IOException {
        ScaleCodecWriter scaleCodecWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        scaleCodecWriter.writeByte(network.payloadVersion);
        writeAccount(scaleCodecWriter, accountPublicKey);
        scaleCodecWriter.writeByte(0x01);
        scaleCodecWriter.writeByteArray(signature);
        scaleCodecWriter.writeByteArray(Hex.decode(era));
        scaleCodecWriter.writeBIntCompact(nonce);
        scaleCodecWriter.writeBIntCompact(tip);
        palletParameter.writeTo(scaleCodecWriter);
        byte[] txContent = scaleCodecWriter.toByteArray();
        ScaleCodecWriter finalWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        finalWriter.writeCompact(txContent.length);
        finalWriter.writeByteArray(txContent);
        return finalWriter.toByteArray();
    }

    public String getTxId(byte[] accountPublicKey, byte[] signature) throws IOException {
        byte[] signedTx = getSignedTransaction(accountPublicKey, signature);
        return "0x" + Hex.toHexString(AddressCodec.blake2b(signedTx, 256));
    }
}
