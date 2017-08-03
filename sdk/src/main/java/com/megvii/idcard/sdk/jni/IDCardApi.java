package com.megvii.idcard.sdk.jni;

import android.content.Context;

public class IDCardApi {
	static {
		System.loadLibrary("IDCardWrapper");
	}

	public static native long nativeInit(Context context, byte[] modelData);

	public static native float[] nativeGetIDCardConfig(long handle);

	public static native int nativeSetIDCardConfig(long handle, int orientation, float shadow_area_th,
			float facula_area_th, float card_area_th, int shadowConfirmTh, int faculaActivatedTh, int faculaConfirmTh,
			int roi_left, int roi_top, int roi_right, int roi_bottom, int needFilter);

	public static native float[] nativeDetect(long handle, byte[] imageData, int width, int height, int imageMode);

	public static native float[] nativeCalculateQuality(long handle);

	public static native void nativeRelease(long handle);

	public static native long nativeGetApiExpication(Context context);

	public static native String nativeGetVersion();

	public static native long nativeGetApiName();

}
