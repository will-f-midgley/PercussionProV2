//
// Created by Kuba on 12/02/2025.
//


#include <oboe/Oboe.h>

#include "Log.h"
#include "include/AudioHandler.h"
#include "percussionapp.h"

namespace percussionapp {

    //on start, get all Kotlin method IDs and store the vm object for later use
    void AudioHandler::start(JavaVM* _vm, jobject obj) {

        JNIEnv *env;
        vm = _vm;
        kotlinAudioEngine = obj;

        _isRunning = true;

        auto result = vm->GetEnv((void**)&env, JNI_VERSION_1_6);
        if (result == JNI_EDETACHED) {
            if (vm->AttachCurrentThread(&env, NULL) == JNI_OK) {
                LOG("ATTACHED");
            } else {
                LOG("DETACHED");
            }
        } else if (result == JNI_EVERSION) {
            LOG("UNSUPPORTED VERSION");
        } else if (result == JNI_OK){
            LOG("JNI OK");
        }

        engineClass = env->GetObjectClass(kotlinAudioEngine);
        if (env->ExceptionCheck()) {
            LOG("clazz ERROR!");
            return;
        }

        onSoundID = env->GetStaticMethodID(engineClass, "onSound", "(I)V");
        if (env->ExceptionCheck()) {
            LOG("methodID ERROR!");
            return;
        }
        showWaveID = env->GetStaticMethodID(engineClass, "showWave", "([D)V");
        if (env->ExceptionCheck()) {
            LOG("methodID ERROR!");
            return;
        }
        newBarID = env->GetStaticMethodID(engineClass, "newBar", "(I)V");
        if (env->ExceptionCheck()) {
            LOG("methodID ERROR!");
            return;
        }
    }

    void AudioHandler::stop() {
        LOG("stop() called");
        _isRunning = false;
    };

    bool AudioHandler::isRunning() const {
        return _isRunning;
    }

    void AudioHandler::onSound(int timeCode) {

        JNIEnv *env;

        auto result = vm->GetEnv((void**)&env, JNI_VERSION_1_6);
        if (result == JNI_EDETACHED) {
            if (vm->AttachCurrentThread(&env, NULL) != JNI_OK) {
                LOG("DETACHED");
            }
        } else if (result == JNI_EVERSION) {
            LOG("UNSUPPORTED VERSION");
        }

        jclass target = env->GetObjectClass(kotlinAudioEngine);
        if (env->ExceptionCheck()) {
            LOG("clazz ERROR!");
            return;
        }
        if (onSoundID == NULL) {
            LOG("METHOD ID IS NULL");
        } else {
            env->CallStaticVoidMethod(target, onSoundID, timeCode);
            if (env->ExceptionCheck()) {
                LOG("call method ERROR!");
                return;
            }
        }
    }

    void AudioHandler::onFrequencyUpdate(double * spectrum) {

        double newSpectrum[FFT_WINDOWS];
        std::copy(spectrum, spectrum + FFT_WINDOWS, newSpectrum);
        JNIEnv *env;

        auto result = vm->GetEnv((void**)&env, JNI_VERSION_1_6);
        //LOG("STEP 1");
        if (result == JNI_EDETACHED) {
            if (vm->AttachCurrentThread(&env, NULL) != JNI_OK) {
                LOG("DETACHED");
            }
        } else if (result == JNI_EVERSION) {
            LOG("UNSUPPORTED VERSION");
        }

        jclass target = env->GetObjectClass(kotlinAudioEngine);
        if (env->ExceptionCheck()) {
            LOG("clazz ERROR!");
            return;
        }

        if (showWaveID == NULL) {
            LOG("STEP 3 FAILED");
            LOG("METHOD ID IS NULL");
        } else {
            jdoubleArray convWaveform = env->NewDoubleArray(FFT_WINDOWS);
            env->SetDoubleArrayRegion(convWaveform, 0, FFT_WINDOWS, reinterpret_cast<jdouble*>(newSpectrum));

            /*
            jclass doubleClass = env->FindClass("java/lang/Double");
            jmethodID doubleConstructor = env->GetMethodID(doubleClass, "<init>", "(D)V");

            jobject defaultDouble = env->NewObject(doubleClass,doubleConstructor,0.0);

            jobjectArray convertedWaveform = env->NewObjectArray(FFT_WINDOWS,
                                        doubleClass,defaultDouble);
            for(int i = 0; i < FFT_WINDOWS; i++){
                jobject newDouble = env->NewObject(doubleClass,doubleConstructor,newSpectrum[i]);
                env->SetObjectArrayElement(convertedWaveform,i,newDouble);
            }

             */
            env->CallStaticVoidMethod(target, showWaveID, convWaveform);
            if (env->ExceptionCheck()) {
                LOG("call method ERROR!");
                return;
            }
        }
    }

    void AudioHandler::onNewBar(int bar) {
        JNIEnv *env;

        auto result = vm->GetEnv((void**)&env, JNI_VERSION_1_6);
        //LOG("STEP 1");
        if (result == JNI_EDETACHED) {
            if (vm->AttachCurrentThread(&env, NULL) == JNI_OK) {
                LOG("ATTACHED");
            } else {
                LOG("DETACHED");
            }
        } else if (result == JNI_EVERSION) {
            LOG("UNSUPPORTED VERSION");
        } else if (result == JNI_OK){
            //LOG("NOTHING FOUND!?!?");
        }

        jclass target = env->GetObjectClass(kotlinAudioEngine);
        if (env->ExceptionCheck()) {
            LOG("clazz ERROR!");
            return;
        }

        if (newBarID == nullptr) {
            LOG("STEP 3 FAILED");
            LOG("METHOD ID IS NULL");
        } else {
            env->CallStaticVoidMethod(target, newBarID,bar);
            if (env->ExceptionCheck()) {
                LOG("call method ERROR!");
                return;
            }
        }
    }

}