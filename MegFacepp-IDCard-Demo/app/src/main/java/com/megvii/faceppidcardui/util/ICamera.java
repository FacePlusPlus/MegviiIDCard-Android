package com.megvii.faceppidcardui.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.view.Surface;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 照相机工具类
 */
public class ICamera {

	public Camera mCamera;
	public int cameraWidth;
	public int cameraHeight;
	private int cameraId = 0;// 后置摄像头
	private boolean mIsVertical = false;
	private int screenWidth;
	private int screenHeight;
	public int orientation;

	public ICamera(boolean isVertical) {
		this.mIsVertical = isVertical;
	}

	/**
	 * 打开相机
	 */
	public Camera openCamera(Activity activity) {
		try {
			screenWidth = activity.getWindowManager().getDefaultDisplay()
					.getWidth();
			screenHeight = activity.getWindowManager().getDefaultDisplay()
					.getHeight();
			mCamera = Camera.open(cameraId);
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(cameraId, cameraInfo);
			Camera.Parameters params = mCamera.getParameters();
			Camera.Size bestPreviewSize = getNearestRatioSize(
					mCamera.getParameters(), screenWidth, screenHeight);
			cameraWidth = bestPreviewSize.width;
			cameraHeight = bestPreviewSize.height;
			params.setPreviewSize(cameraWidth, cameraHeight);
			List<String> focusModes = params.getSupportedFocusModes();
			if (focusModes
					.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}
			orientation = getCameraAngle(activity);
			mCamera.setDisplayOrientation(orientation);
			mCamera.setParameters(params);
			return mCamera;
		} catch (Exception e) {
			return null;
		}
	}


	public void autoFocus() {
		try {
			if (mCamera != null) {
				Parameters parameters = mCamera.getParameters();
				List<String> focusModes = parameters.getSupportedFocusModes();
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
					mCamera.cancelAutoFocus();
					parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
					mCamera.setParameters(parameters);
					mCamera.autoFocus(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// 通过屏幕参数、相机预览尺寸计算布局参数
	public RelativeLayout.LayoutParams getLayoutParam(Activity activity) {
		float scale = cameraWidth * 1.0f / cameraHeight;

		int layout_width = Screen.mWidth;
		int layout_height = (int) (layout_width * scale);

		if (Screen.mWidth >= Screen.mHeight) {
			layout_height = Screen.mHeight;
			layout_width = (int) (layout_height / scale);
		}

		if(!mIsVertical){
			int temp_value = layout_height;
			layout_height = layout_width;
			layout_width = temp_value;
		}

		RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
				layout_width, layout_height);
		layout_params.addRule(RelativeLayout.CENTER_HORIZONTAL);// 设置照相机水平居中

		return layout_params;








//		float scale = Math.min(screenWidth * 1.0f / cameraWidth, screenHeight
//				* 1.0f / cameraHeight);
//		int layout_width = (int) (scale * cameraWidth);
//		int layout_height = (int) (scale * cameraHeight);
//
//		if (mIsVertical) {
//			scale = Math.min(screenWidth * 1.0f / cameraHeight, screenHeight
//					* 1.0f / cameraWidth);
//			layout_width = (int) (scale * cameraHeight);
//			layout_height = (int) (scale * cameraWidth);
//		}
//
//		RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
//				layout_width, layout_height);
//		layout_params.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//		return layout_params;
	}

	/**
	 * 开始检测脸
	 */
	public void actionDetect(Camera.PreviewCallback mActivity) {
		if (mCamera != null) {
			mCamera.setPreviewCallback(mActivity);
		}
	}

	public void startPreview(SurfaceTexture surfaceTexture) {
		if (mCamera != null) {
			try {
				mCamera.setPreviewTexture(surfaceTexture);
				mCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void closeCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * 通过传入的宽高算出最接近于宽高值的相机大小
	 */
	private Camera.Size calBestPreviewSize(Camera.Parameters camPara,
                                           final int width, final int height) {
		List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
		ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
		for (Camera.Size tmpSize : allSupportedSize) {
			if (tmpSize.width > tmpSize.height) {
				widthLargerSize.add(tmpSize);
			}
		}

		Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size lhs, Camera.Size rhs) {
				int off_one = Math.abs(lhs.width * lhs.height - width * height);
				int off_two = Math.abs(rhs.width * rhs.height - width * height);
				return off_one - off_two;
			}
		});

		return widthLargerSize.get(0);
	}

	public static Camera.Size getNearestRatioSize(Camera.Parameters para,
                                                  final int screenWidth, final int screenHeight) {
		List<Camera.Size> supportedSize = para.getSupportedPreviewSizes();
		for (Camera.Size tmp : supportedSize) {
			if (tmp.width == 1280 && tmp.height == 720) {
				return tmp;
			}
		}
		Collections.sort(supportedSize, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size lhs, Camera.Size rhs) {
				int diff1 = (((int) ((1000 * (Math.abs(lhs.width
						/ (float) lhs.height - screenWidth
						/ (float) screenHeight))))) << 16)
						- lhs.width;
				int diff2 = (((int) (1000 * (Math.abs(rhs.width
						/ (float) rhs.height - screenWidth
						/ (float) screenHeight)))) << 16)
						- rhs.width;

				return diff1 - diff2;
			}
		});

		return supportedSize.get(0);
	}

	/**
	 * 打开前置或后置摄像头
	 */
	public Camera getCameraSafely(int cameraId) {
		Camera camera = null;
		try {
			camera = Camera.open(cameraId);
		} catch (Exception e) {
			camera = null;
		}
		return camera;
	}

	public RelativeLayout.LayoutParams getParams(Camera camera) {
		Camera.Parameters camPara = camera.getParameters();
		// 注意Screen是否初始化
		Camera.Size bestPreviewSize = calBestPreviewSize(camPara, screenWidth,
				screenHeight);
		cameraWidth = bestPreviewSize.width;
		cameraHeight = bestPreviewSize.height;
		camPara.setPreviewSize(cameraWidth, cameraHeight);
		camera.setParameters(camPara);

		float scale = bestPreviewSize.width / bestPreviewSize.height;

		RelativeLayout.LayoutParams layoutPara = new RelativeLayout.LayoutParams(
				(int) (bestPreviewSize.width),
				(int) (bestPreviewSize.width / scale));

		layoutPara.addRule(RelativeLayout.CENTER_HORIZONTAL);// 设置照相机水平居中
		return layoutPara;
	}

	public Bitmap getBitMap(byte[] data, Camera camera, boolean mIsFrontalCamera) {
		int width = camera.getParameters().getPreviewSize().width;
		int height = camera.getParameters().getPreviewSize().height;
		YuvImage yuvImage = new YuvImage(data, camera.getParameters()
				.getPreviewFormat(), width, height, null);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		yuvImage.compressToJpeg(new Rect(0, 0, width, height), 80,
				byteArrayOutputStream);
		byte[] jpegData = byteArrayOutputStream.toByteArray();
		// 获取照相后的bitmap
		Bitmap tmpBitmap = BitmapFactory.decodeByteArray(jpegData, 0,
				jpegData.length);
		Matrix matrix = new Matrix();
		matrix.reset();
		if (mIsFrontalCamera) {
			matrix.setRotate(-90);
		} else {
			matrix.setRotate(90);
		}
		tmpBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(),
				tmpBitmap.getHeight(), matrix, true);
		tmpBitmap = tmpBitmap.copy(Bitmap.Config.ARGB_8888, true);

		int hight = tmpBitmap.getHeight() > tmpBitmap.getWidth() ? tmpBitmap
				.getHeight() : tmpBitmap.getWidth();

		float scale = hight / 800.0f;

		if (scale > 1) {
			tmpBitmap = Bitmap.createScaledBitmap(tmpBitmap,
					(int) (tmpBitmap.getWidth() / scale),
					(int) (tmpBitmap.getHeight() / scale), false);
		}
		return tmpBitmap;
	}

	/**
	 * 获取照相机旋转角度
	 */
	public int getCameraAngle(Activity activity) {
		int rotateAngle = 90;
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			rotateAngle = (info.orientation + degrees) % 360;
			rotateAngle = (360 - rotateAngle) % 360; // compensate the mirror
		} else { // back-facing
			rotateAngle = (info.orientation - degrees + 360) % 360;
		}
		return rotateAngle;
	}
}