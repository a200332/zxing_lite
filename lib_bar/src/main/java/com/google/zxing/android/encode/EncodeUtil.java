package com.google.zxing.android.encode;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.text.TextUtils;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * description: 创建二维码
 * create by kalu on 2019/1/30 13:08
 */
public class EncodeUtil {

    public static Bitmap encode(String str) {
        return encode(str, 500);
    }

    public static Bitmap encode(String str, int size) {
        return encode(str, size, null);
    }

    public static Bitmap encode(String str, Resources resources, int logo) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, logo, options);
        int sampleSize = options.outHeight / 400;
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            options.outConfig = Bitmap.Config.RGB_565;
        }
        options.inJustDecodeBounds = false;
        return encode(str, 500, BitmapFactory.decodeResource(resources, logo, options));
    }

    public static Bitmap encode(String str, Bitmap logo) {
        return encode(str, 500, logo);
    }

    public static Bitmap encode(String str, Resources resources, int size, int logo) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, logo, options);
        int sampleSize = options.outHeight / 400;
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            options.outConfig = Bitmap.Config.RGB_565;
        }
        options.inJustDecodeBounds = false;
        return encode(str, size, BitmapFactory.decodeResource(resources, logo, options));
    }

    public static Bitmap encode(String str, int size, Bitmap logo) {

        try {
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            // step1
            ErrorCorrectionLevel level = (null == logo ? ErrorCorrectionLevel.L : ErrorCorrectionLevel.H);
            BitMatrix bitMatrix = new QRCodeWriter().encode(str, size, size, 0, level);

            // step2
            int[] pixels = new int[size * size];
            if (null == logo) {
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * size + x] = 0xff000000;
                        } else {
                            pixels[y * size + x] = 0xffffffff;
                        }
                    }
                }
            } else {

                int IMAGE_HALFWIDTH = size / 10;
                int width = bitMatrix.getWidth();//矩阵高度
                int height = bitMatrix.getHeight();//矩阵宽度
                int halfW = width / 2;
                int halfH = height / 2;
                Matrix m = new Matrix();
                float sx = (float) 2 * IMAGE_HALFWIDTH / logo.getWidth();
                float sy = (float) 2 * IMAGE_HALFWIDTH / logo.getHeight();
                m.setScale(sx, sy);
                //设置缩放信息
                //将logo图片按martix设置的信息缩放
                logo = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), m, false);

                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                                && y > halfH - IMAGE_HALFWIDTH
                                && y < halfH + IMAGE_HALFWIDTH) {
                            //该位置用于存放图片信息
                            //记录图片每个像素信息
                            pixels[y * width + x] = logo.getPixel(x - halfW + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                        } else {
                            if (bitMatrix.get(x, y)) {
                                pixels[y * size + x] = 0xff000000;
                            } else {
                                pixels[y * size + x] = 0xffffffff;
                            }
                        }
                    }
                }
            }

            // step3
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
