package com.megvii.idcard.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import com.megvii.idcard.sdk.IDCard.IDCardConfig;
import com.megvii.idcard.sdk.IDCard.IDCardDetect;
import com.megvii.idcard.sdk.IDCard.IDCardQuality;
import com.megvii.idcard.sdk.util.SDKUtil;

import java.util.ArrayList;

public class IDCardQualityAssessment {

	private IDCard mIdCard;
	private RectF mRoi;
	private int orientation;
	public boolean isFilter;
	public float mClear = 0.8f;
	public float mIdcard = 0.5f;
	public float mBound = 0.8f;

	/**
	 * 初始化成功返回null，初始化失败返回错误原因
	 */
	public String init(Context context, byte[] model) {

		mIdCard = new IDCard();

		return mIdCard.init(context, model);
	}

	public IDCardQualityResult getQuality(byte[] data, int width, int height, int imageMode) {
		IDCardQualityResult result = new IDCardQualityResult();
		IDCardDetect iCardDetect = mIdCard.detect(data, width, height, imageMode);
		final float in_bound = iCardDetect.inBound;
		final float is_idcard = iCardDetect.isIdcard;
		final float clear = iCardDetect.clear;
		result.fails.clear();
		if (in_bound < mBound)
			result.fails.add(IDCardQualityResult.IDCardFailedType.QUALITY_FAILED_TYPE_OUTSIDETHEROI);
		if (in_bound < mIdcard)
			result.fails.add(IDCardQualityResult.IDCardFailedType.QUALITY_FAILED_TYPE_OUTSIDETHEROI);
		if (in_bound < mClear)
			result.fails.add(IDCardQualityResult.IDCardFailedType.QUALITY_FAILED_TYPE_BLUR);

		if (in_bound >= mBound && is_idcard >= mIdcard && clear >= mClear) {
			IDCardQuality idCardQuality = mIdCard.CalculateQuality(0);
			result.mIdCardQuality = idCardQuality;

			if (!idCardQuality.isShadowPass)
				result.fails.add(0, IDCardQualityResult.IDCardFailedType.QUALITY_FAILED_TYPE_SHADOW);
			if (!idCardQuality.isfaculaePass)
				result.fails.add(0, IDCardQualityResult.IDCardFailedType.QUALITY_FAILED_TYPE_SPECULARHIGHLIGHT);

			if (idCardQuality.isfaculaePass && idCardQuality.isShadowPass) {
				result.mIDCardBitmap = SDKUtil.cutImage(mRoi, data, width, height, orientation);
				result.isValid = true;
			}
		}

		result.fails.add(IDCardQualityResult.IDCardFailedType.QUALITY_FAILED_TYPE);
		return result;
	}

	/**
	 * @param width
	 *            表示图片的宽
	 * @param height
	 *            表示图片的高
	 * @param roi
	 *            表示图片中要截取进行计算的区域
	 * @param isVertical
	 *            表示相机是否是竖直显示
	 * @param orientation
	 *            表示相机旋转角度
	 */
	public void setConfig(int width, int height, RectF roi, int orientation, boolean isVertical) {
		this.mRoi = roi;
		this.orientation = orientation;
		IDCardConfig idCardConfig = mIdCard.getIdCardConfig();

		int left = (int) (width * roi.left);
		int top = (int) (height * roi.top);
		int right = (int) (width * roi.right);
		int bottom = (int) (height * roi.bottom);
		if (isVertical) {
			left = (int) (width * roi.top);
			top = (int) (height * roi.left);
			right = (int) (width * roi.bottom);
			bottom = (int) (height * roi.right);
		}

		idCardConfig.orientation = orientation;
		idCardConfig.shadowAreaTh = 500;
		idCardConfig.faculaAreaTh = 500;
		idCardConfig.roi_left = left;
		idCardConfig.roi_top = top;
		idCardConfig.roi_right = right;
		idCardConfig.roi_bottom = bottom;

		Log.w("ceshi", "idCardConfig.orientation===" + idCardConfig.orientation + ", " + left + ", " + top + ", "
				+ right + ", " + bottom);

		if (isFilter)
			idCardConfig.need_filter = 1;
		else
			idCardConfig.need_filter = 0;

		mIdCard.setIdCardConfig(idCardConfig);
	}

	public void release() {
		mIdCard.release();
	}

	public static class IDCardQualityResult {
		private static final long serialVersionUID = -4741010398548950411L;

		public Bitmap mIDCardBitmap;
		public ArrayList<IDCardFailedType> fails;
		public IDCardQuality mIdCardQuality;
		public boolean isValid;
		public float clear;
		public float isIdcard;
		public float inBound;

		public IDCardQualityResult() {
			fails = new ArrayList<IDCardFailedType>();
		}

		public boolean isValid() {
			if (isValid)
				return true;

			return false;
		}

		public Bitmap croppedImageOfIDCard() {

			return mIDCardBitmap;
		}

		public enum IDCardFailedType {
			QUALITY_FAILED_TYPE,
			QUALITY_FAILED_TYPE_OUTSIDETHEROI, QUALITY_FAILED_TYPE_BLUR, QUALITY_FAILED_TYPE_SPECULARHIGHLIGHT, QUALITY_FAILED_TYPE_SHADOW;
		}
	}
}
