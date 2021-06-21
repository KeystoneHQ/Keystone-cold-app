/*
 *
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
 *
 */

package com.keystone.cold.callables;

import com.keystone.cold.encryption.interfaces.CONSTANTS;
import com.keystone.cold.encryptioncore.base.Packet;

import java.util.concurrent.Callable;

public class RestartSeCallable implements Callable {
    @Override
    public Boolean call() {
        try {
            final Packet packet = new Packet.Builder(CONSTANTS.METHODS.RESTART_SE)
                    .build();
            final Callable<Packet> callable = new BlockingCallable(packet);
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
