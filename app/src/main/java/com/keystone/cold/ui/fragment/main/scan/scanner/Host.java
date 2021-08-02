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

package com.keystone.cold.ui.fragment.main.scan.scanner;

import android.os.Handler;

import com.keystone.coinlib.coins.polkadot.UOS.SubstratePayload;
import com.keystone.cold.R;
import com.keystone.cold.ui.fragment.main.scan.scanner.bean.ZxingConfig;
import com.keystone.cold.ui.fragment.main.scan.scanner.camera.CameraManager;
import com.sparrowwallet.hummingbird.UR;


public interface Host {
    ZxingConfig getConfig();

    void handleDecode(String text);

    void handleDecode(UR ur);

    void handleDecode(SubstratePayload substratePayload);

    void handleProgress(int total, int scan);

    void handleProgressPercent(double percent);

    CameraManager getCameraManager();

    Handler getHandler();
}
