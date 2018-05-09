package com.megvii.faceppidcard.util;


import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class KeyUtil {

    public static String CN_LICENSE_URL = "https://api-cn.faceplusplus.com/sdk/v3/auth";
    public static String US_LICENSE_URL = "https://api-us.faceplusplus.com/sdk/v3/auth";

    public static String API_KEY="cFWkwe3Ypq6SV_n8I1alh97z4R0jgKLV";
    public static String API_SECRET="NI2rBPEqnjDL7V054LRZbcs5AjnjVk9v";

    public static boolean isReadKey(Context context) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        try {
            inputStream = context.getAssets().open("key");
            while ((count = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, count);
            }
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String str = new String(byteArrayOutputStream.toByteArray());
        String authKey = null;
        String authScrect = null;
        try {
            String[] strs = str.split(";");
            authKey = strs[0].trim();
            authScrect = strs[1].trim();
        } catch (Exception e) {
        }
        API_KEY = authKey;
        API_SECRET = authScrect;
        if (API_KEY == null || API_SECRET == null)
            return false;

        return true;
    }


}
