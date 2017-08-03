#include <jni.h>

#ifndef _Included_com_megvii_idcard_Api
#define _Included_com_megvii_idcard_Api
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeInit(
		JNIEnv *, jobject, jobject, jbyteArray);

JNIEXPORT jfloatArray JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetIDCardConfig(
		JNIEnv *, jobject, jlong);

JNIEXPORT jint JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeSetIDCardConfig(
		JNIEnv *, jobject, jlong, jint, jfloat, jfloat, jfloat, jint, jint, jint,
		jint, jint, jint, jint, jint);

JNIEXPORT jfloatArray JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeDetect(
		JNIEnv *, jobject, jlong, jbyteArray, jint, jint, jint);

JNIEXPORT jfloatArray JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeCalculateQuality(
		JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeRelease
(JNIEnv *, jobject, jlong);

JNIEXPORT jlong JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetApiExpication(
		JNIEnv *, jobject, jobject);

JNIEXPORT jstring JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetVersion(
		JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_com_megvii_idcard_sdk_jni_IDCardApi_nativeGetApiName(
		JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
