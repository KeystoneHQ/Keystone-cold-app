package com.keystone.cold.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.Objects;

public class ViewUtils {

    public static Drawable addBadge(Resources res,Drawable drawable, int badgeSize){
        int radius = badgeSize / 2;
        int width = Objects.requireNonNull(drawable).getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight() + 2 * badgeSize;
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, badgeSize, width, height - badgeSize);
        drawable.draw(canvas);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawCircle(bitmap.getWidth() - radius, radius, radius, paint);
        return new BitmapDrawable(res, bitmap);
    }
}
