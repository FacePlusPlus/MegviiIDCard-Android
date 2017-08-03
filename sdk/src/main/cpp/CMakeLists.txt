cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_AUTOMOC ON)
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_INCLUDE_CURRENT_DIR ON)


include_directories(${CMAKE_SOURCE_DIR}/include)
message (${CMAKE_SOURCE_DIR})
set(jnilibs "${CMAKE_SOURCE_DIR}/../jniLibs")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${jnilibs}/${ANDROID_ABI})

add_library(IDCardWrapper
             SHARED
             com_megvii_idcard_Api.cpp)

add_library(idcard SHARED IMPORTED )
set_target_properties(idcard PROPERTIES
    IMPORTED_LOCATION "${jnilibs}/${ANDROID_ABI}/libMegviiIDCard-0.3.0.so")

find_library( # Sets the name of the path variable.
              log-lib
              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries(IDCardWrapper idcard ${log-lib})

