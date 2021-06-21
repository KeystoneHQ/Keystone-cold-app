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

package com.keystone.cold.scan.bean;

import androidx.annotation.ColorRes;

import java.io.Serializable;

public class ZxingConfig implements Serializable {
    private final boolean isFullScreenScan;
    @ColorRes
    private final int frameColor;
    @ColorRes
    private final int frameLineColor;


    public ZxingConfig(boolean isFullScreenScan,
                       int reactColor,
                       int frameLineColor) {

        this.isFullScreenScan = isFullScreenScan;
        this.frameColor = reactColor;
        this.frameLineColor = frameLineColor;
    }

    public int getFrameLineColor() {
        return frameLineColor;
    }

    public int getFrameColor() {
        return frameColor;
    }

    public boolean isFullScreenScan() {
        return isFullScreenScan;
    }

}
