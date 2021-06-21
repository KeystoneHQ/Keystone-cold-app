/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.protocol.builder;

import android.text.TextUtils;

import com.keystone.cold.encryptioncore.utils.Preconditions;
import com.keystone.cold.protobuf.PayloadProtoc;
import com.keystone.cold.protobuf.SyncProtoc;
import com.keystone.cold.protocol.EncodeConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SyncBuilder extends BaseBuilder {


    public SyncBuilder(EncodeConfig config) {
        super(PayloadProtoc.Payload.Type.TYPE_SYNC, config);
    }

    @Override
    public String build() {
        payload.setSync(sync);
        return super.build();
    }

    public void addCoin(Coin coin) {
        sync.addCoins(coin.toProto());
    }

    public int getCoinsCount() {
        return sync.getCoinsCount();
    }

    public static class Sync {
        public List<Coin> coins = new ArrayList<>();
    }

    public static class Coin {
        public String coinCode;
        public boolean active;
        public final List<Account> accounts = new ArrayList<>();

        public Coin() {
        }

        public Coin setCoinCode(String coinCode) {
            this.coinCode = coinCode;
            return this;
        }

        public Coin setActive(boolean active) {
            this.active = active;
            return this;
        }

        public Coin setAccounts(List<Account> accounts) {
            this.accounts.clear();
            this.accounts.addAll(accounts);
            return this;
        }

        public Coin addAccount(Account account) {
            this.accounts.add(account);
            return this;
        }

        SyncProtoc.Coin.Builder toProto() {
            Preconditions.checkArgument(!TextUtils.isEmpty(coinCode), "coinCode is null");
            Preconditions.checkArgument(accounts.size() > 0, "accounts is empty");
            List<SyncProtoc.Account> accounts = this.accounts.stream()
                    .map(a -> a.toProto().build())
                    .collect(Collectors.toList());
            return SyncProtoc.Coin.newBuilder()
                    .setCoinCode(coinCode)
                    .setActive(active)
                    .addAllAccounts(accounts);
        }
    }

    public static class Account {
        public String hdPath;
        public String xPub;
        public int addressLength;
        public boolean isMultiSign;

        SyncProtoc.Account.Builder toProto() {
            Preconditions.checkArgument(!TextUtils.isEmpty(hdPath), "hdPath is null");
            Preconditions.checkArgument(!TextUtils.isEmpty(xPub), "xpub is null");
            return SyncProtoc.Account.newBuilder()
                    .setHdPath(hdPath)
                    .setXPub(xPub)
                    .setAddressLength(addressLength)
                    .setIsMultiSign(isMultiSign);
        }
    }
}
