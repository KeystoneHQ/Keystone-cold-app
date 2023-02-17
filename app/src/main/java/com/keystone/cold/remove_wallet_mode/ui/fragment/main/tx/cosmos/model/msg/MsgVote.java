package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cosmos.model.msg;

import org.json.JSONException;
import org.json.JSONObject;

public class MsgVote extends Msg {

    public static MsgVote from(JSONObject jsonObject) {
        try {
            String option = jsonObject.optString("option");
            String proposalId = jsonObject.getString("proposal_id");
            String voter = jsonObject.getString("voter");
            return new MsgVote(option, proposalId, voter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String option;
    private String proposalId;
    private String voter;

    public MsgVote(String option, String proposalId, String voter) {
        this.type = "Vote";
        this.option = option;
        this.proposalId = proposalId;
        this.voter = voter;
    }


    public String getOption() {
        if (option != null) {
            switch (option) {
                case "0":
                    option = "UNSPECIFIED";
                    break;
                case "1":
                    option = "YES";
                    break;
                case "2":
                    option = "ABSTAIN";
                    break;
                case "3":
                    option = "NO";
                    break;
                case "4":
                    option = "NO_WITH_VETO";
                    break;
            }
        }
        return option;
    }

    public String getProposalId() {
        return proposalId;
    }

    public String getVoter() {
        return voter;
    }

    @Override
    public String toString() {
        return "MsgVote{" +
                "type='" + type + '\'' +
                ", option='" + option + '\'' +
                ", proposalId='" + proposalId + '\'' +
                ", voter='" + voter + '\'' +
                '}';
    }
}
