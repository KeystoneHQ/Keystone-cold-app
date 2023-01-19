package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.solana;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.keystone.cold.MainApplication;
import com.keystone.cold.R;
import com.keystone.cold.databinding.SolInstructionArgumentBinding;
import com.keystone.cold.databinding.SolInstructionBinding;
import com.keystone.cold.ui.common.BaseBindingAdapter;
import com.keystone.cold.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InstructionAdapter extends BaseBindingAdapter<JSONObject, SolInstructionBinding> {
    private static final String TAG = "InstructionAdapter";
    private final String field;
    private final Pattern patterUnknown = Pattern.compile("Unknown");
    private final static String SOL_UNIT = "1000000000";


    public InstructionAdapter(Context context, String field) {
        super(context);
        this.field = field;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.sol_instruction;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        SolInstructionBinding binding = DataBindingUtil.getBinding(holder.itemView);
        JSONObject instruction = items.get(position);
        String program = "Unknown";
        String error = "";
        List<InstructionArgument> arguments = new ArrayList<>();
        boolean parseSuccess = true;
        try {
            if (instruction.get("readable") instanceof String) {
                parseSuccess = false;
                error = instruction.getString("readable");
                Log.e(TAG, "onBindViewHolder: ", new Exception(error));
            } else {
                JSONObject readable = instruction.getJSONObject("readable");
                program = readable.getString("program_name");
                String method = readable.getString("method_name");
                JSONObject object = readable.getJSONObject(field);
                Iterator<String> keys = object.keys();
                arguments.add(new InstructionArgument("Method", method));
                while (keys.hasNext()) {
                    String title = keys.next();
                    String content = object.get(title).toString();
                    if (title.equalsIgnoreCase("amount")) {
                        content = conversionUnit(content) + " SOL";
                    }
                    arguments.add(new InstructionArgument(title, content));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "onBindItem: ", e);
        }
        String instructionOrder = String.format("#%d ", position + 1);
        String instructionTitle = String.format("Program: %s", program);
        binding.tvInstructionOrder.setText(instructionOrder);
        binding.instructionTitle.setText(highLight(instructionTitle));
        InstructionArgumentAdapter adapter = new InstructionArgumentAdapter(context);
        adapter.setItems(arguments);
        if (parseSuccess) {
            binding.arguments.setAdapter(adapter);
        } else {
            binding.error.setText(error);
            binding.error.setVisibility(View.VISIBLE);
            binding.arguments.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onBindItem(SolInstructionBinding binding, JSONObject instruction) {

    }


    private SpannableStringBuilder highLight(String content) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(content);
        highLight(spannable, patterUnknown, R.color.check_info_color);
        return spannable;
    }

    private void highLight(SpannableStringBuilder spannable, Pattern pattern, int colorId) {
        Matcher matcher = pattern.matcher(spannable);
        while (matcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(MainApplication.getApplication().getColor(colorId)), matcher.start(),
                    matcher.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    private String conversionUnit(String original) {
        try {
            BigDecimal lamport = new BigDecimal(original);
            BigDecimal unit = new BigDecimal(SOL_UNIT);
            BigDecimal sol = lamport.divide(unit);
            return sol.toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return original;
    }

    static class InstructionArgument {
        String title;
        String content;

        public InstructionArgument(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    static class InstructionArgumentAdapter extends BaseBindingAdapter<InstructionArgument, SolInstructionArgumentBinding> {

        public InstructionArgumentAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.sol_instruction_argument;
        }

        @Override
        protected void onBindItem(SolInstructionArgumentBinding binding, InstructionArgument item) {
            binding.title.setText(StringUtils.capitalizes(item.title));
            binding.content.setText(item.content);
        }
    }
}
