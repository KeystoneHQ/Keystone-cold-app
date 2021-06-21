package com.keystone.coinlib.coins.polkadot.UOS;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Network {
    public String name;
    public byte SS58Prefix;
    public String genesisHash;
    public int decimals;
    public int payloadVersion;

    public Network(String name, byte SS58Prefix, String genesisHash, int decimals, int payloadVersion) {
        this.name = name;
        this.SS58Prefix = SS58Prefix;
        this.genesisHash = genesisHash;
        this.decimals = decimals;
        this.payloadVersion = payloadVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;
        return SS58Prefix == network.SS58Prefix &&
                decimals == network.decimals &&
                Objects.equals(name, network.name) &&
                Objects.equals(genesisHash, network.genesisHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, SS58Prefix, genesisHash, decimals);
    }

    public static Network of(String genesisHash) {
        return Network.supportedNetworks.stream()
                .filter(n -> n.genesisHash.equals(genesisHash))
                .findFirst()
                .orElse(new Network("UNKNOWN", (byte) 0, genesisHash, 0, 0x00));
    }

    public String coinCode() {
        if (this == POLKADOT) {
            return "DOT";
        } else if (this == KUSAMA) {
            return "KSM";
        }
        return "";
    }

    public static final Network POLKADOT = new Network("Polkadot", (byte) 0, "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3", 10, 0x84);
    public static final Network KUSAMA = new Network("Kusama", (byte) 2, "b0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe", 12, 0x84);

    public static final List<Network> supportedNetworks = Arrays.asList(
            POLKADOT, KUSAMA
    );
}
