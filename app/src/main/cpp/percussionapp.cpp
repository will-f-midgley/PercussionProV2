
#include <jni.h>
#include <memory>
#include "Log.h"
#include "AudioHandler.h"
#include "AudioDetector.h"
#include "Player.h"
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>
#include "libs/AudioFile.h"
#include <vector>

std::shared_ptr<percussionapp::AudioHandler> handler;
std::unique_ptr<percussionapp::AudioDetector> detector;
std::shared_ptr<percussionapp::Player> player ;
std::shared_ptr<percussionapp::Sample> claveSample;
const char* clavePath = "clave_mono.wav";

void startHandler(JNIEnv *pEnv, jobject kotlinEngine);
void stopHandler();

extern "C" {
JNIEXPORT jboolean JNICALL

//create pointers for all objects- handler, detector, player and samples
Java_com_example_percussionapp_KotlinAudioEngine_JNICreate(JNIEnv *env, jobject kotlinEngine, jobject kotlinAssetManager) {

    //get audio assets from asset manager
    AAssetManager *assetManager = AAssetManager_fromJava(env, kotlinAssetManager);
    if (assetManager == nullptr) {
        LOG("AssetManager not initialized");
        return false;
    }

    handler = std::make_shared<percussionapp::AudioHandler>();
    if (not handler) {
        LOG("Failed to create audio handler");
        handler.reset();
        return false;
    }

    const char* samplePath = clavePath;

    AAsset *asset = AAssetManager_open(assetManager, samplePath, AASSET_MODE_BUFFER);
    if (!asset) {
        LOG("Failed to open asset %s", samplePath);
    }

    size_t sampleLength = AAsset_getLength(asset);
    auto* data = new uint8_t[sampleLength];
    int bytesRead = AAsset_read(asset,data,sampleLength);
    if (bytesRead != sampleLength){
        LOG("Error: read %d bytes from %s but %d expected",bytesRead,samplePath,sampleLength);
    }
    else {
        LOG("Opened %s, size %zu", samplePath, sampleLength);
    }

    std::vector<uint8_t> dataVector(data, data + sampleLength);
    delete[] data;
    claveSample = std::make_shared<percussionapp::Sample>(dataVector);
    player = std::make_shared<percussionapp::Player>(claveSample);

    if (not player) {
        LOG("Failed to create player");
        player.reset();
        return false;
    }

    detector = std::make_unique<percussionapp::AudioDetector>();
    if (not detector) {
        LOG("Failed to create detector");
       detector.reset(nullptr);
       return false;
    } else{

        return detector->generatePlan();
    }

    LOG("Successfully created handler, record & player objects");
    return true;
    //return reinterpret_cast<jlong>(handler.release());
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIDelete(JNIEnv *env, jobject kotlinEngine) {

    if (not detector){
        LOG("Attempted to reset uninitialised detector");
        return false;
    }
    //now we ACTUALLY stop the stream
    detector->stop();

    detector.reset(nullptr);
    LOG("Reset detector pointer");


    if (not player){
        LOG("Attempted to reset uninitialised player");
        return false;
    }
    player->stop();

    player.reset();
    LOG("Reset player pointer");


    if (not handler){
        LOG("Attempted to reset uninitialised audio handler");
        return false;
    }
    stopHandler();
    handler.reset();
    LOG("Reset audio handler pointer");

    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIStartPlayer(JNIEnv *env, jobject kotlinEngine, jint style) {

    //handler needs to be running to pass events to
    startHandler(env, kotlinEngine);
    if (player){
        if (!player->isRunning()) {
            player->changeStyle((int) style);
            if (!player->start(handler)) {
                LOG("couldn't start player...");
                return false;
            }
        } else if (!player->isPlaying()){
            player->startPlaying();
        }

    }else{
        LOG("Player not created");
        return false;
    }
    LOG("Player started");
    return true;
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIStartDetector(JNIEnv *env, jobject kotlinEngine) {

    startHandler(env, kotlinEngine);
    if (detector){
        if(!detector->isRunning()){
            detector->generatePlan();
            detector->start(handler);
            if (player){
                detector->assignTimer(player);
            } else{
                LOG("Player not created, can't pass values to Detector");
            }
        } else if(!detector->isDetecting()){
            detector->startDetecting();
        }
    }else{
        LOG("AudioDetector not created");
        return false;
    }
    LOG("Detector started");
    return true;
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIStopPlayer(JNIEnv *env, jobject kotlinEngine) {

    //for some reason, crashes can occur when reopening input streams - so we don't fully close the stream, just stop processing the input
    player->stopPlaying();

    if (detector && !detector->isDetecting()){
        stopHandler();
    }
    LOG("Stopped player");
    return true;
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIStopDetector(JNIEnv *env, jobject kotlinEngine) {

    //for some reason, crashes can occur when reopening input streams - so we don't fully close the stream, just stop processing the input
    detector->stopDetecting();

    if (player && !player->isPlaying()){
        stopHandler();
    }

    LOG("Stopped detector");
    return true;
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIIsRunning(JNIEnv *env, jobject kotlinEngine) {

    if (handler){
        return handler->isRunning();
    } else{
        LOG("Audiohandler not created");
        return false;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIIsPlaying(JNIEnv *env, jobject thiz) {
    if (player) {
        return player->isPlaying();
    } else {
        LOG("Player not created");
        return false;
    }
}
JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIIsRecording(JNIEnv *env, jobject thiz) {
    if (detector) {
        return detector->isDetecting();
    } else {
        LOG("Detector not created");
        return false;
    }
}

//DEBUG FUNCTION - CAN BE USED FOR SENDING NOTE INFO FROM KOTLIN
JNIEXPORT jint JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNISendNote(JNIEnv *env, jobject thiz, jint type) {
    if (player){
        if (player->isRunning()){
            long long currTime = duration_cast<std::chrono::milliseconds>
                    (std::chrono::high_resolution_clock::now().time_since_epoch()).count();
            int timed = player->checkNoteOnTime(type, currTime);
            handler->onSound(timed);
            return 0;
        }
    }
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIChangeDifficulty(JNIEnv *env, jobject thiz,
                                                                     jint difficulty) {
    if (player){
        player->changeDifficulty(difficulty);
        return true;
    }
    return false;
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIChangeLatency(JNIEnv *env, jobject thiz,
                                                                  jint latency) {
    if (player){
        player->changeLatency(latency);
        return true;
    }
    return false;
}


JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIToggleMetronome(JNIEnv *env, jobject thiz,
                                                                    jboolean metronome) {
    if (player){
        player->toggleMetronome(metronome);
        return true;
    }
    return false;
}

JNIEXPORT jboolean JNICALL
Java_com_example_percussionapp_KotlinAudioEngine_JNIChangeTempo(JNIEnv *env, jobject thiz,
                                                                jint tempo) {
    if (player){
        player->changeTempo(tempo);
        return true;
    }
    return false;
}

}

void startHandler(JNIEnv *env, jobject kotlinEngine){
    if (handler) {
        if (!handler->isRunning()) {
            JavaVM *vm;
            env->GetJavaVM(&vm);
            jobject engineRef = env->NewGlobalRef(kotlinEngine);

            //pass the VM so the handler can make callbacks to kotlin when sound detected
            handler->start(vm, engineRef);
            LOG("Handler started");
        } else {
            LOG("Handler already running");
        }
    } else{
        LOG("Audiohandler not created");
    }
}

void stopHandler(){
    if (handler){
        handler->stop();
        LOG("Handler stopped");
    } else{
        LOG("Audiohandler not created");
    }
}
