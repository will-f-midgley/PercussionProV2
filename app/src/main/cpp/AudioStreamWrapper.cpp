//
// Created by Kuba on 24/04/2025.
//

#include "AudioStreamWrapper.h"
#include "Log.h"

namespace percussionapp {

    bool AudioStreamWrapper::start(std::shared_ptr<AudioHandler> audioSink) {
        audioHandler = std::move(audioSink);
        if (audioHandler == nullptr){
            return false;
        }
        return true;
    }

    bool AudioStreamWrapper::openAndStartStream(oboe::AudioStreamBuilder builder){
        //        std::shared_ptr<oboe::AudioStreamDataCallback> thisPlayer(static_cast<oboe::AudioStreamDataCallback*>(this));
        //        builder.setDataCallback(thisPlayer);
        oboe::Result r;
        LOG("trying to open stream...");
        r = builder.openStream(aStream);
        if (r != oboe::Result::OK) {
            LOG("Error opening aStream: %s", convertToText(r));
            return false;
        }
        AudioStreamWrapper::printStreamInfo();
        _isRunning = true;
        r = aStream->requestStart();
        if (r != oboe::Result::OK) {
            _isRunning = false;
            LOG("Error starting aStream: %s", convertToText(r));
            return false;
        }
        return true;
    }

    bool AudioStreamWrapper::stop() {
        _isRunning = false;
        if (aStream != nullptr) {
            LOG("stream is NOT null!");
            oboe::Result r = aStream->requestStop();
            if (r != oboe::Result::OK) {
                LOG("Error stopping aStream: %s", convertToText(r));
                return false;
            }
            aStream->close();
            LOG("aStream closed");
            oboe::StreamState s = aStream->getState();
            switch(s){
                case oboe::StreamState::Closed:
                    break;
                case oboe::StreamState::Closing:
                    LOG("Ok, in progress..");
                    break;
                default:
                    LOG("Error");
            }
        } else{
            LOG("stream is null?!");
        }
        LOG("Stopped stream");
        return true;
    }

    void AudioStreamWrapper::printStreamInfo(){
        int32_t framesPerCall = aStream->getFramesPerDataCallback();
        int32_t sampleRate = aStream->getSampleRate();
        oboe::AudioFormat format = aStream->getFormat();
        int32_t bufferCapacity = aStream->getBufferCapacityInFrames();
        int32_t bufferSize = aStream->getBufferSizeInFrames();
        oboe::SharingMode sharingMode = aStream->getSharingMode();
        oboe::PerformanceMode perfMode = aStream->getPerformanceMode();
        int32_t framesPerBurst = aStream->getFramesPerBurst();

        LOG("Frames per data callback: %d",framesPerCall);
        LOG("Sample rate: %d",sampleRate);
        switch(format) {
            case oboe::AudioFormat::Float:
                LOG("Format: float");
                break;
            case oboe::AudioFormat::I16:
                LOG("Format: I16");
                break;
            case oboe::AudioFormat::I24:
                LOG("Format: I24");
                break;
            case oboe::AudioFormat::I32:
                LOG("Format: I16");
                break;
            case oboe::AudioFormat::IEC61937:
                LOG("Format: IEC61937");
                break;
            case oboe::AudioFormat::Invalid:
                LOG("!!Format: Invalid");
                break;
            case oboe::AudioFormat::Unspecified:
                LOG("!!Format: unspecified");
                break;
        }
        LOG("Buffer capacity: %d",bufferCapacity);
        LOG("Buffer size: %d", bufferSize);
        if (sharingMode == oboe::SharingMode::Exclusive) {
            LOG("Exclusive mode");
        }else{
            LOG("Shared mode, may have worse latency!");
        }
        if (perfMode == oboe::PerformanceMode::LowLatency) {
            LOG("performance mode: LOW LATENCY");
        }else
        {
            LOG("ERROR: NOT LOW LATENCY!");
        }
        LOG("no. of frames read at one time: %d",framesPerBurst);
    }

} // percussionapp