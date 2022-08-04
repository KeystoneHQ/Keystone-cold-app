package com.keystone.cold.ui.fragment.main.near.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.NearActionAttrBinding;
import com.keystone.cold.databinding.NearActionItemBinding;
import com.keystone.cold.ui.fragment.main.near.model.NearTx;
import com.keystone.cold.ui.fragment.main.near.model.actions.Action;
import com.keystone.cold.ui.fragment.main.near.model.actions.AddKey;
import com.keystone.cold.ui.fragment.main.near.model.actions.CreateAccount;
import com.keystone.cold.ui.fragment.main.near.model.actions.DeleteAccount;
import com.keystone.cold.ui.fragment.main.near.model.actions.DeleteKey;
import com.keystone.cold.ui.fragment.main.near.model.actions.DeployContract;
import com.keystone.cold.ui.fragment.main.near.model.actions.FunctionCall;
import com.keystone.cold.ui.fragment.main.near.model.actions.Stake;
import com.keystone.cold.ui.fragment.main.near.model.actions.Transfer;
import com.keystone.cold.ui.fragment.main.near.model.actions.accesskey.FunctionCallPermission;

import java.math.BigDecimal;
import java.util.List;

public class NearActionView extends LinearLayout {
    private LayoutInflater inflater;
    private NearTx nearTx;

    //Each Ⓝ is divisible into 10^24yocto Ⓝ.
    private final static String NEAR_UNIT = "1000000000000000000000000";

    public NearActionView(Context context) {
        this(context, null);
    }

    public NearActionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NearActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NearActionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflater = LayoutInflater.from(getContext());
    }

    public void setData(NearTx nearTx) {
        removeAllViews();
        this.nearTx = nearTx;
        List<Action> actionList = nearTx.getActions();
        for (int i = 0; i < actionList.size(); i++) {
            NearActionItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.near_action_item, null, false);
            binding.actionNum.setText("Action " + (i + 1));
            Action action = actionList.get(i);
            NearActionAttrBinding nearActionAttrBinding = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
            nearActionAttrBinding.key.setText("Action Type");
            nearActionAttrBinding.value.setText(action.getActionType());
            binding.llActionContainer.addView(nearActionAttrBinding.getRoot());
            addActionAttrView(binding.llActionContainer, action);
            addView(binding.getRoot());
        }
    }

    private void addActionAttrView(LinearLayout container, Action action) {
        if (action instanceof CreateAccount) {
            addCreateAccount(container, (CreateAccount) action);
        } else if (action instanceof DeployContract) {
            addDeployContract(container, (DeployContract) action);
        } else if (action instanceof FunctionCall) {
            addFunctionCall(container, (FunctionCall) action);
        } else if (action instanceof Transfer) {
            addTransfer(container, (Transfer) action);
        } else if (action instanceof Stake) {
            addStake(container, (Stake) action);
        } else if (action instanceof AddKey) {
            addAddKey(container, (AddKey) action);
        } else if (action instanceof DeleteKey) {
            addDeleteKey(container, (DeleteKey) action);
        } else if (action instanceof DeleteAccount) {
            addDeleteAccount(container, (DeleteAccount) action);
        }
    }

    private void addDeleteAccount(LinearLayout container, DeleteAccount action) {
        NearActionAttrBinding nearActionAttrBinding = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        nearActionAttrBinding.key.setText("Beneficiary ID");
        nearActionAttrBinding.value.setText(action.getBeneficiaryId());
        container.addView(nearActionAttrBinding.getRoot(), getTopMargin());
    }

    private void addDeleteKey(LinearLayout container, DeleteKey action) {
        NearActionAttrBinding nearActionAttrBinding = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        nearActionAttrBinding.key.setText("Access Key(Public Key)");
        nearActionAttrBinding.value.setText(action.getPublicKey());
        container.addView(nearActionAttrBinding.getRoot(), getTopMargin());
    }

    private void addAddKey(LinearLayout container, AddKey action) {
        NearActionAttrBinding keyPermission = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        keyPermission.key.setText("Access Key Permission");
        keyPermission.value.setText(action.getAccessKey().getKeyPermission().getPermissionType());
        container.addView(keyPermission.getRoot(), getTopMargin());

        NearActionAttrBinding publicKey = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        publicKey.key.setText("Access Key(Public Key)");
        publicKey.value.setText(action.getPublicKey());
        container.addView(publicKey.getRoot(), getTopMargin());

        if (action.getAccessKey().getKeyPermission() instanceof FunctionCallPermission) {
            FunctionCallPermission functionCallPermission = (FunctionCallPermission) action.getAccessKey().getKeyPermission();

            NearActionAttrBinding allowance = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
            allowance.key.setText("Allowance");
            allowance.value.setText(functionCallPermission.getAllowance());
            container.addView(allowance.getRoot(), getTopMargin());

            NearActionAttrBinding contract = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
            contract.key.setText("Contract Name");
            contract.value.setText(functionCallPermission.getReceiverId());
            container.addView(contract.getRoot(), getTopMargin());

            NearActionAttrBinding methodNames = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
            methodNames.key.setText("Method Names");
            methodNames.value.setText(functionCallPermission.getMethodNames());
            container.addView(methodNames.getRoot(), getTopMargin());
        }

    }

    private void addStake(LinearLayout container, Stake action) {
        NearActionAttrBinding nearActionAttrBinding = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        nearActionAttrBinding.key.setText("value");
        nearActionAttrBinding.value.setText(action.getStake());
        container.addView(nearActionAttrBinding.getRoot(), getTopMargin());
    }

    private void addTransfer(LinearLayout container, Transfer action) {
        NearActionAttrBinding nearActionAttrBinding = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        nearActionAttrBinding.key.setText("value");
        nearActionAttrBinding.value.setText(conversionUnit(action.getDeposit()) + " NEAR");
        container.addView(nearActionAttrBinding.getRoot(), getTopMargin());
    }

    private void addFunctionCall(LinearLayout container, FunctionCall action) {
        NearActionAttrBinding deposit = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        deposit.key.setText("Deposit Value");
        deposit.value.setText(conversionUnit(action.getDeposit()) + " NEAR");
        container.addView(deposit.getRoot(), getTopMargin());

        NearActionAttrBinding gas = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        gas.key.setText("Prepaid Gas");
        gas.value.setText(conversionUnit(String.valueOf(action.getGas())) + " NEAR");
        container.addView(gas.getRoot(), getTopMargin());

        NearActionAttrBinding contract = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        contract.key.setText("Execution Contract");
        contract.value.setText(nearTx.getReceiverId());
        container.addView(contract.getRoot(), getTopMargin());

        NearActionAttrBinding method = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        method.key.setText("Method Names");
        method.value.setText(action.getMethod_name());
        container.addView(method.getRoot(), getTopMargin());
    }

    private void addDeployContract(LinearLayout container, DeployContract action) {
        NearActionAttrBinding nearActionAttrBinding = DataBindingUtil.inflate(inflater, R.layout.near_action_attr, null, false);
        nearActionAttrBinding.key.setText("Contract Name");
        nearActionAttrBinding.value.setText(nearTx.getReceiverId());
        container.addView(nearActionAttrBinding.getRoot(), getTopMargin());
    }

    private void addCreateAccount(LinearLayout container, CreateAccount action) {
    }

    private LayoutParams getTopMargin() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int) getResources().getDimension(R.dimen.tutorial_item_margin);
        return layoutParams;
    }

    private String conversionUnit(String original) {
        try {
            BigDecimal yoctoN = new BigDecimal(original);
            BigDecimal unit = new BigDecimal(NEAR_UNIT);
            BigDecimal N = yoctoN.divide(unit);
            return N.toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return original;
    }
}
