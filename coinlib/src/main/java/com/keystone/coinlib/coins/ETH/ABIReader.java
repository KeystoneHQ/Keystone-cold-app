package com.keystone.coinlib.coins.ETH;

import android.text.TextUtils;

import com.esaulpaugh.headlong.abi.ABIJSON;
import com.esaulpaugh.headlong.abi.ABIType;
import com.esaulpaugh.headlong.abi.ArrayType;
import com.esaulpaugh.headlong.abi.Function;
import com.esaulpaugh.headlong.abi.Tuple;
import com.esaulpaugh.headlong.abi.TupleType;
import com.keystone.coinlib.abi.Contract;
import com.keystone.coinlib.coins.ETH.CanonicalValues.CanonicalValue;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_ARRAY;
import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_BIG_DECIMAL;
import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_BIG_INTEGER;
import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_BOOLEAN;
import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_BYTE;
import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_INT;
import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_LONG;
import static com.esaulpaugh.headlong.abi.ABIType.TYPE_CODE_TUPLE;

public class ABIReader {
    static Map<String, Function> functions = new HashMap<>();
    static List<FallbackHandler> fallbackHandlers = new ArrayList<>();

    static {
        fallbackHandlers.add(new Erc20Handler());
        fallbackHandlers.add(new GnosisHandler());
    }

    private void addABI(String json) {
        try {
            functions.clear();
            List<Function> functions = ABIJSON.parseNormalFunctions(json);
            functions.forEach(f -> {
                this.functions.put(f.selectorHex(), f);
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


    public static DecodedFunctionCall staticDecodeCall(String data) {
        try {
            String noPrefix = data;
            if (data.startsWith("0x")) {
                noPrefix = data.substring(2);
            }
            if (TextUtils.isEmpty(noPrefix)) {
                return null;
            }
            String methodId = noPrefix.substring(0, 8);
            Function entry = functions.get(methodId);
            Tuple result = entry.decodeCall(Hex.decode(noPrefix));
            return new DecodedFunctionCall(entry, result);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public DecodedFunctionCall decodeCall(String data, Contract contract, String address) {
        if (contract.isEmpty()) {
            for (FallbackHandler fallbackHandler : fallbackHandlers) {
                DecodedFunctionCall call = fallbackHandler.decodeCall(data, contract, address);
                if (call != null) {
                    return call;
                }
            }
        } else {
            addABI(contract.getAbi());
            return getDecodedFunctionCall(data);
        }
        return null;
    }

    private DecodedFunctionCall getDecodedFunctionCall(String data) {
        try {
            String noPrefix = removePrefix(data);
            if (TextUtils.isEmpty(noPrefix)) {
                return null;
            }
            String methodId = noPrefix.substring(0, 8);
            Function entry = functions.get(methodId);
            Tuple result = entry.decodeCall(Hex.decode(noPrefix));
            DecodedFunctionCall call = new DecodedFunctionCall(entry, result);
            for (FallbackHandler fallbackHandler : fallbackHandlers) {
                fallbackHandler.handleNestedContract(call);
            }
            return call;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String removePrefix(String data) {
        if (data.startsWith("0x")) {
            return data.substring(2);
        }
        return data;
    }

    public static class DecodedFunctionCall {
        public final Function function;
        public final Tuple callParameters;

        public DecodedFunctionCall(Function function, Tuple callParameters) {
            if (function.getInputs().size() == callParameters.size()) {
                this.function = function;
                this.callParameters = callParameters;
            } else {
                throw new InvalidParameterException("DecodedFunctionInputs initialize error: function inputs and decoded call result size not match");
            }
        }

        public JSONObject toJson() {
            JSONObject result = new JSONObject();
            try {
                String name = function.getName();
                result.put("method", name);
                JSONArray array = new JSONArray();
                int index = 0;
                for (ABIType<?> type : function.getInputs().elementTypes()) {
                    Object parameter = callParameters.get(index);
                    index++;
                    array.put(decodeParameter(type, parameter));
                }
                result.put("param", array);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }

        protected static JSONObject decodeParameter(ABIType<?> type, Object callParameter) {
            JSONObject parameter = new JSONObject();
            try {
                parameter.put("name", type.getName());
                parameter.put("type", type.getCanonicalType());
                CanonicalValue value = CanonicalValue.getCanonicalValue(type);
                value.resolveValueToJSONObject(callParameter, parameter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return parameter;
        }

        private JSONArray decodeArrayType(ArrayType array, Object callParameters) {
            JSONArray jsonArray = new JSONArray();
            switch (array.getElementType().typeCode()) {
                case TYPE_CODE_BOOLEAN: {
                    boolean[] params = (boolean[]) callParameters;
                    for (boolean o : params) {
                        jsonArray.put(o);
                    }
                    break;
                }
                case TYPE_CODE_BYTE: {
                    byte[] params = (byte[]) callParameters;
                    for (byte o : params) {
                        jsonArray.put(String.valueOf(o));
                    }
                    break;
                }
                case TYPE_CODE_INT: {
                    int[] params = (int[]) callParameters;
                    for (int o : params) {
                        jsonArray.put(o);
                    }
                    break;
                }
                case TYPE_CODE_LONG: {
                    long[] params = (long[]) callParameters;
                    for (long o : params) {
                        jsonArray.put(o);
                    }
                    break;
                }
                case TYPE_CODE_BIG_INTEGER:
                case TYPE_CODE_BIG_DECIMAL: {
                    Object[] params = (Object[]) callParameters;
                    for (Object o : params) {
                        jsonArray.put(o.toString());
                    }
                    break;
                }
                case TYPE_CODE_ARRAY: {
                    Object[] params = (Object[]) callParameters;
                    for (Object o : params) {
                        jsonArray.put(decodeArrayType((ArrayType) array.getElementType(), (Object[]) o));
                    }
                    break;
                }
                case TYPE_CODE_TUPLE: {
                    Object[] params = (Object[]) callParameters;
                    for (Object o : params) {
                        jsonArray.put(decodeTuple((TupleType) array.getElementType(), (Tuple) o));
                    }
                }
            }
            return jsonArray;
        }

        private JSONArray decodeTuple(TupleType type, Tuple callParameter) {
            JSONArray array = new JSONArray();
            try {
                int index = 0;
                for (ABIType abiType : type.elementTypes()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", abiType.getName());
                    jsonObject.put("type", abiType.getCanonicalType());
                    Object param = callParameter.get(index);
                    index++;
                    switch (abiType.typeCode()) {
                        case TYPE_CODE_BOOLEAN:
                        case TYPE_CODE_BYTE:
                        case TYPE_CODE_INT:
                        case TYPE_CODE_LONG:
                        case TYPE_CODE_BIG_INTEGER:
                        case TYPE_CODE_BIG_DECIMAL:
                            jsonObject.put("value", param);
                            break;
                        case TYPE_CODE_ARRAY:
                            jsonObject.put("value", decodeArrayType((ArrayType) abiType, param));
                            break;
                        case TYPE_CODE_TUPLE:
                            jsonObject.put("value", decodeTuple((TupleType) abiType, (Tuple) param));
                    }
                    array.put(jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return array;
        }
    }
}
