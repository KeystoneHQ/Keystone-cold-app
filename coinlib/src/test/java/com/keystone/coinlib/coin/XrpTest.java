/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.coinlib.coin;

import com.keystone.coinlib.coins.XRP.TransactionFlagMap;
import com.keystone.coinlib.coins.XRP.Xrp;
import com.keystone.coinlib.coins.XRP.XrpTransaction;
import com.keystone.coinlib.coins.XRP.transcationtype.AccountDelete;
import com.keystone.coinlib.coins.XRP.transcationtype.AccountSet;
import com.keystone.coinlib.coins.XRP.transcationtype.CheckCancel;
import com.keystone.coinlib.coins.XRP.transcationtype.CheckCash;
import com.keystone.coinlib.coins.XRP.transcationtype.CheckCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.DepositPreauth;
import com.keystone.coinlib.coins.XRP.transcationtype.EscrowCancel;
import com.keystone.coinlib.coins.XRP.transcationtype.EscrowCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.EscrowFinish;
import com.keystone.coinlib.coins.XRP.transcationtype.OfferCancel;
import com.keystone.coinlib.coins.XRP.transcationtype.OfferCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.Payment;
import com.keystone.coinlib.coins.XRP.transcationtype.PaymentChannelClaim;
import com.keystone.coinlib.coins.XRP.transcationtype.PaymentChannelCreate;
import com.keystone.coinlib.coins.XRP.transcationtype.PaymentChannelFund;
import com.keystone.coinlib.coins.XRP.transcationtype.SetRegularKey;
import com.keystone.coinlib.coins.XRP.transcationtype.SignerListSet;
import com.keystone.coinlib.coins.XRP.transcationtype.TrustSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
public class XrpTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "rndm7RphBZG6CpZvKcG9AjoFbSvcKhwLCx",
                "rrBD4sBsxrpzbohAEYWH4moPSsoxupWLA",
                "rsc38kSbRZ74VjiNa8CG8xtkdqw2AWaXBb",
                "r4Mh3Hdvk1UJJSs8tjkz9qnbxNyMD5qhYz",
                "rsR6GtwgEtcJRaMrW2cNx8nwNqFovnJ32C",
                "rhzrij6yt1wCwRAFgQK5VqxyxyhbNw7QR9",
                "rNLCXobmiL4LbQkbjFJCWSW6XQm8XDLoCq",
                "rKaNnXijwXQhyWegPkmUYchJzAxgKQjry9",
                "rJ1gcRd2w38wwFNdSiqqVEuf4jYHU1fpP1",
                "rE8fnyfbtdwkbumCm3aRR5dWcTHvS6pnWt",
                "rLJYeuBpLdo6CY3xhc3SWt7hzYS6votewa",
                };
        String pubKey = "xpub6C438jHkPCDoEy5jAH4a9hBtYrcprSwGvEA8L5HNhqDyJa1WZPpZXj9DNNtsRjcHxzsuZJq18sMSkbmqYKqpDacP8aMSK63ExzX2bPoMdAo";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Xrp.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

    @Test
    public void testTransactionFlagMap(){
        System.out.println(new TransactionFlagMap().getString(2147745792L, "AccountSet"));
        System.out.println(new TransactionFlagMap().getAccountSetFlagsString(5, "AccountSetFlag"));
        System.out.println(new TrustSet().formatCurrency("USD"));
    }

    @Test
    public void testAccountSet() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"TransactionType\": \"AccountSet\",\n" +
                "    \"Account\" : \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"Fee\": \"12\",\n" +
                "    \"Sequence\": 5,\n" +
                "    \"Domain\": \"6578616D706C652E636F6D\",\n" +
                "    \"SetFlag\": 5,\n" +
                "    \"ClearFlag\": 6,\n" +
                "    \"LastLedgerSequence\": 7108629,\n" +
                "    \"Memos\": [\n" +
                "        {\n" +
                "            \"Memo\": {\n" +
                "                \"MemoType\": \"687474703a2f2f6578616d706c652e636f6d2f6d656d6f2f67656e65726963\",\n" +
                "                \"MemoData\": \"72656e74\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"Memo\": {\n" +
                "                \"MemoType\": \"687474703a2f2f6578616d706c652e636f6d2f6d656d6f2f67656e65726963\",\n" +
                "                \"MemoData\": \"72656e74\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"Flags\": 2147745793,\n" +
                "    \"MessageKey\": \"03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB\"\n" +
                "}");
        assertTrue(new AccountSet().isValid(tx));
        System.out.println(new AccountSet().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testAccountDelete() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"TransactionType\": \"AccountDelete\",\n" +
                "    \"Account\": \"rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm\",\n" +
                "    \"Destination\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
                "    \"DestinationTag\": 13,\n" +
                "    \"Fee\": \"5000000\",\n" +
                "    \"Sequence\": 2470665,\n" +
                "    \"Memos\": [\n" +
                "        {\n" +
                "            \"Memo\": {\n" +
                "                \"MemoType\": \"687474703a2f2f6578616d706c652e636f6d2f6d656d6f2f67656e65726963\",\n" +
                "                \"MemoData\": \"72656e74\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"Memo\": {\n" +
                "                \"MemoType\": \"697474703a2f2f6578616d706c652e636f6d2f6d656d6f2f67656e65726963\",\n" +
                "                \"MemoData\": \"73656e74\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"Flags\": 2147483648\n" +
                "}");
        assertTrue(new AccountDelete().isValid(tx));
        System.out.println(new AccountDelete().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testCheckCancel() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
                "    \"TransactionType\": \"CheckCancel\",\n" +
                "    \"CheckID\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\",\n" +
                "    \"Fee\": \"12\"\n" +
                "}");
        assertTrue(new CheckCancel().isValid(tx));
        System.out.println(new CheckCancel().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testCheckCash() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
                "    \"TransactionType\": \"CheckCash\",\n" +
                "    \"Amount\": \"100000000\",\n" +
                "    \"CheckID\": \"838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334\",\n" +
                "    \"Fee\": \"12\"\n" +
                "}");

        JSONObject tx2 = new JSONObject("{\n" +
                "    \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
                "    \"TransactionType\": \"CheckCash\",\n" +
                "    \"Amount\": {\n" +
                "    \t\"value\": \"13.1\",\n" +
                "    \t\"currency\": \"FOO\",\n" +
                "    \t\"issuer\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"\n" +
                "\t},\n" +
                "    \"CheckID\": \"838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334\",\n" +
                "    \"Fee\": \"12\"\n" +
                "}");

        assertTrue(new CheckCash().isValid(tx));
        System.out.println(new CheckCash().flatTransactionDetail(tx).toString(2));
        assertTrue(new CheckCash().isValid(tx2));
        System.out.println(new CheckCash().flatTransactionDetail(tx2).toString(2));
    }

    @Test
    public void testCheckCreate() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "  \"TransactionType\": \"CheckCreate\",\n" +
                "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
                "  \"Destination\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
                "  \"SendMax\": \"100000000\",\n" +
                "  \"Expiration\": 570113521,\n" +
                "  \"InvoiceID\": \"6F1DFD1D0FE8A32E40E1F2C05CF1C15545BAB56B617F9C6C2D63A6B704BEF59B\",\n" +
                "  \"DestinationTag\": 1,\n" +
                "  \"Fee\": \"12\"\n" +
                "}");

        JSONObject tx2 = new JSONObject("{\n" +
                "  \"TransactionType\": \"CheckCreate\",\n" +
                "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
                "  \"Destination\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
                "  \"SendMax\": {\n" +
                "    \t\"value\": \"500000\",\n" +
                "    \t\"currency\": \"FOO\",\n" +
                "    \t\"issuer\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"\n" +
                "  \t},\n" +
                "  \"Expiration\": 570113521,\n" +
                "  \"InvoiceID\": \"6F1DFD1D0FE8A32E40E1F2C05CF1C15545BAB56B617F9C6C2D63A6B704BEF59B\",\n" +
                "  \"DestinationTag\": 1,\n" +
                "  \"Fee\": \"12\"\n" +
                "}");

        assertTrue(new CheckCreate().isValid(tx));
        System.out.println(new CheckCreate().flatTransactionDetail(tx).toString(2));
        assertTrue(new CheckCreate().isValid(tx2));
        System.out.println(new CheckCreate().flatTransactionDetail(tx2).toString(2));
    }

    @Test
    public void testDepositPreauth() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "  \"TransactionType\" : \"DepositPreauth\",\n" +
                "  \"Account\" : \"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\",\n" +
                "  \"Authorize\" : \"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\",\n" +
                "  \"Fee\" : \"10\",\n" +
                "  \"Flags\" : 2147483648,\n" +
                "  \"Sequence\" : 2\n" +
                "}");
        assertTrue(new DepositPreauth().isValid(tx));
        System.out.println(new DepositPreauth().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testEscrowCancel() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "  \"TransactionType\": \"EscrowCancel\",\n" +
                "  \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "  \"OfferSequence\": 7\n" +
                "}");
        assertTrue(new EscrowCancel().isValid(tx));
        System.out.println(new EscrowCancel().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testEscrowCreate() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"TransactionType\": \"EscrowCreate\",\n" +
                "    \"Amount\": \"10000\",\n" +
                "    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
                "    \"CancelAfter\": 533257958,\n" +
                "    \"FinishAfter\": 533171558,\n" +
                "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
                "    \"DestinationTag\": 23480,\n" +
                "    \"SourceTag\": 11747\n" +
                "}\n");
        assertTrue(new EscrowCreate().isValid(tx));
        System.out.println(new EscrowCreate().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testEscrowFinish() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"TransactionType\": \"EscrowFinish\",\n" +
                "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"OfferSequence\": 7,\n" +
                "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
                "    \"Fulfillment\": \"A0028000\"\n" +
                "}");
        assertTrue(new EscrowFinish().isValid(tx));
        System.out.println(new EscrowFinish().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testOfferCancel() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"TransactionType\": \"OfferCancel\",\n" +
                "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
                "    \"Fee\": \"12\",\n" +
                "    \"Flags\": 0,\n" +
                "    \"LastLedgerSequence\": 7108629,\n" +
                "    \"OfferSequence\": 6,\n" +
                "    \"Sequence\": 7\n" +
                "}");
        assertTrue(new OfferCancel().isValid(tx));
        System.out.println(new OfferCancel().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testOfferCreate() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"TransactionType\": \"OfferCreate\",\n" +
                "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
                "    \"Fee\": \"12\",\n" +
                "    \"Flags\": 2147549185,\n" +
                "    \"LastLedgerSequence\": 7108682,\n" +
                "    \"Sequence\": 8,\n" +
                "    \"TakerGets\": \"6000000\",\n" +
                "    \"TakerPays\": {\n" +
                "      \"currency\": \"GKO\",\n" +
                "      \"issuer\": \"ruazs5h1qEsqpke88pcqnaseXdm6od2xc\",\n" +
                "      \"value\": \"2\"\n" +
                "    }\n" +
                "}");

        JSONObject tx2 = new JSONObject("{\n" +
                "    \"TransactionType\": \"OfferCreate\",\n" +
                "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
                "    \"Fee\": \"12\",\n" +
                "    \"Flags\": 1,\n" +
                "    \"LastLedgerSequence\": 7108682,\n" +
                "    \"Sequence\": 8,\n" +
                "    \"TakerPays\": \"6000000\",\n" +
                "    \"TakerGets\": {\n" +
                "      \"currency\": \"GKO\",\n" +
                "      \"issuer\": \"ruazs5h1qEsqpke88pcqnaseXdm6od2xc\",\n" +
                "      \"value\": \"2\"\n" +
                "    }\n" +
                "}");
        assertTrue(new OfferCreate().isValid(tx));
        System.out.println(new OfferCreate().flatTransactionDetail(tx).toString(2));
        assertTrue(new OfferCreate().isValid(tx2));
        System.out.println(new OfferCreate().flatTransactionDetail(tx2).toString(2));
    }

    @Test
    public void testPayment() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "  \"TransactionType\" : \"Payment\",\n" +
                "  \"Account\" : \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "  \"Destination\" : \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
                "  \"Amount\" : {\n" +
                "     \"currency\" : \"0000534F534F0000000000000000000000000000\",\n" +
                "     \"value\" : \"1\",\n" +
                "     \"issuer\" : \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\"\n" +
                "  },\n" +
                "  \"Fee\": \"12\",\n" +
                "  \"Flags\": 2147549185,\n" +
                "  \"Sequence\": 2,\n" +
                "}");
        assertTrue(new Payment().isValid(tx));
        System.out.println(new Payment().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testPaymentChannelClaim() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "  \"TransactionType\": \"PaymentChannelClaim\",\n" +
                "  \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\",\n" +
                "  \"Balance\": \"1000000\",\n" +
                "  \"Amount\": \"1000000\",\n" +
                "  \"Flags\": 2147549185,\n" +
                "  \"Signature\": \"30440220718D264EF05CAED7C781FF6DE298DCAC68D002562C9BF3A07C1E721B420C0DAB02203A5A4779EF4D2CCC7BC3EF886676D803A9981B928D3B8ACA483B80ECA3CD7B9B\",\n" +
                "  \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\"\n" +
                "}");
        assertTrue(new PaymentChannelClaim().isValid(tx));
        System.out.println(new PaymentChannelClaim().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testPaymentChannelCreate() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"TransactionType\": \"PaymentChannelCreate\",\n" +
                "    \"Amount\": \"10000\",\n" +
                "    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
                "    \"SettleDelay\": 86400,\n" +
                "    \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\",\n" +
                "    \"CancelAfter\": 533171558,\n" +
                "    \"DestinationTag\": 23480,\n" +
                "    \"SourceTag\": 11747\n" +
                "}");
        assertTrue(new PaymentChannelCreate().isValid(tx));
        System.out.println(new PaymentChannelCreate().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testPaymentChannelFund() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"TransactionType\": \"PaymentChannelFund\",\n" +
                "    \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\",\n" +
                "    \"Amount\": \"200000\",\n" +
                "    \"Expiration\": 543171558\n" +
                "}");
        assertTrue(new PaymentChannelFund().isValid(tx));
        System.out.println(new PaymentChannelFund().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testSetRegularKey() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Flags\": 0,\n" +
                "    \"TransactionType\": \"SetRegularKey\",\n" +
                "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"Fee\": \"12\",\n" +
                "    \"RegularKey\": \"rAR8rR8sUkBoCZFawhkWzY4Y5YoyuznwD\"\n" +
                "}");
        assertTrue(new SetRegularKey().isValid(tx));
        System.out.println(new SetRegularKey().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testSignerListSet() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"Flags\": 0,\n" +
                "    \"TransactionType\": \"SignerListSet\",\n" +
                "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
                "    \"Fee\": \"12\",\n" +
                "    \"SignerQuorum\": 3,\n" +
                "    \"SignerEntries\": [\n" +
                "        {\n" +
                "            \"SignerEntry\": {\n" +
                "                \"Account\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
                "                \"SignerWeight\": 2\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"SignerEntry\": {\n" +
                "                \"Account\": \"rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v\",\n" +
                "                \"SignerWeight\": 1\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"SignerEntry\": {\n" +
                "                \"Account\": \"raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n\",\n" +
                "                \"SignerWeight\": 1\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}");
        assertTrue(new SignerListSet().isValid(tx));
        System.out.println(new SignerListSet().flatTransactionDetail(tx).toString(2));
    }

    @Test
    public void testTrustSet() throws JSONException {
        JSONObject tx = new JSONObject("{\n" +
                "    \"TransactionType\": \"TrustSet\",\n" +
                "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
                "    \"Fee\": \"12\",\n" +
                "    \"Flags\": 262144,\n" +
                "    \"LastLedgerSequence\": 8007750,\n" +
                "    \"LimitAmount\": {\n" +
                "      \"currency\": \"USD\",\n" +
                "      \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
                "      \"value\": \"100\"\n" +
                "    },\n" +
                "    \"Sequence\": 12\n" +
                "}");

        assertTrue(new TrustSet().isValid(tx));
        System.out.println(new TrustSet().flatTransactionDetail(tx).toString(2));
    }

}










