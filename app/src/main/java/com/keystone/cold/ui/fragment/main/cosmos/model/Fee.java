package com.keystone.cold.ui.fragment.main.cosmos.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Fee {


    public static Fee from(JSONObject jsonObject) {

        try {
            String gas = jsonObject.getString("gas");
            JSONArray amountArray = jsonObject.getJSONArray("amount");
            List<Amount> amounts = new ArrayList<>();
            for (int i = 0; i < amountArray.length(); i++) {
                JSONObject amountObject = amountArray.getJSONObject(i);
                Amount amount = Amount.from(amountObject);
                if (amount != null) {
                    amounts.add(amount);
                }
            }
            return new Fee(gas, amounts);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private String gas;
    private List<Amount> amounts;


    public Fee(String gas, List<Amount> amounts) {
        this.gas = gas;
        this.amounts = amounts;
    }

    public String getGas() {
        return gas;
    }

    public List<Amount> getAmounts() {
        return amounts;
    }

    public String getAmountDenom() {
        String denom = null;
        if (amounts.size()!=0) {
            denom =  amounts.get(0).getDenom();
            if ("uatom".equals(denom)) {
                denom = "atom";
            }
        }
        return denom;
    }

    public String getAmountValue() {
        String amount = null;
        if (amounts.size()!=0) {
            amount = amounts.get(0).getAmount();
            if ("uatom".equals(amounts.get(0).getDenom())) {
                amount = conversionUnit(amount);
            }
        }
        return amount;
    }

    @Override
    public String toString() {
        return "Fee{" +
                "gas='" + gas + '\'' +
                ", amount=" + amounts +
                '}';
    }

    private final static String ATOM_TO_UATOM_UNIT = "1000000";
    private String conversionUnit(String original) {
        try {
            BigDecimal uAtom = new BigDecimal(original);
            BigDecimal unit = new BigDecimal(ATOM_TO_UATOM_UNIT);
            BigDecimal atom = uAtom.divide(unit);
            return atom.toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return original;
    }

}


