#include "MG_IDCard.h"
#include "com_megvii_idcard_Api.h"
#include <android/log.h>
#include <jni.h>

#include <vector>
#include <algorithm>
#include <string>
#include <chrono>
#include <cmath>

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"mgf-c",__VA_ARGS__)

class Timer {
	std::chrono::system_clock::time_point start;
	std::string name;

public:
	Timer(std::string name) :
			name(name) {
		start = std::chrono::system_clock::now();
	}

	~Timer() {
		std::chrono::system_clock::time_point end =
				std::chrono::system_clock::now();
		LOGE("%s, used time = %lld\n", name.c_str(),
				(std::chrono::duration_cast < std::chrono::microseconds
						> (end - start)).count());
	}
};

#define DETECTION_TRACKING 1
#define DETECTION_NORMAL 0
#define LANDMARK_ST_NR 106
#define ALPHA 0.7
#define BETA 0.3

#define IMAGEMODE_GRAY 0
#define IMAGEMODE_BGR 1
#define IMAGEMODE_NV21 2
#define IMAGEMODE_RGBA 3

struct ApiHandle {
	MG_IDC_APIHANDLE api;
	MG_IDC_IMAGEHANDLE imghandle;
	MG_IDC_QUALITY *quality;

	int w, h, orientation, need_filter;
};

jlong Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeInit(JNIEnv *env, jobject,
		jobject context, jbyteArray model) {

	jbyte* model_data = env->GetByteArrayElements(model, 0);
	long model_len = env->GetArrayLength(model);

	ApiHandle *h = new ApiHandle();
	int retcode = mg_idcard.CreateApiHandle(env, context,
			reinterpret_cast<const MG_BYTE*>(model_data), model_len, &h->api);
	if (retcode != 0) {
		return retcode;
	}

	h->imghandle = nullptr;
	h->quality = nullptr;
	h->w = h->h = 0;

	env->ReleaseByteArrayElements(model, model_data, 0);
	return reinterpret_cast<jlong>(h);
}

jfloatArray Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetIDCardConfig(
		JNIEnv *env, jobject, jlong handle) {
	//LOGE("nativeGetIDCardConfig: %d", 0);
	ApiHandle *h = reinterpret_cast<ApiHandle*>(handle);

	jfloatArray retArray = env->NewFloatArray(11);
	//set config
	MG_IDC_APICONFIG config;
	mg_idcard.GetAPIConfig(h->api, &config);
	float orientation = config.orientation;
	float shadow_area_th = config.shadow_area_th;
	float facula_area_th = config.facula_area_th;
	float card_area_th = config.card_area_th;
	float shadow_confirm_th = config.shadow_confirm_th;
	float facula_activated_th = config.facula_activated_th;
	float facula_confirm_th = config.facula_confirm_th;
	float left = config.roi.left;
	float top = config.roi.top;
	float right = config.roi.right;
	float bottom = config.roi.bottom;
	env->SetFloatArrayRegion(retArray, 0, 1, &orientation);
	env->SetFloatArrayRegion(retArray, 1, 1, &shadow_area_th);
	env->SetFloatArrayRegion(retArray, 2, 1, &facula_area_th);
	env->SetFloatArrayRegion(retArray, 3, 1, &card_area_th);
	env->SetFloatArrayRegion(retArray, 4, 1, &shadow_confirm_th);
	env->SetFloatArrayRegion(retArray, 5, 1, &facula_activated_th);
	env->SetFloatArrayRegion(retArray, 6, 1, &facula_confirm_th);

	env->SetFloatArrayRegion(retArray, 7, 1, &left);
	env->SetFloatArrayRegion(retArray, 8, 1, &top);
	env->SetFloatArrayRegion(retArray, 9, 1, &right);
	env->SetFloatArrayRegion(retArray, 10, 1, &bottom);
	return retArray;
}

jint Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeSetIDCardConfig(JNIEnv *,
		jobject, jlong handle, jint orientation, jfloat shadow_area_th,
		jfloat facula_area_th, jfloat card_area_th, jint shadow_confirm_th,
		jint facula_activated_th, jint facula_confirm_th, jint left, jint top,
		jint right, jint bottom, jint need_filter) {
	ApiHandle *h = reinterpret_cast<ApiHandle*>(handle);
	h->orientation = orientation;
	h->need_filter = need_filter;
	MG_IDC_APICONFIG config;
	mg_idcard.GetAPIConfig(h->api, &config);
	config.orientation = orientation;
	config.shadow_area_th = shadow_area_th;
	config.facula_area_th = facula_area_th;
	config.card_area_th = card_area_th;
	config.shadow_confirm_th = shadow_confirm_th;
	config.facula_activated_th = facula_activated_th;
	config.facula_confirm_th = facula_confirm_th;
	MG_RECTANGLE _roi;
	_roi.left = left;
	_roi.top = top;
	_roi.right = right;
	_roi.bottom = bottom;
	config.roi = _roi;
	int retcode = mg_idcard.SetAPIConfig(h->api, &config);
	return retcode;
}

jfloatArray Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeDetect(JNIEnv *env,
		jobject, jlong handle, jbyteArray img, jint width, jint height,
		jint imageMode) {
	ApiHandle *h = reinterpret_cast<ApiHandle*>(handle);
	jbyte* img_data = env->GetByteArrayElements(img, 0);
	LOGE("nativeDetect img_data length: %d", env->GetArrayLength(img));

	if (h->imghandle != nullptr && (h->w != width || h->h != height)) {
		mg_idcard.ReleaseImageHandle(h->imghandle);
		h->imghandle = nullptr;
	}
	if (h->imghandle == nullptr) {
		mg_idcard.CreateImageHandle(width, height, &h->imghandle);
		h->w = width;
		h->h = height;
	}

	MG_IDC_IMAGEHANDLE imageHandle = h->imghandle;

	if (imageMode == IMAGEMODE_GRAY) {
		mg_idcard.SetImageData(imageHandle, (unsigned char*) img_data,
				MG_IMAGEMODE_GRAY);
	} else if (imageMode == IMAGEMODE_BGR) {
		mg_idcard.SetImageData(imageHandle, (unsigned char*) img_data,
				MG_IMAGEMODE_BGR);
	} else if (imageMode == IMAGEMODE_NV21) {
		mg_idcard.SetImageData(imageHandle, (unsigned char*) img_data,
				MG_IMAGEMODE_NV21);
	} else if (imageMode == IMAGEMODE_RGBA) {
		mg_idcard.SetImageData(imageHandle, (unsigned char*) img_data,
				MG_IMAGEMODE_RGBA);
	}
	LOGE("nativeDetect mg_idcard.Detect: %d", 111);
	MG_IDC_CONFIDENCE confidence;
	mg_idcard.Detect(h->api, imageHandle, &confidence);

	jfloatArray floatArray = env->NewFloatArray(3);

	env->SetFloatArrayRegion(floatArray, 0, 1, &confidence.in_bound);
	env->SetFloatArrayRegion(floatArray, 1, 1, &confidence.is_idcard);
	env->SetFloatArrayRegion(floatArray, 2, 1, &confidence.clear);

	env->ReleaseByteArrayElements(img, img_data, 0); //release javabytearray
	return floatArray;
}

jfloatArray Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeCalculateQuality(
		JNIEnv *env, jobject, jlong handle) {
	ApiHandle *h = reinterpret_cast<ApiHandle*>(handle);

	if (h->imghandle == nullptr) {
		return nullptr;
	}

	MG_IDC_IMAGEHANDLE imageHandle = h->imghandle;
//	{
//		Timer _("mg_idcard.CalculateQuality");
	LOGE("CalculateQuality h->need_filter = %d", h->need_filter);
	mg_idcard.CalculateQuality(h->api, imageHandle, h->need_filter, &h->quality, nullptr, nullptr, nullptr);
//	}
	LOGE("CalculateQuality h->quality = %p", h->quality);

	MG_IDC_POLYGONS shadows = h->quality->shadow;
	MG_IDC_POLYGONS faculae = h->quality->faculae;
	MG_IDC_POLYGON card = h->quality->idcard;

	std::vector<float> result;

	//LOGE("shadows.size = %d", shadows.size);
	result.push_back(shadows.size);
	for (int i = 0; i < shadows.size; ++i) {
		for (int a = 0; a < 3; ++a) {
//			result.push_back(shadows.polygon[i].average[a]);
			result.push_back(0);
		}
		for (int a = 0; a < 3; ++a) {
//			result.push_back(shadows.polygon[i].variance[a]);
			result.push_back(0);
		}
		result.push_back(shadows.polygon[i].size);
		for (int j = 0; j < shadows.polygon[i].size; ++j) {
			result.push_back(shadows.polygon[i].vertex[j].x);
			result.push_back(shadows.polygon[i].vertex[j].y);
		}
	}

	//LOGE("faculae.size = %d", faculae.size);
	result.push_back(faculae.size);
	for (int i = 0; i < faculae.size; ++i) {
		for (int a = 0; a < 3; ++a) {
			result.push_back(0);
			//result.push_back(faculae.polygon[i].average[a]);
		}
		for (int a = 0; a < 3; ++a) {
			result.push_back(0);
			//result.push_back(faculae.polygon[i].variance[a]);
		}
		result.push_back(faculae.polygon[i].size);
		for (int j = 0; j < faculae.polygon[i].size; ++j) {
			result.push_back(faculae.polygon[i].vertex[j].x);
			result.push_back(faculae.polygon[i].vertex[j].y);
		}
	}

	result.push_back(1);
	for (int a = 0; a < 3; ++a) {
		result.push_back(0);
		//result.push_back(card.average[a]);
	}
	for (int a = 0; a < 3; ++a) {
		result.push_back(0);
		//result.push_back(card.variance[a]);
	}
	result.push_back(card.size);
	for (int j = 0; j < card.size; ++j) {
		result.push_back(card.vertex[j].x);
		result.push_back(card.vertex[j].y);
	}

	jfloatArray floatArray = env->NewFloatArray(result.size());
	env->SetFloatArrayRegion(floatArray, 0, result.size(), result.data());

	mg_idcard.ReleaseQuality(h->quality);
	h->quality = nullptr;

	return floatArray;
}

void Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeRelease(JNIEnv *, jobject,
		jlong handle) {
	ApiHandle *h = reinterpret_cast<ApiHandle*>(handle);

	if (h->imghandle != nullptr)
		mg_idcard.ReleaseImageHandle(h->imghandle);

	mg_idcard.ReleaseApiHandle(h->api);
	delete h;
}

jlong Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetApiExpication(
		JNIEnv *env, jobject, jobject ctx) {
	return (long) mg_idcard.GetApiExpiration(env, ctx);
}

jstring Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetVersion(JNIEnv *env,
		jobject) {
	const char* version = mg_idcard.GetApiVersion();
	return env->NewStringUTF(version);
}

jlong Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetApiName(JNIEnv *,
		jobject) {
	return (jlong)(mg_idcard.GetApiVersion);
}
