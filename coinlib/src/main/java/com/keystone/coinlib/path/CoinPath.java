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

import com.keystone.coinlib.exception.InvalidPathException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CoinPath {

    private static final M M = new M("M");
    private static final M m = new M("m");

    CoinPath() {
    }

    public static M m() {
        return m;
    }

    public static M M() {
        return M;
    }

    public static AddressIndex parsePath(String path) throws InvalidPathException {
        return parsePath(path,false);
    }

    public static AddressIndex parsePath(String path, boolean allHardend) throws InvalidPathException {
        // m/44'/60'/0'/0/0
        String regNormal = "^[mM](/\\d+'?){3}/[0,1]/\\d+$";
        String regAllHardend = "^[mM](/\\d+'){5}$";

        String regEx = allHardend ? regAllHardend : regNormal;
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(path);

        if (!matcher.matches()) {
            throw new InvalidPathException("Path format is not correct");
        }
        String[] split = path.split("/");
        String purposeStr = split[1].substring(0, split[1].length() - 1);
        String coinTypeStr = split[2].substring(0, split[2].length() - 1);
        String accountStr = split[3].substring(0, split[3].length() - 1);
        String changeStr;
        String addressStr;
        if (!allHardend) {
            changeStr = split[4];
            addressStr = split[5];
        } else {
            changeStr = split[4].substring(0, split[4].length() - 1);
            addressStr = split[5].substring(0, split[4].length() - 1);
        }


        Account account = m
                .purpose(Integer.parseInt(purposeStr))
                .coinType(Integer.parseInt(coinTypeStr))
                .account(Integer.parseInt(accountStr));

        Change change;
        if (Integer.parseInt(changeStr) == 0) {
            change = account.external();
        } else {
            change = account.internal();
        }

        return change.address(Integer.parseInt(addressStr));
    }
}