
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.4.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("percussionapp")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.

include_directories(include)
add_library(${CMAKE_PROJECT_NAME} SHARED
    # List C/C++ source files with relative paths to this CMakeLists.txt.
    percussionapp.cpp AudioHandler.cpp Player.cpp AudioDetector.cpp Sample.cpp AudioStreamWrapper.cpp)


# FFTW library

set (COMPILED_FFTW3_DIR ${CMAKE_SOURCE_DIR}/fftw3-android/fftw3-android-master)

include_directories(${COMPILED_FFTW3_DIR}/jni/fftw3/api)

add_library(fftw3 STATIC IMPORTED REQUIRED)
set_target_properties(fftw3 PROPERTIES IMPORTED_LOCATION ${COMPILED_FFTW3_DIR}/obj/local/${CMAKE_ANDROID_ARCH_ABI}/libfftw3.a)


#litert
#set (LITERT_DIR F:/tensorflow-libs/litert-1.2.0)
#set (TENSORFLOW_DIR F:/tensorflow-libs)
#add_library(litert SHARED IMPORTED)
#set_target_properties(litert PROPERTIES IMPORTED_LOCATION ${LITERT_DIR}/jni/${CMAKE_ANDROID_ARCH_ABI}/libtensorflowlite_jni.so)
#include_directories(${LITERT_DIR}/headers)
#include_directories(${TENSORFLOW_DIR})


#if(NOT TARGET litert)
#    message(FATAL_ERROR "litert not found. :(")
#endif()

find_package(oboe REQUIRED CONFIG)



if(NOT TARGET fftw3)
    message(FATAL_ERROR "fftw3 not found. :(")
endif()

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
    # List libraries link to the target library
    android
    log
    oboe::oboe
    fftw3)
