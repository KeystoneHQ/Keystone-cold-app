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

package com.keystone.coinlib.path;

import androidx.annotation.NonNull;

import com.keystone.coinlib.exception.InvalidPathException;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Account {
    private final CoinType coinType;
    private final int account;
    private final String string;

    Account(final CoinType coinType, final int account) {
        this.coinType = coinType;
        this.account = account;
        string = String.format(Locale.US, "%s/%d'", coinType, account);
    }

    public int getValue() {
        return account;
    }

    public CoinType getParent() {
        return coinType;
    }

    @NonNull
    @Override
    public String toString() {
        return string;
    }

    public Change external() {
        return new Change(this, 0);
    }

    public Change internal() {
        return new Change(this, 1);
    }

    public static Account parseAccount(String path) throws InvalidPathException {
        // M/44'/60'/0'

        String regEx = "^[mM](/\\d+'?){3}?";

        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(path);

        if (!matcher.matches()) {
            throw new InvalidPathException("Path format is not correct");
        }
        String[] split = path.split("/");
        String purposeStr = split[1].substring(0, split[1].length() - 1);
        String coinTypeStr = split[2].substring(0, split[2].length() - 1);
        String accountStr = split[3].substring(0, split[3].length() - 1);

        return CoinPath.M()
                .purpose(Integer.parseInt(purposeStr))
                .coinType(Integer.parseInt(coinTypeStr))
                .account(Integer.parseInt(accountStr));
    }

}