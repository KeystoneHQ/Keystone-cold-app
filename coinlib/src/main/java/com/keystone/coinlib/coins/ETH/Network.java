/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.ETH;

public enum Network {
    Mainnet(1),
    Ropsten(3),
    Rinkeby(4),
    Goerli(5),
    Kovan(42),
    Ethereum_Classic(61);

    private final int chainId;

    Network(int chainId) {
        this.chainId = chainId;
    }

    public int getChainId() {
        return chainId;
    }

    public static Network getNetwork(int chainId) {
        for (Network value : Network.values()) {
            if (chainId == value.chainId) {
                return value;
            }
        }
        return null;
    }
}
