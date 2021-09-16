package com.keystone.coinlib.coins.ETH;

import static org.web3j.crypto.Hash.sha3;

import android.util.Log;

import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LegacyTypedDataEncoder {
    public static byte[] legacyTypedSignatureHash(List<StructuredData.LegacyTypedData> data) throws IOException {
        List<Object> _data = data.stream().map(StructuredData.LegacyTypedData::getValue).collect(Collectors.toList());
        List<String> _types = data.stream().map(StructuredData.LegacyTypedData::getType).collect(Collectors.toList());
        List<Object> _schemas = data.stream().map(d -> d.getType() + " " + d.getName()).collect(Collectors.toList());
        return soliditySha3(Arrays.asList("bytes32", "bytes32"), Arrays.asList(
                soliditySha3(Arrays.asList("string", "string"), _schemas),
                soliditySha3(_types, _data)
        ));
    }

    private static byte[] soliditySha3(List<String> types, List<Object> values) throws IOException {
        return sha3(solidityPack(types, values));
    }

    private static byte[] solidityPack(List<String> types, List<Object> values) throws IOException {
        if (types.size() != values.size()) {
            throw new IllegalArgumentException("Number of types are not matching the values");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < types.size(); i++) {
            String type = elementaryName(types.get(i));
            Object value = values.get(i);
            baos.write(solidityHexValue(type, value));
        }
        return baos.toByteArray();
    }

    private static byte[] solidityHexValue(String type, Object value) {
        return solidityHexValue(type, value, null);
    }

    private static byte[] solidityHexValue(String type, Object value, Integer bitSize) {
        int size;
        BigInteger num;
        if (isArray(type)) {
            String subType = type.replaceAll("\\[.*?\\]", "");
            if (!isArray(subType)) {
                String arraySize = parseTypeArray(type);
                assert arraySize != null;
                if (!arraySize.equals("dynamic")) {
                    int arraySizeInt = Integer.parseInt(arraySize, 10);
                    if (arraySizeInt != 0 && ((List<Object>) value).size() > arraySizeInt) {
                        throw new IllegalArgumentException("Elements exceed array size: " + arraySize);
                    }
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ((List<Object>) value).forEach(v -> {
                try {
                    baos.write(solidityHexValue(subType, v, 256));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return baos.toByteArray();
        } else if (type.equals("bytes")) {
            return (byte[]) value;
        } else if (type.equals("string")) {
            return ((String) value).getBytes(StandardCharsets.UTF_8);
        } else if (type.equals("bool")) {
            if (bitSize == null) bitSize = 8;
            int paddingLength = bitSize / 4;
            return String.format("%" + paddingLength + "s", ((boolean) value) ? 1 : 0).replace(" ", "0").getBytes();
        } else if (type.equals("address")) {
            int byteSize = 20;
            if (bitSize != null) {
                byteSize = bitSize / 8;
            }
            return Utils.setLengthLeft((byte[]) value, byteSize);
        } else if (type.startsWith("bytes")) {
            size = parseTypeN(type);
            if (size < 1 || size > 32) {
                throw new IllegalArgumentException("Invalid bytes<N> width: " + size);
            }
            return Utils.setLengthRight((byte[]) value, size);
        } else if (type.startsWith("uint")) {
            size = parseTypeN(type);
            if ((size % 8) != 0 || (size < 8) || (size > 256)) {
                throw new IllegalArgumentException("Invalid uint<N> width: " + size);
            }
            num = parseNumber(value);
            if (num.bitLength() > size) {
                throw new IllegalArgumentException("Supplied uint exceeds width: " + size + " vs " + num.bitLength());
            }

            if (bitSize == null) {
                bitSize = size;
            }
            return Utils.setLengthLeft(Utils.un2SComplement(num.toByteArray()), bitSize / 8);
        } else if (type.startsWith("int")) {
            size = parseTypeN(type);
            if ((size % 8) != 0 || (size < 8) || (size > 256)) {
                throw new IllegalArgumentException("Invalid int<N> width: " + size);
            }

            num = parseNumber(value);
            if (num.bitLength() > size) {
                throw new IllegalArgumentException("Supplied int exceeds width: " + size + " vs " + num.bitLength());
            }
            if (bitSize == null) {
                bitSize = size;
            }
            return Utils.setLengthLeft(num.toByteArray(), bitSize / 8);
        } else {
            // FIXME: support all other types
            throw new IllegalArgumentException("Unsupported or invalid type: " + type);
        }
    }

    private static String elementaryName(String name) {
        if (name.startsWith("int[")) {
            return "int256" + name.substring(3);
        } else if (name.equals("int")) {
            return "int256";
        } else if (name.startsWith("uint[")) {
            return "uint256" + name.substring(4);
        } else if (name.equals("uint")) {
            return "uint256";
        } else if (name.startsWith("fixed[")) {
            return "fixed128x128" + name.substring(5);
        } else if (name.equals("fixed")) {
            return "fixed128x128";
        } else if (name.startsWith("ufixed[")) {
            return "ufixed128x128" + name.substring(6);
        } else if (name.equals("ufixed")) {
            return "ufixed128x128";
        }
        return name;
    }

    private static boolean isArray(String type) {
        return type.endsWith("]");
    }

    private static String parseTypeArray(String type) {
        Matcher matcher = Pattern.compile("(.*)\\[(.*?)\\]").matcher(type);
        if (matcher.matches()) {
            return matcher.group(2).equals("") ? "dynamic" : matcher.group(2);
        }
        return null;
    }

    private static int parseTypeN(String type) {
        Matcher matcher = Pattern.compile("\\D+(\\d+)").matcher(type);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1), 10);
        }
        throw new IllegalArgumentException("Invalid TypeN" + type);
    }

    private static BigInteger parseNumber(Object arg) {
        if (arg instanceof String) {
            if (Utils.isHexPrefixed((String) arg)) {
                return new BigInteger(Utils.stripHexPrefix((String) arg), 16);
            } else {
                return new BigInteger((String) arg, 10);
            }
        } else if (arg instanceof Integer || arg instanceof Long) {
            return new BigInteger(String.valueOf(arg));
        } else if (arg instanceof BigInteger) {
            return (BigInteger) arg;
        } else {
            throw new IllegalArgumentException("Argument is not a number");
        }
    }


    private static class Utils {
        public static byte[] setLengthLeft(byte[] value, int byteSize) {
            return setLength(value, byteSize, false);
        }

        public static byte[] setLengthRight(byte[] value, int byteSize) {
            return setLength(value, byteSize, true);
        }

        private static byte[] zeros(int byteSize) {
            return Hex.decode(String.format("%" + byteSize * 2 + "s", 0).replace(" ", "0"));
        }

        public static byte[] setLength(byte[] value, int length, boolean right) {
            byte[] bytes = zeros(length);
            if (right) {
                if (value.length < length) {
                    System.arraycopy(value, 0, bytes, 0, value.length);
                } else {
                    System.arraycopy(value, 0, bytes, 0, bytes.length);
                }
            } else {
                if (value.length < length) {
                    System.arraycopy(value, 0, bytes, bytes.length - value.length, value.length);
                } else {
                    System.arraycopy(value, value.length - bytes.length, bytes, 0, bytes.length);
                }
            }
            return bytes;
        }

        public static boolean isHexPrefixed(String value) {
            return value.startsWith("0x");
        }

        public static String stripHexPrefix(String value) {
            return value.replace("0x", "");
        }

        public static byte[] un2SComplement(byte[] value) {
            if (value[0] != 0) {
                return value;
            } else {
                byte[] bytes = zeros(value.length - 1);
                System.arraycopy(value, 1, bytes, 0, bytes.length);
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = bytes[i] < 0 ? (byte) (bytes[i] + 0xff) : bytes[i];
                }
                return bytes;
            }
        }
    }
}
