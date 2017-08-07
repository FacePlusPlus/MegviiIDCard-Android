# MegviiIDCard-Android
Android sdk and demo for IdCard.

## How to use
1. Add the JitPack repository to your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
2. Add the dependency
```
dependencies {
        compile 'com.github.FacePlusPlus:MegviiIDCard-Android:0.3.0'
}
```
3. Go to the [official website](https://www.faceplusplus.com.cn/) register an account
4. Apply key and secret from [here](https://console.faceplusplus.com.cn/app/apikey/list), create a file named "key" in assets directory, paste key and secret in format like key;secret
5. Bund your bundle id from [here](https://console.faceplusplus.com.cn/app/bundle/list)
6. Download sdk from [here](https://console.faceplusplus.com.cn/service/card/intro), find the model 
that named with megviiidcard_0_3_0_model and put it in raw directory
7. Run demo or your app.
