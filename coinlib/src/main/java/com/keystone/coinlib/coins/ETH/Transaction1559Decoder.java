package com.keystone.coinlib.coins.ETH;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class Transaction1559Decoder {

    public static RawTransaction decode(String hexTransaction) {
        final byte[] transaction = Hex.decode(hexTransaction);
        final RlpList rlpList = RlpDecoder.decode(transaction);
        final RlpList values = (RlpList) rlpList.getValues().get(1);
        final long chainId = ((RlpString) values.getValues().get(0)).asPositiveBigInteger().longValue();
        final BigInteger nonce = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        final BigInteger maxPriorityFeePerGas = ((RlpString) values.getValues().get(2)).asPositiveBigInteger();
        final BigInteger maxFeePerGas = ((RlpString) values.getValues().get(3)).asPositiveBigInteger();
        final BigInteger gasLimit = ((RlpString) values.getValues().get(4)).asPositiveBigInteger();
        final String to = ((RlpString) values.getValues().get(5)).asString();
        final BigInteger value = ((RlpString) values.getValues().get(6)).asPositiveBigInteger();
        final String data = ((RlpString) values.getValues().get(7)).asString();
        return RawTransaction.createTransaction(chainId, nonce, gasLimit, to, value, data, maxPriorityFeePerGas, maxFeePerGas);
    }

    public static Sign.SignatureData decodeSignature(String hexTransaction) {
        final byte[] transaction = Hex.decode(hexTransaction);
        final RlpList rlpList = RlpDecoder.decode(transaction);
        final RlpList values = (RlpList) rlpList.getValues().get(1);
        Sign.SignatureData signatureData = null;
        if (values.getValues().size() > 9) {
            final byte[] y = ((RlpString) values.getValues().get(9)).getBytes();
            final byte[] r =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(10)).getBytes()),
                            32);
            final byte[] s =
                    Numeric.toBytesPadded(
                            Numeric.toBigInt(((RlpString) values.getValues().get(11)).getBytes()),
                            32);
            signatureData =  new Sign.SignatureData(y, r, s);
        }
        return signatureData;
    }
}
