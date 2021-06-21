package com.keystone.coinlib.coin;


import com.keystone.coinlib.coins.XTN.Xtn;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XtnTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "2N15bQ1KCcH4NhcTxytfYDREiKmCiigaA5f",
                "2MxWT6eEmjnhyzGjo5q9qppCF4e6ng9eNqc",
                "2MyR2Fmpdw4orTpSfPDPQSFhvJMcrATBnrW",
                "2MzqZNaSsNbgCmGFLfyHCqVvHXXBNvC9rTJ",
                "2Mx6byBJtxiungkVvX4uyGy6TQ4uBmA73V7",
                "2N14sRRuAA45s3HnD9UjGHMt4CsFvrSXpLW",
                "2N1cqmw8FRvJS72YWmrkqxRZHu74QJiv2nu",
                "2Mw2UemiDkkTeKCH5DsbJJgL8wqLk5JMReV",
                "2MznBcVXhpab4vQT9e7jZSBVC1W5VZeC4tJ",
                "2N4KPNKGBGxhQzYFhejLJgHsNKHBmjmucfJ",
        };
        String pubKey = "xpub6CT8ayCfq1XAk9fbrfwVTc92DLhsr1bpzxQnX97ixZgSJqhYEdaYx1As3RT6rwEiUgRxCg8PYvg58Waz3wvVDnPZ9aMf9AdCnf6nowJftd8";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Xtn.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }
}
