package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.keystone.cold.R;
import com.keystone.cold.databinding.CosmosInvolvedMessageItemBinding;
import com.keystone.cold.databinding.CosmosMsgItemBinding;
import com.keystone.cold.databinding.ItemCosmosMsgBinding;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.Amount;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.Msg;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgBeginRedelegate;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgDelegate;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgExec;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgSend;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgTransfer;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgUndelegate;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgVote;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.MsgWithdrawDelegationReward;
import com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg.UnsupportMsg;

import java.math.BigDecimal;
import java.util.List;

public class CosmosMsgView extends LinearLayout {
    private LayoutInflater inflater;

    private final static String ATOM_TO_UATOM_UNIT = "1000000";

    public CosmosMsgView(Context context) {
        this(context, null);
    }

    public CosmosMsgView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CosmosMsgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CosmosMsgView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflater = LayoutInflater.from(getContext());
    }

    public void setData(List<Msg> msgs) {
        removeAllViews();
        for (int i = 0; i < msgs.size(); i++) {
            addMsgView(msgs.get(i));
        }
    }

    private void addMsgType(Msg msg) {
        CosmosMsgItemBinding msgTypeView = generateMsgView();
        msgTypeView.tvTittle.setText(getContext().getString(R.string.transaction_type));
        msgTypeView.tvValue.setText(msg.getType());
        addView(msgTypeView.getRoot());
    }

    private void addMsgView(Msg msg) {
        if (msg == null) {
            return;
        }
        addMsgType(msg);
        if (msg instanceof MsgSend) {
            addMsgSend((MsgSend) msg);
        } else if (msg instanceof MsgVote) {
            addMsgVote((MsgVote) msg);
        } else if (msg instanceof MsgDelegate) {
            addMsgDelegate((MsgDelegate) msg);
        } else if (msg instanceof MsgUndelegate) {
            addMsgUndelegate((MsgUndelegate) msg);
        } else if (msg instanceof MsgTransfer) {
            addMsgTransfer((MsgTransfer) msg);
        } else if (msg instanceof MsgWithdrawDelegationReward) {
            addMsgWithdrawDelegationReward((MsgWithdrawDelegationReward) msg);
        } else if (msg instanceof MsgBeginRedelegate) {
            addMsgBeginRedelegate((MsgBeginRedelegate) msg);
        } else if (msg instanceof MsgExec) {
            addMsgExec((MsgExec) msg);
        } else if (msg instanceof UnsupportMsg) {
            addUnSupportMsg();
        }
    }

    private void addMsgExec(MsgExec msg) {
        ItemCosmosMsgBinding view = DataBindingUtil.inflate(inflater, R.layout.item_cosmos_msg, null, false);
        view.tvTittle.setText(getContext().getString(R.string.details));
        view.cmMessages.setData(msg.getMsgs());
        view.cmMessages.setVisibility(VISIBLE);
        addView(view.getRoot());
    }

    private void addUnSupportMsg() {
        CosmosInvolvedMessageItemBinding view = DataBindingUtil.inflate(inflater, R.layout.cosmos_involved_message_item, null, false);
        view.tvTittle.setText(getContext().getString(R.string.message_tittle));
        view.tvContent.setText(getContext().getString(R.string.unsupport_message_hint));
        view.tvContent.setVisibility(VISIBLE);
        addView(view.getRoot());
    }

    private void addMsgBeginRedelegate(MsgBeginRedelegate msg) {
        String delegateAddress = msg.getDelegatorAddress();
        CosmosMsgItemBinding delegateAddressView = generateMsgView();
        delegateAddressView.tvTittle.setText(getContext().getString(R.string.tx_to));
        delegateAddressView.tvValue.setText(delegateAddress);
        addView(delegateAddressView.getRoot());

        String validatorSrcAddress = msg.getValidatorSrcAddress();
        CosmosMsgItemBinding validatorSrcAddressView = generateMsgView();
        validatorSrcAddressView.tvTittle.setText(getContext().getString(R.string.old_validator));
        validatorSrcAddressView.tvValue.setText(validatorSrcAddress);
        addView(validatorSrcAddressView.getRoot());

        String validatorDstAddress = msg.getValidatorDstAddress();
        CosmosMsgItemBinding validatorDstAddressView = generateMsgView();
        validatorDstAddressView.tvTittle.setText(getContext().getString(R.string.new_validator));
        validatorDstAddressView.tvValue.setText(validatorDstAddress);
        addView(validatorDstAddressView.getRoot());

        addAmount(msg.getAmount());
    }


    private void addMsgWithdrawDelegationReward(MsgWithdrawDelegationReward msg) {
        String delegateAddress = msg.getDelegatorAddress();
        CosmosMsgItemBinding delegateAddressView = generateMsgView();
        delegateAddressView.tvTittle.setText(getContext().getString(R.string.delegate_address));
        delegateAddressView.tvValue.setText(delegateAddress);
        addView(delegateAddressView.getRoot());


        String validatorAddress = msg.getValidatorAddress();
        CosmosMsgItemBinding validatorAddressView = generateMsgView();
        validatorAddressView.tvTittle.setText(getContext().getString(R.string.validator_address));
        validatorAddressView.tvValue.setText(validatorAddress);
        addView(validatorAddressView.getRoot());
    }

    private void addMsgTransfer(MsgTransfer msg) {
        String sender = msg.getSender();
        CosmosMsgItemBinding senderView = generateMsgView();
        senderView.tvTittle.setText(getContext().getString(R.string.tx_from));
        senderView.tvValue.setText(sender);
        addView(senderView.getRoot());


        String receiver = msg.getReceiver();
        CosmosMsgItemBinding receiverView = generateMsgView();
        receiverView.tvTittle.setText(getContext().getString(R.string.tx_to));
        receiverView.tvValue.setText(receiver);
        addView(receiverView.getRoot());


        addAmount(msg.getToken());
    }

    private void addAmount(Amount amount) {
        if (amount == null) {
            return;
        }
        String denom = amount.getDenom();
        String amountStr = amount.getAmount();
        String display;
        if ("uatom".equals(denom)) {
            display = conversionUnit(amountStr) + " ATOM";
        } else {
            display = amountStr + " " + denom;
        }
        CosmosMsgItemBinding amountView = generateMsgView();
        amountView.tvTittle.setText(getContext().getString(R.string.amount));
        amountView.tvValue.setText(display);
        addView(amountView.getRoot());

    }

    private void addMsgUndelegate(MsgUndelegate msg) {
        String delegateAddress = msg.getDelegatorAddress();
        CosmosMsgItemBinding delegateAddressView = generateMsgView();
        delegateAddressView.tvTittle.setText(getContext().getString(R.string.delegate_address));
        delegateAddressView.tvValue.setText(delegateAddress);
        addView(delegateAddressView.getRoot());


        String validatorAddress = msg.getValidatorAddress();
        CosmosMsgItemBinding validatorAddressView = generateMsgView();
        validatorAddressView.tvTittle.setText(getContext().getString(R.string.validator_address));
        validatorAddressView.tvValue.setText(validatorAddress);
        addView(validatorAddressView.getRoot());


        addAmount(msg.getAmount());
    }

    private void addMsgDelegate(MsgDelegate msg) {
        String delegateAddress = msg.getDelegatorAddress();
        CosmosMsgItemBinding delegateAddressView = generateMsgView();
        delegateAddressView.tvTittle.setText(getContext().getString(R.string.delegate_address));
        delegateAddressView.tvValue.setText(delegateAddress);
        addView(delegateAddressView.getRoot());


        String validatorAddress = msg.getValidatorAddress();
        CosmosMsgItemBinding validatorAddressView = generateMsgView();
        validatorAddressView.tvTittle.setText(getContext().getString(R.string.validator_address));
        validatorAddressView.tvValue.setText(validatorAddress);
        addView(validatorAddressView.getRoot());


        addAmount(msg.getAmount());
    }

    private void addMsgVote(MsgVote msg) {
        String voter = msg.getVoter();
        CosmosMsgItemBinding voterView = generateMsgView();
        voterView.tvTittle.setText(getContext().getString(R.string.voter));
        voterView.tvValue.setText(voter);
        addView(voterView.getRoot());


        String proposal = msg.getProposalId();
        CosmosMsgItemBinding proposalView = generateMsgView();
        proposalView.tvTittle.setText(getContext().getString(R.string.proposal));
        proposalView.tvValue.setText("#" + proposal);
        addView(proposalView.getRoot());


        String option = msg.getOption();
        CosmosMsgItemBinding optionView = generateMsgView();
        optionView.tvTittle.setText(getContext().getString(R.string.option));
        optionView.tvValue.setText(option);
        addView(optionView.getRoot());

    }

    private void addMsgSend(MsgSend msgSend) {
        String fromAddress = msgSend.getFromAddress();
        CosmosMsgItemBinding from = generateMsgView();
        from.tvTittle.setText(getContext().getString(R.string.tx_from));
        from.tvValue.setText(fromAddress);
        addView(from.getRoot());


        String toAddress = msgSend.getToAddress();
        CosmosMsgItemBinding to = generateMsgView();
        to.tvTittle.setText(getContext().getString(R.string.tx_to));
        to.tvValue.setText(toAddress);
        addView(to.getRoot());


        List<Amount> amounts = msgSend.getAmounts();
        if (amounts.size() > 0) {
            addAmount(amounts.get(0));
        }
    }

    private CosmosMsgItemBinding generateMsgView() {
        return DataBindingUtil.inflate(inflater, R.layout.cosmos_msg_item, null, false);
    }


    private LayoutParams getContainerLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int) getResources().getDimension(R.dimen.tutorial_item_margin);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.tutorial_item_margin);
        layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.tutorial_item_margin);
        layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.tutorial_item_margin);
        return layoutParams;
    }

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
