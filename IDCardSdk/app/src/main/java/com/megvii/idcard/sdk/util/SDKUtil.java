package com.megvii.idcard.sdk.util;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.FROYO)
public class SDKUtil {
	
	/**
	 * 切图
	 */
	public static Bitmap cutImage(RectF rect, byte[] data, int CameraWidth, int cameraHeight, int rotate) {
		byte[] imageData = rotate(data, CameraWidth, cameraHeight, rotate);
		int width = CameraWidth;
		int height = cameraHeight;
		if (rotate == 90 || rotate == 270) {
			width = cameraHeight;
			height = CameraWidth;
		}

		YuvImage yuv = new YuvImage(imageData, ImageFormat.NV21, width, height, null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

		byte[] bytes = out.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

		if (rect == null)
			return bitmap;

		rect = new RectF(rect.left * bitmap.getWidth(), rect.top * bitmap.getHeight(), rect.right * bitmap.getWidth(),
				rect.bottom * bitmap.getHeight());
		return cropImage(rect, bitmap);
	}

	/**
	 * 切图
	 */
	public static Bitmap cropImage(RectF rect, Bitmap bitmap) {
		Log.w("ceshi", "rect===" + rect);
		float width = rect.width() * 1;
		if (width > bitmap.getWidth()) {
			width = bitmap.getWidth();
		}

		float hight = rect.height() * 1;
		if (hight > bitmap.getHeight()) {
			hight = bitmap.getHeight();
		}

		float l = rect.centerX() - (width / 2);
		if (l < 0) {
			l = 0;
		}
		float t = rect.centerY() - (hight / 2);
		if (t < 0) {
			t = 0;
		}
		if (l + width > bitmap.getWidth()) {
			width = bitmap.getWidth() - l;
		}
		if (t + hight > bitmap.getHeight()) {
			hight = bitmap.getHeight() - t;
		}

		return Bitmap.createBitmap(bitmap, (int) l, (int) t, (int) width, (int) hight);

	}


	public static byte[] rotate(byte[] data, int imageWidth, int imageHeight, int rotate) {
		switch (rotate) {
		case 0:
			return data;
		case 90:
			return rotateYUV420Degree90(data, imageWidth, imageHeight);
		case 180:
			return rotateYUV420Degree180(data, imageWidth, imageHeight);
		case 270:
			return rotateYUV420Degree270(data, imageWidth, imageHeight);
		}
		return data;
	}

	public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		// Rotate the Y luma
		int i = 0;
		for (int x = 0; x < imageWidth; x++) {
			for (int y = imageHeight - 1; y >= 0; y--) {
				yuv[i] = data[y * imageWidth + x];
				i++;
			}

		}
		// int offset = imageWidth * imageHeight;
		// for (int x = 0; x < imageWidth / 2; x ++)
		// for (int y = imageHeight / 2 - 1; y >= 0; y --) {
		// yuv[i] = data[offset + (y * imageWidth) + x];
		// i ++ ;
		// yuv[i] = data[offset + (y * imageWidth) + x + 1];
		// i ++;
		// }

		// Rotate the U and V color components
		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (int x = imageWidth - 1; x > 0; x = x - 2) {
			for (int y = 0; y < imageHeight / 2; y++) {
				yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
				i--;
				yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
				i--;
			}
		}
		return yuv;
	}

	public static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		int i = 0;
		int count = 0;

		for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
			yuv[count] = data[i];
			count++;
		}

		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth * imageHeight; i -= 2) {
			yuv[count++] = data[i - 1];
			yuv[count++] = data[i];
		}
		return yuv;
	}

	public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth, int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		// Rotate the Y luma

		int index = 0;
		int i = 0;
		for (int x = imageWidth - 1; x >= 0; x--) {
			index = 0;
			for (int y = 0; y < imageHeight; y++) {
				yuv[i] = data[index + x];
				i++;

				index += imageWidth;
			}

		}
		// Rotate the U and V color components
		i = imageWidth * imageHeight;
		int count = imageWidth * imageHeight;
		for (int x = imageWidth - 1; x > 0; x = x - 2) {
			index = count;
			for (int y = 0; y < imageHeight / 2; y++) {

				yuv[i] = data[index + (x - 1)];
				i++;
				yuv[i] = data[index + x];
				i++;

				index += imageWidth;
			}
		}
		return yuv;
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	public static String getErrorType(int retCode) {
		switch (retCode) {
		case 0:
			return "MG_RETCODE_OK";
		case -1:
			return "MG_RETCODE_FAILED";
		case 1:
			return "MG_RETCODE_INVALID_ARGUMENT";
		case 2:
			return "MG_RETCODE_INVALID_HANDLE";
		case 3:
			return "MG_RETCODE_INDEX_OUT_OF_RANGE";
		case 101:
			return "MG_RETCODE_EXPIRE";
		case 102:
			return "MG_RETCODE_INVALID_BUNDLEID";
		case 103:
			return "MG_RETCODE_INVALID_LICENSE";
		case 104:
			return "MG_RETCODE_INVALID_MODEL";
		}

		return null;
	}
}
