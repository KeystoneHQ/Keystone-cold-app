package com.keystone.cold.ui.fragment.main.aptos.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.NearActionAttrBinding;
import com.keystone.cold.databinding.NearActionItemBinding;
import com.keystone.cold.ui.fragment.main.aptos.model.payload.PayLoad;
import com.keystone.cold.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class AptosTransactionView extends LinearLayout {

    private LayoutInflater inflater;
    private PayLoad payLoad;

    public AptosTransactionView(Context context) {
        this(context, null);
    }

    public AptosTransactionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AptosTransactionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AptosTransactionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflater = LayoutInflater.from(getContext());
    }

    public void setData(PayLoad payLoad) {
        removeAllViews();
        this.payLoad = payLoad;

        NearActionItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.near_action_item, null, false);
        binding.actionNum.setText("Payload");

        String rawJson = payLoad.getRawJson();
        try {
            JSONObject jsonObject = new JSONObject(rawJson);
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                NearActionAttrBinding nearActionAttrBinding = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
                nearActionAttrBinding.key.setText(StringUtils.capitalizes(key));
                if (jsonObject.get(key) instanceof JSONObject) {
                    nearActionAttrBinding.value.setText(jsonObject.getJSONObject(key).toString(2));
                } else if (jsonObject.get(key) instanceof JSONArray) {
                    nearActionAttrBinding.value.setText(formatJSONArray(jsonObject.getJSONArray(key)));
                } else {
                    nearActionAttrBinding.value.setText(jsonObject.get(key).toString());
                }
                binding.llActionContainer.addView(nearActionAttrBinding.getRoot());
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        addView(binding.getRoot());
    }


    private String formatJSONArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) {
            return "[]";
        }

        StringBuilder content = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            content.append("\"").append(jsonArray.get(i).toString()).append("\"").append(",").append("\n");
        }
        int pos = content.lastIndexOf(",");
        if (pos != -1) {
            content.deleteCharAt(pos);
        }

        if (!TextUtils.isEmpty(content)) {
            return "[" + "\n" + content + "]";
        }
        return "[]";
    }
}
