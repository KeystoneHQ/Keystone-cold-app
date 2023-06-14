package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CardanoTransaction {
    private final CardanoTransactionOverview overview;
    private final CardanoTransactionDetail detail;

    private final String signData;
    private String signed;

    public CardanoTransaction(CardanoTransactionOverview overview, CardanoTransactionDetail detail, String signData) {
        this.overview = overview;
        this.detail = detail;
        this.signData = signData;
    }

    public CardanoTransactionOverview getOverview() {
        return overview;
    }

    public CardanoTransactionDetail getDetail() {
        return detail;
    }

    public String getSignData() {
        return signData;
    }

    public String getSigned() {
        return signed;
    }

    public void setSigned(String signed) {
        this.signed = signed;
    }

    public static class CardanoTransactionOverview {
        private final String fee;
        private final List<CardanoAddress> from;
        private final List<CardanoAddress> to;
        private final String network;
        private final String method;

        private final String totalOutputAmount;
        private final String stakeAmount;
        private final String deposit;
        private final String rewardAmount;
        private final String depositReclaim;
        private final String rewardAccount;

        public CardanoTransactionOverview(String fee, List<CardanoAddress> from, List<CardanoAddress> to, String network, String method, String totalOutputAmount, String stakeAmount, String deposit, String rewardAmount, String depositReclaim, String rewardAccount) {
            this.fee = fee;
            this.from = from;
            this.to = to;
            this.network = network;
            this.method = method;
            this.totalOutputAmount = totalOutputAmount;
            this.stakeAmount = stakeAmount;
            this.deposit = deposit;
            this.rewardAmount = rewardAmount;
            this.depositReclaim = depositReclaim;
            this.rewardAccount = rewardAccount;
        }

        public String getFee() {
            return fee;
        }

        public String getFromText() {
            StringBuilder builder = new StringBuilder();
            for (CardanoAddress cardanoFrom : from) {
                builder.append(cardanoFrom.address);
                builder.append("\n");
                builder.append("\n");
            }
            return builder.toString();
        }

        public String getToText() {
            StringBuilder builder = new StringBuilder();
            for (CardanoAddress cardanoTo : to) {
                builder.append(cardanoTo.address);
                builder.append("\n");
                builder.append("\n");
            }
            return builder.toString();
        }

        public String getNetwork() {
            return network;
        }

        public String getMethod() {
            return method;
        }

        public String getTotalOutputAmount() {
            return totalOutputAmount;
        }

        public String getStakeAmount() {
            return stakeAmount;
        }

        public String getDeposit() {
            return deposit;
        }

        public String getRewardAmount() {
            return rewardAmount;
        }

        public String getDepositReclaim() {
            return depositReclaim;
        }

        public String getRewardAccount() {
            return rewardAccount;
        }

        public static CardanoTransactionOverview fromCardanoTxJSON(JSONObject object) throws JSONException {
            JSONObject overview = object.getJSONObject(KEY_OVERVIEW);
            String fee = object.getString(KEY_FEE);
            JSONArray from = object.getJSONArray(KEY_FROM);
            JSONArray to = object.getJSONArray(KEY_TO);
            ArrayList<CardanoAddress> fromlist = new ArrayList<>();
            for (int i = 0; i < from.length(); i++) {
                JSONObject address = from.getJSONObject(i);
                fromlist.add(new CardanoAddress(address.getString(KEY_ADDRESS), address.getString(KEY_AMOUNT), address.isNull(KEY_PATH) ? "" : address.optString(KEY_PATH), address.isNull(KEY_ASSET_TEXT) ? "" : address.optString(KEY_ASSET_TEXT)));
            }
            ArrayList<CardanoAddress> tolist = new ArrayList<>();
            for (int i = 0; i < to.length(); i++) {
                JSONObject address = to.getJSONObject(i);
                tolist.add(new CardanoAddress(address.getString(KEY_ADDRESS), address.getString(KEY_AMOUNT), address.isNull(KEY_PATH) ? "" : address.optString(KEY_PATH), address.isNull(KEY_ASSET_TEXT) ? "" : address.optString(KEY_ASSET_TEXT)));
            }
            String network = object.getString(KEY_NETWORK);
            String method = object.getString(KEY_METHOD);
            String totalOutputAmount = null;
            String stakeAmount = null;
            String deposit = null;
            String rewardAmount = null;
            String depositReclaim = null;
            String rewardAccount = null;
            JSONObject transfer = overview.isNull(KEY_TRANSFER) ? null : overview.optJSONObject(KEY_TRANSFER);
            JSONObject stake = overview.isNull(KEY_STAKE) ? null : overview.optJSONObject(KEY_STAKE);
            JSONObject withdrawal = overview.isNull(KEY_WITHDRAWAL) ? null : overview.optJSONObject(KEY_WITHDRAWAL);
            if (transfer != null) {
                totalOutputAmount = transfer.getString(KEY_TOTAL_OUTPUT_AMOUNT);
            }
            if (stake != null) {
                stakeAmount = stake.getString(KEY_STAKE_AMOUNT);
                deposit = stake.isNull(KEY_DEPOSIT) ? "" : stake.optString(KEY_DEPOSIT);
            }
            if (withdrawal != null) {
                rewardAccount = withdrawal.isNull(KEY_REWARD_ACCOUNT) ? "" : withdrawal.optString(KEY_REWARD_ACCOUNT);
                rewardAmount = withdrawal.getString(KEY_REWARD_AMOUNT);
                depositReclaim = withdrawal.isNull(KEY_DEPOSIT_RECLAIM) ? "" : withdrawal.optString(KEY_DEPOSIT_RECLAIM);
            }
            return new CardanoTransactionOverview(fee, fromlist, tolist, network, method, totalOutputAmount, stakeAmount, deposit, rewardAmount, depositReclaim, rewardAccount);
        }
    }

    public static class CardanoTransactionDetail {
        private final String fee;
        private final List<CardanoAddress> from;
        private final List<CardanoAddress> to;
        private final String network;
        private final String method;

        private final String totalInputAmount;
        private final String totalOutputAmount;
        private final String depositReclaim;
        private final String deposit;
        private final List<CardanoStakeAction> actions;

        public CardanoTransactionDetail(String fee, List<CardanoAddress> from, List<CardanoAddress> to, String network, String method, String totalInputAmount, String totalOutputAmount, String depositReclaim, String deposit, List<CardanoStakeAction> actions) {
            this.fee = fee;
            this.from = from;
            this.to = to;
            this.network = network;
            this.method = method;
            this.totalInputAmount = totalInputAmount;
            this.totalOutputAmount = totalOutputAmount;
            this.depositReclaim = depositReclaim;
            this.deposit = deposit;
            this.actions = actions;
        }

        public String getFee() {
            return fee;
        }

        public String getNetwork() {
            return network;
        }

        public String getMethod() {
            return method;
        }

        public List<CardanoAddress> getFrom() {
            return from;
        }

        public List<CardanoAddress> getTo() {
            return to;
        }

        public String getTotalInputAmount() {
            return totalInputAmount;
        }

        public String getTotalOutputAmount() {
            return totalOutputAmount;
        }

        public String getDepositReclaim() {
            return depositReclaim;
        }

        public String getDeposit() {
            return deposit;
        }

        public List<CardanoStakeAction> getActions() {
            return actions;
        }

        public static CardanoTransactionDetail fromCardanoTxJSON(JSONObject object) throws JSONException {
            String fee = object.getString(KEY_FEE);
            JSONArray from = object.getJSONArray(KEY_FROM);
            JSONArray to = object.getJSONArray(KEY_TO);
            ArrayList<CardanoAddress> fromlist = new ArrayList<>();
            for (int i = 0; i < from.length(); i++) {
                JSONObject address = from.getJSONObject(i);
                fromlist.add(new CardanoAddress(address.getString(KEY_ADDRESS), address.getString(KEY_AMOUNT), address.isNull(KEY_PATH) ? "" : address.optString(KEY_PATH), address.isNull(KEY_ASSET_TEXT) ? "" : address.optString(KEY_ASSET_TEXT)));
            }
            ArrayList<CardanoAddress> tolist = new ArrayList<>();
            for (int i = 0; i < to.length(); i++) {
                JSONObject address = to.getJSONObject(i);
                tolist.add(new CardanoAddress(address.getString(KEY_ADDRESS), address.getString(KEY_AMOUNT), address.isNull(KEY_PATH) ? "" : address.optString(KEY_PATH), address.isNull(KEY_ASSET_TEXT) ? "" : address.optString(KEY_ASSET_TEXT)));
            }
            String network = object.getString(KEY_NETWORK);
            String method = object.getString(KEY_METHOD);

            JSONObject detail = object.getJSONObject(KEY_DETAIL);

            String totalInputAmount = detail.isNull(KEY_TOTAL_INPUT_AMOUNT) ? "" : detail.optString(KEY_TOTAL_INPUT_AMOUNT);
            String totalOutputAmount = detail.getString(KEY_TOTAL_OUTPUT_AMOUNT);
            String depositReclaim = detail.isNull(KEY_DEPOSIT_RECLAIM) ? "" : detail.optString(KEY_DEPOSIT_RECLAIM);
            String deposit = detail.isNull(KEY_DEPOSIT) ? "" : detail.optString(KEY_DEPOSIT);
            ArrayList<CardanoStakeAction> actions = new ArrayList<>();
            JSONArray stakeContent = detail.isNull(KEY_DEPOSIT) ? null : detail.optJSONArray(KEY_STAKE_CONTENT);
            if (stakeContent != null) {
                for (int i = 0; i < stakeContent.length(); i++) {
                    JSONObject action = stakeContent.getJSONObject(i);
                    JSONObject stake = action.isNull(KEY_STAKE) ? null : action.optJSONObject(KEY_STAKE);
                    JSONObject withdrawal = action.isNull(KEY_WITHDRAWAL) ? null : action.optJSONObject(KEY_WITHDRAWAL);
                    JSONObject registration = action.isNull(KEY_REGISTRATION) ? null : action.optJSONObject(KEY_REGISTRATION);
                    String stakeKey = null;
                    String pool = null;
                    String registrationStakeKey = null;
                    String rewardAddress = null;
                    String rewardAmount = null;
                    String deregistrationStakeKey = null;
                    if (stake != null) {
                        pool = stake.getString(KEY_POOL);
                        stakeKey = stake.getString(KEY_STAKE_KEY);
                    }
                    if (withdrawal != null) {
                        rewardAddress = withdrawal.isNull(KEY_REWARD_ADDRESS) ? null : withdrawal.optString(KEY_REWARD_ADDRESS);
                        rewardAmount = withdrawal.isNull(KEY_REWARD_ADDRESS) ? null : withdrawal.optString(KEY_REWARD_AMOUNT);
                        deregistrationStakeKey = withdrawal.isNull(KEY_REWARD_ADDRESS) ? null : withdrawal.optString(KEY_DEREGISTRATION_STAKE_KEY);
                    }
                    if (registration != null) {
                        registrationStakeKey = registration.getString(KEY_REGISTRATION_STAKE_KEY);
                    }
                    actions.add(new CardanoStakeAction(stakeKey, pool, registrationStakeKey, rewardAddress, rewardAmount, deregistrationStakeKey));
                }
            }
            return new CardanoTransactionDetail(fee, fromlist, tolist, network, method, totalInputAmount, totalOutputAmount, depositReclaim, deposit, actions);
        }
    }

    public static class CardanoAddress {
        private final String address;
        private final String amount;
        private final String path;
        private final String assetText;

        public CardanoAddress(String address, String amount, String path, String assetText) {
            this.address = address;
            this.amount = amount;
            this.path = path;
            this.assetText = assetText;
        }

        public String getAddress() {
            return address;
        }

        public String getAmount() {
            return amount;
        }

        public String getPath() {
            return path;
        }

        public String getAssetText() {
            return assetText;
        }
    }

    public static class CardanoStakeAction {
        private final String stakeKey;
        private final String pool;
        private final String registrationStakeKey;
        private final String rewardAddress;
        private final String rewardAmount;
        private final String deregistrationStakeKey;

        public CardanoStakeAction(String stakeKey, String pool, String registrationStakeKey, String rewardAddress, String rewardAmount, String deregistrationStakeKey) {
            this.stakeKey = stakeKey;
            this.pool = pool;
            this.registrationStakeKey = registrationStakeKey;
            this.rewardAddress = rewardAddress;
            this.rewardAmount = rewardAmount;
            this.deregistrationStakeKey = deregistrationStakeKey;
        }

        public String getStakeKey() {
            return stakeKey;
        }

        public String getPool() {
            return pool;
        }

        public String getRegistrationStakeKey() {
            return registrationStakeKey;
        }

        public String getRewardAddress() {
            return rewardAddress;
        }

        public String getRewardAmount() {
            return rewardAmount;
        }

        public String getDeregistrationStakeKey() {
            return deregistrationStakeKey;
        }
    }


    private final static String KEY_OVERVIEW = "overview";
    private final static String KEY_DETAIL = "detail";
    private final static String KEY_FEE = "fee";
    private final static String KEY_NETWORK = "network";
    private final static String KEY_METHOD = "method";
    private final static String KEY_SIGN_DATA = "sign_data";
    private final static String KEY_FROM = "from";
    private final static String KEY_TO = "to";
    private final static String KEY_TRANSFER = "transfer";
    private final static String KEY_STAKE = "stake";
    private final static String KEY_WITHDRAWAL = "withdrawal";
    private final static String KEY_TOTAL_OUTPUT_AMOUNT = "total_output_amount";
    private final static String KEY_STAKE_AMOUNT = "stake_amount";
    private final static String KEY_DEPOSIT = "deposit";
    private final static String KEY_REWARD_AMOUNT = "reward_amount";
    private final static String KEY_DEPOSIT_RECLAIM = "deposit_reclaim";
    private final static String KEY_REWARD_ACCOUNT = "reward_account";
    private final static String KEY_TOTAL_INPUT_AMOUNT = "total_input_amount";
    private final static String KEY_STAKE_CONTENT = "stake_content";
    private final static String KEY_REGISTRATION = "registration";
    private final static String KEY_STAKE_KEY = "stake_key";
    private final static String KEY_POOL = "pool";
    private final static String KEY_REGISTRATION_STAKE_KEY = "registration_stake_key";
    private final static String KEY_REWARD_ADDRESS = "reward_address";
    private final static String KEY_DEREGISTRATION_STAKE_KEY = "deregistration_stake_key";
    private final static String KEY_ADDRESS = "address";
    private final static String KEY_AMOUNT = "amount";
    private final static String KEY_PATH = "path";
    private final static String KEY_ASSET_TEXT = "assets_text";

    public static CardanoTransaction fromJSON(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        String signData = jsonObject.getString(KEY_SIGN_DATA);
        return new CardanoTransaction(CardanoTransactionOverview.fromCardanoTxJSON(jsonObject), CardanoTransactionDetail.fromCardanoTxJSON(jsonObject), signData);
    }
}
