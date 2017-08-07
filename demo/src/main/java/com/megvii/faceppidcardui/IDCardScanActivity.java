package com.megvii.faceppidcardui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megvii.faceppidcardui.util.IDCardIndicator;
import com.megvii.idcard.sdk.IDCard;
import com.megvii.idcard.sdk.IDCard.IDCardConfig;
import com.megvii.idcard.sdk.IDCard.IDCardDetect;
import com.megvii.idcard.sdk.IDCard.IDCardQuality;
import com.megvii.faceppidcardui.util.ConUtil;
import com.megvii.faceppidcardui.util.DialogUtil;
import com.megvii.faceppidcardui.util.ICamera;
import com.megvii.faceppidcardui.util.Util;

public class IDCardScanActivity extends Activity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

	private TextureView textureView;
	private DialogUtil mDialogUtil;
	private ICamera mICamera;// 照相机工具类
	private IDCardIndicator mIndicatorView;
	private boolean mIsVertical = false, mIsDebug = false, isTextDetect = false, isClearShadow = false;
	private float faculaPass;// 光斑敏感度
	private TextView fps, fps_1;
	private TextView errorType, verticalType;
	private IDCard mIdCard;
	private HandlerThread mHandlerThread = new HandlerThread("hhh");
	private Handler mHandler;
	private ImageView image;
	private float setClear = 0.8f, setIdcard = -1f, setBound = 0.8f;
	private RelativeLayout barRel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.megvii.faceppidcard.R.layout.idcardscan_layout);
		init();
	}

	private void init() {
		mIdCard = new IDCard();
		mIdCard.init(this, Util.readModel(this));
		setClear = getIntent().getFloatExtra("clear", 0.8f);
		setIdcard = getIntent().getFloatExtra("idcard", -1);
		setBound = getIntent().getFloatExtra("bound", 0.8f);
		faculaPass = getIntent().getFloatExtra("faculaPass", 0.3f);
		mIsVertical = getIntent().getBooleanExtra("isvertical", false);
		mIsDebug = getIntent().getBooleanExtra("isDebug", false);
		isTextDetect = getIntent().getBooleanExtra("isTextDetect", false);
		isClearShadow = getIntent().getBooleanExtra("isClearShadow", false);
		Log.w("ceshi", "setClear==" + setClear + ", setIdcard==" + setIdcard + ", setBound==" + setBound);
		Log.w("ceshi", "mIsVertical==" + mIsVertical + ", mIsDebug==" + mIsDebug + ", isTextDetect==" + isTextDetect);
		image = (ImageView) findViewById(com.megvii.faceppidcard.R.id.image);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
		mICamera = new ICamera(mIsVertical);
		mDialogUtil = new DialogUtil(this);
		textureView = (TextureView) findViewById(com.megvii.faceppidcard.R.id.idcardscan_layout_surface);
		textureView.setSurfaceTextureListener(this);
		textureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mICamera.autoFocus();
			}
		});
		barRel = (RelativeLayout) findViewById(com.megvii.faceppidcard.R.id.idcard_layout_barRel);
		fps = (TextView) findViewById(com.megvii.faceppidcard.R.id.idcardscan_layout_fps);
		fps.setVisibility(View.VISIBLE);
		fps_1 = (TextView) findViewById(com.megvii.faceppidcard.R.id.idcardscan_layout_fps_1);
		fps_1.setVisibility(View.VISIBLE);
		errorType = (TextView) findViewById(com.megvii.faceppidcard.R.id.idcardscan_layout_error_type);
		verticalType = (TextView) findViewById(com.megvii.faceppidcard.R.id.idcardscan_layout_verticalerror_type);
		mIndicatorView = (IDCardIndicator) findViewById(com.megvii.faceppidcard.R.id.idcardscan_layout_indicator);

		if (mIsVertical) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			verticalType.setVisibility(View.VISIBLE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			errorType.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Camera mCamera = mICamera.openCamera(this);
		if (mCamera != null) {
			RelativeLayout.LayoutParams layout_params = mICamera.getLayoutParam(this);
			textureView.setLayoutParams(layout_params);
			mIndicatorView.setLayoutParams(layout_params);

		} else {
			mDialogUtil.showDialog("打开摄像头失败");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mICamera.closeCamera();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mIdCard.release();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDialogUtil.onDestory();
	}

	int width;
	int height;
	int orientation = 0;

	private void doPreview() {
		if (!mHasSurface)
			return;

		mICamera.startPreview(textureView.getSurfaceTexture());

		IDCardConfig idCardConfig = mIdCard.getIdCardConfig();

		RectF rectF = mIndicatorView.getPosition();
		width = mICamera.cameraWidth;
		height = mICamera.cameraHeight;

		int left = (int) (width * rectF.left);
		int top = (int) (height * rectF.top);
		int right = (int) (width * rectF.right);
		int bottom = (int) (height * rectF.bottom);
		if (mIsVertical) {
			left = (int) (width * rectF.top);
			top = (int) (height * rectF.left);
			right = (int) (width * rectF.bottom);
			bottom = (int) (height * rectF.right);
			orientation = 180 - mICamera.orientation;
		}

		idCardConfig.orientation = orientation;
		idCardConfig.shadowAreaTh = 500;
		idCardConfig.faculaAreaTh = 500;
		idCardConfig.roi_left = left;
		idCardConfig.roi_top = top;
		idCardConfig.roi_right = right;
		idCardConfig.roi_bottom = bottom;

		mIdCard.setIdCardConfig(idCardConfig);
	}

	private boolean mHasSurface = false;

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		mHasSurface = true;
		doPreview();

		mICamera.actionDetect(this);
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mHasSurface = false;
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

	boolean isSuccess = false;

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		if (isSuccess)
			return;
		isSuccess = true;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				long actionDetectTme = System.currentTimeMillis();
				IDCardDetect iCardDetect = mIdCard.detect(data, width, height, IDCard.IMAGEMODE_NV21);
				final long DetectTme = System.currentTimeMillis() - actionDetectTme;
				final float in_bound = iCardDetect.inBound;
				final float is_idcard = iCardDetect.isIdcard;
				final float clear = iCardDetect.clear;
				String errorStr = "";

				if (clear < setClear)
					errorStr = "请点击屏幕对焦";
				else if (in_bound < setBound)
					errorStr = "请将身份证对准引导框";

				String fps_1Str = "";

				if (in_bound >= setBound && is_idcard >= setIdcard && clear >= setClear) {
					long actionQualityTme = System.currentTimeMillis();
					Log.w("ceshi", "faculaPass===" + faculaPass);
					final IDCardQuality idCardQuality = mIdCard.CalculateQuality(faculaPass);
					drawFaculae(idCardQuality);
					final long idCardQualityTme = System.currentTimeMillis() - actionQualityTme;

					fps_1Str = "\nIdCardQualityTme: " + idCardQualityTme + "\nisfaculaePass: "
							+ idCardQuality.isfaculaePass + "\nfaculaeLenth: " + idCardQuality.faculaes.length
							+ "\nShadowLenth: " + idCardQuality.Shadows.length;
					if (!idCardQuality.isfaculaePass)
						errorStr = "有光斑";
					if (idCardQuality.isfaculaePass && idCardQuality.isShadowPass) {
						Bitmap bitmap = ConUtil.cutImage(mIndicatorView.getPosition(), data, mICamera.mCamera,
								mIsVertical);
						String path = ConUtil.saveBitmap(IDCardScanActivity.this, bitmap);
						enterToResult(path, idCardQuality, clear, is_idcard, in_bound);
					} else
						isSuccess = false;
				} else {
					drawFaculae(null);
					isSuccess = false;
				}

				String fpsStr = "\nin_bound: " + in_bound + "\nis_idcard: " + is_idcard + "\nclear: " + clear
						+ "\nDetectTme: " + DetectTme;
				print(errorStr, fpsStr, fps_1Str);
			}
		});
	}

	private void drawFaculae(final IDCardQuality idCardQuality) {
		if (!mIsDebug)
			return;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mIndicatorView.setiCardQuality(idCardQuality);
			}
		});
	}

	private void print(final String errorStr, final String fpsStr, final String fps_1Str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				errorType.setText(errorStr);
				verticalType.setText(errorStr);
				if (mIsDebug) {
					fps.setText(fpsStr);
					fps_1.setText(fps_1Str);
				}
			}
		});
	}

	private void enterToResult(String path, IDCardQuality iCardQuality, float clear, float is_idcard, float in_bound) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				barRel.setVisibility(View.VISIBLE);
				errorType.setText("");
				verticalType.setText("");
			}
		});
		Intent intent = new Intent(this, IDCardResultActivity.class);
		intent.putExtra("iCardQuality", iCardQuality);
		intent.putExtra("path", path);
		intent.putExtra("clear", clear);
		intent.putExtra("is_idcard", is_idcard);
		intent.putExtra("in_bound", in_bound);
		intent.putExtra("isClearShadow", isClearShadow);
		intent.putExtra("isTextDetect", isTextDetect);
		startActivity(intent);
		//setResult(RESULT_OK, intent);
		finish();
	}

	public boolean isEven01(int num) {
		if (num % 2 == 0) {
			return true;
		} else {
			return false;
		}
	}
}