package com.keystone.cold.remove_wallet_mode.ui.fragment.main.tx.cardano;

import java.util.List;

public class CardanoTransaction {
    private final CardanoTransactionOverview overview;
    private final CardanoTransactionDetail detail;
    private final String signData;

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

    public static class CardanoTransactionOverview {
        private final String fee;
        private final List<CardanoFrom> from;
        private final List<CardanoTo> to;
        private final String network;
        private final String method;

        private final String totalOutputAmount;
        private final String stakeAmount;
        private final String deposit;
        private final String rewardAmount;
        private final String depositReclaim;
        private final String rewardAccount;

        public CardanoTransactionOverview(String fee, List<CardanoFrom> from, List<CardanoTo> to, String network, String method, String totalOutputAmount, String stakeAmount, String deposit, String rewardAmount, String depositReclaim, String rewardAccount) {
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

        public List<CardanoFrom> getFrom() {
            return from;
        }

        public List<CardanoTo> getTo() {
            return to;
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
    }

    public static class CardanoTransactionDetail {
        private final String fee;
        private final List<CardanoFrom> from;
        private final List<CardanoTo> to;
        private final String network;
        private final String method;

        private final String totalInputAmount;
        private final String totalOutputAmount;
        private final String depositReclaim;
        private final String deposit;
        private final List<CardanoStakeAction> actions;

        public CardanoTransactionDetail(String fee, List<CardanoFrom> from, List<CardanoTo> to, String network, String method, String totalInputAmount, String totalOutputAmount, String depositReclaim, String deposit, List<CardanoStakeAction> actions) {
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

        public List<CardanoFrom> getFrom() {
            return from;
        }

        public List<CardanoTo> getTo() {
            return to;
        }

        public String getNetwork() {
            return network;
        }

        public String getMethod() {
            return method;
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
    }

    public static class CardanoFrom {
        private final String address;
        private final String amount;
        private final String path;

        public CardanoFrom(String address, String amount, String path) {
            this.address = address;
            this.amount = amount;
            this.path = path;
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
    }

    public static class CardanoTo {
        private final String address;
        private final String amount;
        private final String assetText;

        public CardanoTo(String address, String amount, String assetText) {
            this.address = address;
            this.amount = amount;
            this.assetText = assetText;
        }

        public String getAddress() {
            return address;
        }

        public String getAmount() {
            return amount;
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
}
