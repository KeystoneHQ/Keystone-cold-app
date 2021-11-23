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

package com.keystone.cold.ui.views.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.MutableLiveData;

import com.keystone.cold.AppExecutors;
import com.keystone.cold.R;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.URDecoder;
import com.sparrowwallet.hummingbird.UREncoder;

import org.spongycastle.util.encoders.Hex;

import static com.sparrowwallet.hummingbird.UR.fromBytes;

public class DynamicQrCodeView extends LinearLayout implements QrCodeHolder {
    private static final int DURATION = 200; //ms
    private String data;
    public int count;
    private final Cache mCache = Cache.getInstance();
    private ProgressBar progressBar;
    private ImageView img;
    public final Handler handler = new Handler();
    public final Runnable runnable;
    private final AppCompatActivity mActivity;
    private boolean detached = false;
    private MutableLiveData<UR> URSubscriber = new MutableLiveData<>();

    private UREncoder encoder;

    public enum QrCapacity {
        HIGH(800),
        MID(500),
        LOW(200);

        public int capacity;

        QrCapacity(int i) {
            this.capacity = i;
        }
    }

    public QrCapacity qrCapacity = QrCapacity.MID;

    public int currentIndex = 0;

    public boolean autoAnimate = true;

    private boolean multiPart = true;

    public DynamicQrCodeView(Context context) {
        this(context, null);
    }

    public DynamicQrCodeView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mActivity = (AppCompatActivity) context;
        runnable = this::showQrCode;
    }

    public void disableMultipart() {
        multiPart = false;
    }

    public MutableLiveData<UR> getURSubscriber() {
        return URSubscriber;
    }

    /*
     if multiPart, s should be hex string
     */
    public void setData(String s) {
        data = s;
        if (multiPart) {
            AppExecutors.getInstance().networkIO().execute(() -> {
                try {
                    UR ur;
                    if (data.toUpperCase().startsWith("UR:")) {
                        ur = URDecoder.decode(data);
                    } else {
                        ur = fromBytes(Hex.decode(data));
                    }
                    URSubscriber.postValue(ur);
                    encoder = new UREncoder(ur, qrCapacity.capacity, 10, 0);
                    mCache.restart();
                    handler.removeCallbacks(runnable);
                    handler.post(runnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            URSubscriber.postValue(null);
            count = 1;
            showQrCode();
        }
    }

    public void displayUR(UR ur) {
        data = ur.toString();
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                mCache.restart();
                URSubscriber.postValue(ur);
                encoder = new UREncoder(ur, qrCapacity.capacity, 10, 0);
                handler.removeCallbacks(runnable);
                handler.post(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void disableModal() {
        img.setOnClickListener(null);
        findViewById(R.id.hint).setVisibility(View.GONE);
    }

    public void setAutoAnimate(boolean autoAnimate) {
        this.autoAnimate = autoAnimate;
        if (!autoAnimate) {
            handler.removeCallbacks(runnable);
        } else {
            handler.post(runnable);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        img = findViewById(R.id.img);
        progressBar = findViewById(R.id.progress);
        OnClickListener showModal = v -> {
            if (!TextUtils.isEmpty(data)) {
                showModal();
            }
        };
        img.setOnClickListener(showModal);
        findViewById(R.id.hint).setOnClickListener(showModal);
    }

    private void showModal() {
        QrCodeModal modal = QrCodeModal.newInstance(data, multiPart);
        modal.show(mActivity.getSupportFragmentManager(), "");
    }

    public void showQrCode() {
        if (multiPart) {
            String part = encoder.nextPart().toUpperCase();
            if (ViewCompat.isLaidOut(this)) {
                mCache.offer(part, this);
            } else {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mCache.offer(part, DynamicQrCodeView.this);
                    }
                });
            }
            if (autoAnimate && !detached) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, DURATION);
            }
        } else {
            if (ViewCompat.isLaidOut(this)) {
                if (data.toUpperCase().startsWith("UR:")) {
                    mCache.offer(data.toUpperCase(), this);
                } else {
                    mCache.offer(data, this);
                }
            } else {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mCache.offer(data, DynamicQrCodeView.this);
                    }
                });
            }
        }
    }

    private void setImageBitmap(Bitmap bm) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            progressBar.setVisibility(GONE);
            img.setVisibility(VISIBLE);
            img.setImageBitmap(bm);
        });
    }

    @Override
    public void onRender(Bitmap bm) {
        setImageBitmap(bm);
    }

    @Override
    public int getViewWidth() {
        return img.getWidth();
    }

    @Override
    public int getViewHeight() {
        return img.getHeight();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(runnable);
        detached = true;
        mCache.restart();
    }
}