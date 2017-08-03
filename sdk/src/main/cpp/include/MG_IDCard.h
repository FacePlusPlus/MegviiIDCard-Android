#ifndef _MG_IDCARD_H_
#define _MG_IDCARD_H_

#include "MG_Common.h"

#if __cplusplus
extern "C" {
#endif

#define _OUT

typedef struct {
    MG_POINT* vertex;
    MG_INT32 size;
} MG_IDC_POLYGON;

typedef struct{
    MG_IDC_POLYGON* polygon;
    MG_INT32 size;
} MG_IDC_POLYGONS;

typedef struct{
    MG_SINGLE in_bound;
    MG_SINGLE is_idcard;
    MG_SINGLE clear;
    MG_SINGLE cos;
} MG_IDC_CONFIDENCE;

typedef struct{
    MG_IDC_POLYGONS shadow;
    MG_IDC_POLYGONS faculae;
    MG_IDC_POLYGON idcard ;
} MG_IDC_QUALITY;


typedef struct{
    MG_RECTANGLE roi;
    MG_INT32 orientation;

    MG_SINGLE shadow_th;
    MG_SINGLE shadow_area_th;

    MG_SINGLE facula_th;
    MG_SINGLE facula_area_th;

    MG_SINGLE card_th;
    MG_SINGLE card_area_th;

    MG_INT32 shadow_confirm_th; //80
    MG_INT32 facula_activated_th; // 210
    MG_INT32 facula_confirm_th; // 220
} MG_IDC_APICONFIG;
    
    typedef struct{
        MG_BYTE* img_data;
        MG_INT32 img_width;
        MG_INT32 img_height;
        MG_INT32 img_channels;
    } MG_IDC_HEATMAP_IMAGE;



struct _MG_IDC_API;
typedef struct _MG_IDC_API* MG_IDC_APIHANDLE;

struct _MG_IDC_IMAGE;
typedef struct _MG_IDC_IMAGE* MG_IDC_IMAGEHANDLE;

typedef struct{
#if MGAPI_BUILD_ON_ANDROID
    MG_RETCODE (*CreateApiHandle)(JNIEnv*,jobject, const MG_BYTE* model_data, MG_INT32 model_length, MG_IDC_APIHANDLE _OUT *api_handle);
#else
    MG_RETCODE (*CreateApiHandle)(const MG_BYTE* model_data, MG_INT32 model_length, MG_IDC_APIHANDLE _OUT *api_handle);
#endif
    MG_RETCODE (*ReleaseApiHandle)(MG_IDC_APIHANDLE api_handle);

    const char* (*GetApiVersion)();

#if MGAPI_BUILD_ON_ANDROID
    MG_UINT64 (*GetApiExpiration)(JNIEnv*, jobject);
#else
    MG_UINT64 (*GetApiExpiration)();
#endif

    MG_RETCODE (*GetAPIConfig)(MG_IDC_APIHANDLE api_handle, MG_IDC_APICONFIG _OUT *api_config);
    MG_RETCODE (*SetAPIConfig)(MG_IDC_APIHANDLE api_handle, const MG_IDC_APICONFIG  *api_config);

    MG_RETCODE (*Detect)(MG_IDC_APIHANDLE api_handle, MG_IDC_IMAGEHANDLE image_handle, MG_IDC_CONFIDENCE _OUT *confidence);
    MG_RETCODE (*CalculateQuality)(MG_IDC_APIHANDLE api_handle, MG_IDC_IMAGEHANDLE image_handle, MG_BOOL need_filter,
                                   MG_IDC_QUALITY _OUT ** quality, MG_IDC_HEATMAP_IMAGE* o_shadow_heatmap, MG_IDC_HEATMAP_IMAGE* o_flare_heatmap, MG_IDC_HEATMAP_IMAGE* o_idcard_heatmap);
    MG_RETCODE (*ReleaseQuality) (MG_IDC_QUALITY *quality);

	// Create an image handle
	// can't modify image size anymore
	MG_RETCODE (*CreateImageHandle)
		(MG_INT32 width, MG_INT32 height, MG_IDC_IMAGEHANDLE _OUT *image_handle_ptr);

	// Set raw data into image handle
	// you can set raw data many times
	MG_RETCODE (*SetImageData)
		(MG_IDC_IMAGEHANDLE image_handle, const MG_BYTE *image_data, MG_IMAGEMODE image_mode);
	// Release image hanlde
 	MG_RETCODE (*ReleaseImageHandle)
 		(MG_IDC_IMAGEHANDLE image_Handle);
    
    MG_SDKAUTHTYPE (*GetSDKAuthType)();
}MG_IDC_API_FUNCTIONS_TYPE;

#undef _OUT

MG_EXPORT extern MG_IDC_API_FUNCTIONS_TYPE mg_idcard;

#if __cplusplus
}
#endif

#endif //_MG_IDCARD_H_
