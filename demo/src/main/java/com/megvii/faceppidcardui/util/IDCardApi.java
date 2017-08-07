package com.megvii.faceppidcardui.util;

import android.content.Context;

public class IDCardApi {
	static {
		System.loadLibrary("IDCardWrapper");
	}

	public native long nativeInit(Context context, byte[] data, int orientation, int left, int top, int right, int bottom);

	public native void nativeRelease(long handle);

	public native float[] nativeGetData(long handle, byte[] data, int with, int height);
	
	public native float[] nativeCalculateQuality(long handle);

}
