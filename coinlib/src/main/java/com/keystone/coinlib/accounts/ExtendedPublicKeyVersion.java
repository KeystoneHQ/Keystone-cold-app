package com.keystone.coinlib.accounts;

public enum ExtendedPublicKeyVersion {

    // https://github.com/satoshilabs/slips/blob/master/slip-0132.md
    // single sig
    xpub(0x0488b21e, "P2PKH or P2WSH_P2SH", "xpub"),
    ypub(0x049d7cb2, "P2WPKH in P2WSH_P2SH", "ypub"),
    zpub(0x04b24746, "P2WPKH", "zpub"),
    // testnet
    tpub(0x043587cf, "P2PKH or P2WSH_P2SH", "tpub"),
    upub(0x044a5262, "P2WPKH in P2WSH_P2SH", "upub"),
    vpub(0x045f1cf6, "P2WPKH", "vpub"),

    // multi sig
    Ypub(0x0295b43f, "Multi-signature P2WSH in P2WSH_P2SH", "Ypub"),
    Zpub(0x02aa7ed3, "Multi-signature P2WSH", "Zpub"),
    // testnet
    Upub(0x024289ef, "Multi-signature P2WSH in P2WSH_P2SH", "Upub"),
    Vpub(0x02575483, "Multi-signature P2WSH", "Vpub");

    private final int version;
    private final String description;
    private final String name;

    ExtendedPublicKeyVersion(int version, String description, String name) {
        this.version = version;
        this.description = description;
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public byte[] getVersionBytes() {
        byte b4 = (byte) (this.version & 0xff);
        byte b3 = (byte) ((this.version >> 8) & 0xff);
        byte b2 = (byte) ((this.version >> 16) & 0xff);
        byte b1 = (byte) ((this.version >> 24) & 0xff);

        return new byte[]{b1, b2, b3, b4};
    }

    public String getDescription() {
        return description;
    }

    public static String convertXPubVersion(String xPub, ExtendedPublicKeyVersion targetVersion) {
        ExtendedPublicKey extendedPublicKey = new ExtendedPublicKey(xPub);
        return extendedPublicKey.toVersion(targetVersion);
    }
}
