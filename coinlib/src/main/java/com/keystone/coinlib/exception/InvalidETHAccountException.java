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

package com.keystone.coinlib.exception;

import com.keystone.coinlib.accounts.ETHAccount;

public class InvalidETHAccountException extends Exception {

    private ETHAccount target;
    public ETHAccount getTarget() {
        return target;
    }


    private ETHAccount account;
    public ETHAccount getAccount() {
        return account;
    }

    public InvalidETHAccountException(String message, ETHAccount account, ETHAccount target) {
        super(message);
        this.account = account;
        this.target = target;
    }


}
