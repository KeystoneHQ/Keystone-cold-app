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

package com.keystone.cold.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.keystone.cold.Utilities;
import com.keystone.cold.selfcheck.SecurityCheck;
import com.keystone.cold.ui.AttackWarningActivity;

import static com.keystone.cold.selfcheck.SecurityCheck.RESULT_UNDER_ATTACK;

public class AttackCheckingService extends Service {

    private Handler handler;
    private SecurityCheck securityCheck;

    private final int checkingInterval = 60 * 1000;
    private final Runnable checkingRunnable = new Runnable() {
        @Override
        public void run() {
            SecurityCheck.CheckResult result = securityCheck.attackChecking(AttackCheckingService.this);
            if (result.result == RESULT_UNDER_ATTACK) {
                Utilities.setAttackDetected(AttackCheckingService.this);
                Bundle data = new Bundle();
                data.putInt("firmware", result.firmwareStatusCode);
                data.putInt("system", result.systemStatusCode);
                data.putInt("signature", result.signatureStatusCode);
                Intent intent = new Intent(AttackCheckingService.this,
                        AttackWarningActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtras(data);
                startActivity(intent);
            } else {
                Utilities.setAttackDetected(AttackCheckingService.this, false);
                handler.postDelayed(this, checkingInterval);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        securityCheck = new SecurityCheck();
        HandlerThread thread = new HandlerThread("security checking thread");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.postDelayed(checkingRunnable, checkingInterval);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i1) {
        return super.onStartCommand(intent, i, i1);
    }
}
