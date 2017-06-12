#!/usr/bin/env bash

#set -x
#set -e

rm -rf release
./gradlew build

mkdir release
mkdir release/libs
mkdir release/demo


rm -rf ./for_eclipse/idcard_ui_library/bin
rm -rf ./for_eclipse/idcard_ui_library/gen
rm -rf ./for_eclipse/demo_project/bin
rm -rf ./for_eclipse/demo_project/gen


rm -rf ./for_eclipse/idcard_ui_library/libs/arm*
rm -rf ./for_eclipse/idcard_ui_library/libs/x86
rm -rf ./for_eclipse/idcard_ui_library/libs/*.jar
cp -r ./tmp/jni/* ./for_eclipse/idcard_ui_library/libs/
mv ./tmp/classes.jar ./for_eclipse/idcard_ui_library/libs/flashliveness.jar
rm -rf ./for_eclipse/idcard_ui_library/res/*
cp -r ./tmp/res/* ./for_eclipse/idcard_ui_library/res/
rm -rf ./for_eclipse/demo_project/assets/*
cp -r ./tmp/assets/* ./for_eclipse/demo_project/assets/
rm -rf ./for_eclipse/demo_project/src/com/megvii/flashalive/*
cp ./app/src/main/java/com/megvii/flashalive/* ./for_eclipse/demo_project/src/com/megvii/flashalive/
rm -rf ./for_eclipse/demo_project/res/*
cp -r ./app/src/main/res/* ./for_eclipse/demo_project/res/
cp -r ./for_eclipse/idcard_ui_library ./release/libs/
cp -r ./for_eclipse ./release/demo/

rm -rf ./FlashLivenessDemo/build
rm -rf ./FlashLivenessDemo/.idea

rm -rf ./FlashLivenessDemo/app/libs/flashliveness_sdk.aar
cp -rf ./release/libs/flashliveness_sdk.aar ./FlashLivenessDemo/app/libs/
rm -rf ./FlashLivenessDemo/app/src/main/java/com/megvii/flashalive/*
cp -r ./app/src/main/java/com/megvii/flashalive/* ./FlashLivenessDemo/app/src/main/java/com/megvii/flashalive/
rm -rf ./FlashLivenessDemo/src/main/res/*
cp -r ./app/src/main/res/* ./FlashLivenessDemo/app/src/main/res/
rm -rf ./FlashLivenessDemo/app/src/main/AndroidManifest.xml
cp -r ./app/src/main/AndroidManifest.xml ./FlashLivenessDemo/app/src/main/
cp -r ./FlashLivenessDemo ./release/demo

