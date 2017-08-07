package com.megvii.idcard.sdk;

import android.content.Context;
import android.util.Log;

import com.megvii.idcard.sdk.jni.IDCardApi;
import com.megvii.idcard.sdk.util.SDKUtil;

import java.io.Serializable;

public class IDCard {
	public final static int IMAGEMODE_GRAY = 0;
	public final static int IMAGEMODE_BGR = 1;
	public final static int IMAGEMODE_NV21 = 2;
	public final static int IMAGEMODE_RGBA = 3;

	private long IDCardHandle;

	public String init(Context context, byte[] model) {
		if (context == null || model == null) {
			int lastErrorCode = 1;
			return SDKUtil.getErrorType(lastErrorCode);
		}

		long handle = IDCardApi.nativeInit(context, model);
		String errorType = SDKUtil.getErrorType((int) handle);
		if (errorType == null) {
			IDCardHandle = handle;
			return null;
		}

		return errorType;
	}

	public IDCardConfig getIdCardConfig() {
		float[] configs = IDCardApi.nativeGetIDCardConfig(IDCardHandle);
		IDCardConfig idCardConfig = new IDCardConfig();
		idCardConfig.orientation = (int) configs[0];
		idCardConfig.shadowAreaTh = configs[1];
		idCardConfig.faculaAreaTh = configs[2];
		idCardConfig.cardAreaTh = configs[3];
		idCardConfig.shadowConfirmTh = (int) configs[4];
		idCardConfig.faculaActivatedTh = (int) configs[5];
		idCardConfig.faculaConfirmTh = (int) configs[6];
		idCardConfig.roi_left = (int) configs[7];
		idCardConfig.roi_top = (int) configs[8];
		idCardConfig.roi_right = (int) configs[9];
		idCardConfig.roi_bottom = (int) configs[10];
		for (int i = 0; i < configs.length; i++) {
			Log.w("ceshi", i + "===" + configs[i]);
		}
		return idCardConfig;
	}

	public void setIdCardConfig(IDCardConfig idCardConfig) {
		IDCardApi.nativeSetIDCardConfig(IDCardHandle, idCardConfig.orientation, idCardConfig.shadowAreaTh,
				idCardConfig.faculaAreaTh, idCardConfig.cardAreaTh, idCardConfig.shadowConfirmTh,
				idCardConfig.faculaActivatedTh, idCardConfig.faculaConfirmTh, idCardConfig.roi_left,
				idCardConfig.roi_top, idCardConfig.roi_right, idCardConfig.roi_bottom, idCardConfig.need_filter);
	}

	public IDCardDetect detect(byte[] imageData, int width, int height, int imageMode) {
//		getIdCardConfig();
		IDCardDetect idCardDetect = new IDCardDetect();
		float[] datas = IDCardApi.nativeDetect(IDCardHandle, imageData, width, height, imageMode);
		idCardDetect.inBound = datas[0];
		idCardDetect.isIdcard = datas[1];
		idCardDetect.clear = datas[2];
		return idCardDetect;
	}

	int offset = 0;

	public IDCardQuality CalculateQuality(float faculaePass) {
		offset = 0;
		IDCardQuality iCardQuality = new IDCardQuality();
		float[] points = IDCardApi.nativeCalculateQuality(IDCardHandle);
		iCardQuality.Shadows = getShadowInfo(points, (int) points[offset++]);
		iCardQuality.faculaes = getFaculaeInfo(points, (int) points[offset++]);
		iCardQuality.cards = getCardInfo(points, (int) points[offset++]);

		if (iCardQuality.Shadows.length == 0)
			iCardQuality.isShadowPass = true;
		if (iCardQuality.faculaes.length == 0)
			iCardQuality.isfaculaePass = true;

		// if (!isContinueShadow(iCardQuality.cards, iCardQuality.Shadows))
		// iCardQuality.isShadowPass = true;

		// if (!isContinueFaculae(iCardQuality.cards, iCardQuality.faculaes,
		// faculaePass))
		// iCardQuality.isfaculaePass = true;
		return iCardQuality;
	}

	private boolean isContinueShadow(Card[] cards, Shadow[] Shadows) {
		float[] cardAverage = cards[0].average;
		float[] cardVariance = cards[0].variance;
		for (int i = 0; i < Shadows.length; i++) {
			float[] ShadowAverage = Shadows[i].average;
			float[] ShadowVariance = Shadows[i].variance;
			for (int j = 0; j < 3; j++) {
				if ((cardAverage[j] - (float) (Math.sqrt(cardVariance[j]) * 0.8)) > ShadowAverage[j])
					return true;
				// if (cardVariance[j] < ShadowVariance[j])
				// return true;
			}
		}
		return false;
	}

	private boolean isContinueFaculae(Card[] cards, Faculae[] faculaes, float faculaePass) {
		float[] cardAverage = cards[0].average;
		float[] cardVariance = cards[0].variance;
		for (int i = 0; i < faculaes.length; i++) {
			float[] faculaeAverage = faculaes[i].average;
			float[] faculaeVariance = faculaes[i].variance;
			for (int j = 0; j < 3; j++) {
				if ((cardAverage[j] + (float) (Math.sqrt(cardVariance[j]) * faculaePass)) < faculaeAverage[j])
					return true;
				if (cardVariance[j] < faculaeVariance[j])
					return true;
			}
		}
		return false;
	}

	private Shadow[] getShadowInfo(float[] points, int size) {
		Shadow[] shadows = new Shadow[size];
		for (int i = 0; i < size; i++) {
			Shadow shadow = new Shadow();
			shadow.average = new float[3];
			for (int j = 0; j < shadow.average.length; j++) {
				shadow.average[j] = points[offset++];
			}
			shadow.variance = new float[3];
			for (int j = 0; j < shadow.variance.length; j++) {
				shadow.variance[j] = points[offset++];
			}
			int n = (int) points[offset++];
			shadow.vertex = new PointF[n];
			for (int j = 0; j < n; ++j) {
				shadow.vertex[j] = new PointF();
				shadow.vertex[j].x = points[offset++];
				shadow.vertex[j].y = points[offset++];
			}
			// draw vertex[j] vertex[(j+1)%n]
			shadows[i] = shadow;
		}
		return shadows;
	}

	private Faculae[] getFaculaeInfo(float[] points, int size) {
		Faculae[] faculaes = new Faculae[size];
		Log.w("ceshi", "size===" + size);
		for (int i = 0; i < size; i++) {
			Faculae faculae = new Faculae();
			faculae.average = new float[3];
			for (int j = 0; j < faculae.average.length; j++) {
				faculae.average[j] = points[offset++];
			}
			faculae.variance = new float[3];
			for (int j = 0; j < faculae.variance.length; j++) {
				faculae.variance[j] = points[offset++];
			}
			int n = (int) points[offset++];
			faculae.vertex = new PointF[n];
			for (int j = 0; j < n; ++j) {
				faculae.vertex[j] = new PointF();
				faculae.vertex[j].x = points[offset++];
				faculae.vertex[j].y = points[offset++];
			}
			// draw vertex[j] vertex[(j+1)%n]
			faculaes[i] = faculae;
		}
		return faculaes;
	}

	private Card[] getCardInfo(float[] points, int size) {
		Card[] cards = new Card[size];
		for (int i = 0; i < size; i++) {
			Card card = new Card();
			card.average = new float[3];
			for (int j = 0; j < card.average.length; j++) {
				card.average[j] = points[offset++];
			}
			card.variance = new float[3];
			for (int j = 0; j < card.variance.length; j++) {
				card.variance[j] = points[offset++];
			}
			int n = (int) points[offset++];
			card.vertex = new PointF[n];
			for (int j = 0; j < n; ++j) {
				card.vertex[j] = new PointF();
				card.vertex[j].x = points[offset++];
				card.vertex[j].y = points[offset++];
			}
			// draw vertex[j] vertex[(j+1)%n]
			cards[i] = card;
		}
		return cards;
	}

	public void release() {
		if (IDCardHandle == 0)
			return;
		IDCardApi.nativeRelease(IDCardHandle);
		IDCardHandle = 0;
	}

	/**
	 * 获取截止日期
	 */
	public static long getApiExpication(Context context) {
		return IDCardApi.nativeGetApiExpication(context);
	}

	public static String getVersion() {
		return IDCardApi.nativeGetVersion();
	}

	public static long getApiName() {
		return IDCardApi.nativeGetApiName();
	}

	public static class IDCardDetect {
		public float inBound, isIdcard, clear;
	}

	public static class IDCardQuality implements Serializable {
		private static final long serialVersionUID = 5507432037314593640L;

		public boolean isShadowPass = false;
		public boolean isfaculaePass = false;

		/**
		 * 生成的serialVersionUID
		 */
		public Shadow[] Shadows;
		public Faculae[] faculaes;
		public Card[] cards;
	}

	public static class Shadow implements Serializable {
		private static final long serialVersionUID = -5095788114139817829L;
		public float[] average;
		public float[] variance;
		public PointF[] vertex;
	}

	public static class Faculae implements Serializable {
		private static final long serialVersionUID = 4644547927906498343L;
		public float[] average;
		public float[] variance;
		public PointF[] vertex;
	}

	public static class Card implements Serializable {
		private static final long serialVersionUID = 3786070988580648667L;
		public float[] average;
		public float[] variance;
		public PointF[] vertex;
	}

	public static class PointF implements Serializable {
		private static final long serialVersionUID = 7096991384851649494L;
		public float x;
		public float y;
	}

	public static class IDCardConfig {
		public int orientation;
		public float shadowAreaTh;
		public float faculaAreaTh;
		public float cardAreaTh;
		public int shadowConfirmTh;
		public int faculaActivatedTh;
		public int faculaConfirmTh;
		public int roi_left;
		public int roi_top;
		public int roi_right;
		public int roi_bottom;

		public int need_filter;// 在CalculateQuality的方法中用到
	}
}
