//
// Created by Kuba on 06/03/2025.
//

#include "AudioDetector.h"

#include <utility>
#include "Log.h"
#include <fftw3.h>
#include "percussionapp.h"

namespace percussionapp {

    bool AudioDetector::generatePlan(){
        in = (double*) fftw_malloc(sizeof(double) * FFT_INPUT_SIZE);
        LOG("allocated in");
        out = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * FFT_WINDOWS);
        LOG("allocated out");
        // FFTW_MEASURE takes too long and causes the app to freeze - tried fixing using futures, but didn't work
        // more improvements needs to be made
        plan = fftw_plan_dft_r2c_1d(FFT_INPUT_SIZE,in,out,FFTW_ESTIMATE);
        LOG("allocated plan");
        return true;

    }

    bool AudioDetector::start(std::shared_ptr<AudioHandler> audioSink) {
        audioHandler = std::move(audioSink);
        oboe::AudioStreamBuilder builder;
        builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
        builder.setSharingMode(oboe::SharingMode::Exclusive);
        builder.setFormat(oboe::AudioFormat::Float);
        builder.setChannelCount(1);
        builder.setSampleRate(10000); //48k
        builder.setDirection(oboe::Direction::Input);
        builder.setFramesPerDataCallback(FRAMES_PER_CALLBACK);
        //builder.setCallback(reinterpret_cast<oboe::AudioStreamCallback *>(this));
        LOG("callback set..");
        //writer.createFile();
        std::shared_ptr<oboe::AudioStreamDataCallback> thisPlayer(static_cast<oboe::AudioStreamDataCallback*>(this));
        builder.setDataCallback(thisPlayer);
        std::shared_ptr<oboe::AudioStreamErrorCallback> thisErr(static_cast<oboe::AudioStreamErrorCallback*>(this));
        builder.setErrorCallback(thisErr);
        openAndStartStream(builder);

        oboe::Result r;
        r = builder.openStream(aStream);
        if (r != oboe::Result::OK) {
            LOG("Error opening aStream: %s", convertToText(r));
            return false;
        }
        AudioStreamWrapper::printStreamInfo();
        _isRunning = true;
        _isDetecting = true;
        r = aStream->requestStart();
        if (r != oboe::Result::OK) {
            _isRunning = false;
            LOG("Error starting aStream: %s", convertToText(r));
            return false;
        }
        return true;
    }

    bool AudioDetector::stop() {
        AudioStreamWrapper::stop();

        //writer.closeFile();

        fftw_destroy_plan(plan);
        fftw_free(in);
        fftw_free(out);
        _isTiming = false;

        return true;
    };

    bool AudioDetector::onError(oboe::AudioStream *a, oboe::Result r){
        switch(r){
            case oboe::Result::ErrorBase:
                LOG("ErrorBase");
                break;
            case oboe::Result::ErrorClosed:
                LOG("ErrorClosed");
                break;
            case oboe::Result::ErrorDisconnected:
                LOG("ErrorDisconnected");
                break;
            case oboe::Result::ErrorIllegalArgument:
                LOG("ErrorIllegalArgument");
                break;
            case oboe::Result::ErrorInternal:
                LOG("ErrorInternal");
                break;
            case oboe::Result::ErrorInvalidFormat:
                LOG("ErrorInvalidFormat");
                break;
            case oboe::Result::ErrorInvalidHandle:
                LOG("ErrorInvalidHandle");
                break;
            case oboe::Result::ErrorInvalidRate:
                LOG("ErrorInvalidRate");
                break;
            case oboe::Result::ErrorInvalidState:
                LOG("ErrorInvalidState");
                break;
            case oboe::Result::ErrorNoFreeHandles:
                LOG("ErrorNoFreeHandles");
                break;
            case oboe::Result::ErrorNoMemory:
                LOG("ErrorNoMemory");
                break;
            case oboe::Result::ErrorNoService:
                LOG("ErrorNoService");
                break;
            case oboe::Result::ErrorNull:
                LOG("ErrorNull");
                break;
            case oboe::Result::ErrorOutOfRange:
                LOG("ErrorOutOfRange");
                break;
            case oboe::Result::ErrorTimeout:
                LOG("ErrorErrorTimeout");
                break;
            case oboe::Result::ErrorUnavailable:
                LOG("ErrorUnavailable");
                break;
            case oboe::Result::ErrorUnimplemented:
                LOG("ErrorUnimplemented");
                break;
            case oboe::Result::ErrorWouldBlock:
                LOG("ErrorWouldBlock");
                break;
            case oboe::Result::Reserved1:
                LOG("Reserved?");
                break;
            default:
                LOG("UNKNOWN");
        }
        return false;
    }

    oboe::DataCallbackResult
    AudioDetector::onAudioReady(oboe::AudioStream *audioStream, void *audioData,
                                int32_t numFrames) {
        if (_isDetecting) {
            double currentRMSAmplitude = 0;
            const float *inputFloats = static_cast<const float *>(audioData);
            int32_t samplesPerFrame = audioStream->getChannelCount();
            int32_t samplesToProcess = samplesPerFrame * numFrames;

            int framesLeft = FFT_INPUT_SIZE - fftInputIndex;

            //check if fftw input is filled
            if (framesLeft <= numFrames) {
                //if filled, copy to array and perform fft
                std::copy(inputFloats,
                          inputFloats + framesLeft,
                          in + fftInputIndex);
                fftw_execute(plan);
                double magnitudes[FFT_WINDOWS];
                //get magnitudes of the signals
                for (int i = 0; i < FFT_WINDOWS; i++) {
                    magnitudes[i] = sqrt(pow(*(out + i)[0], 2) + pow(*(out + i)[1], 2));
                }
                audioHandler->onFrequencyUpdate(magnitudes);
                fftInputIndex = 0;

            } else {
                std::copy(inputFloats,
                          inputFloats + numFrames,
                          in + fftInputIndex);
                fftInputIndex += numFrames;
            }

            for (int32_t i = 0; i < samplesToProcess; i++) {
                currentRMSAmplitude += pow(*(inputFloats + i), 2);
            }

            //LOG("RMS %f",prevRMSAmplitude);
            currentRMSAmplitude = sqrt((currentRMSAmplitude / samplesToProcess));
            float ampDiff = currentRMSAmplitude - prevRMSAmplitude;

            if (ampDiff > minPeakDifference) {
                long long beforeTest = duration_cast<std::chrono::microseconds> (std::chrono::high_resolution_clock::now().time_since_epoch()).count();

                long long noteTime = duration_cast<std::chrono::milliseconds>
                        (std::chrono::high_resolution_clock::now().time_since_epoch()).count();

                // check if note isn't actually just the same note as the last one
                if (noteTime > lastPlayedNoteTimeMilliseconds + noteCooldownMilliseconds) {
                    lastPlayedNoteTimeMilliseconds = noteTime;
                    if (!_isTiming) {
                        //audioHandler->onSound(-3);
                    } else {
                        audioHandler->onSound(timer->checkNoteOnTime(0, noteTime));
                    }
                }
                long long afterTest = duration_cast<std::chrono::microseconds> (std::chrono::high_resolution_clock::now().time_since_epoch()).count();

                LOG("Difference = %lld",afterTest - beforeTest);

            }
            prevRMSAmplitude = currentRMSAmplitude;
        }

        if (_isRunning) {
            return oboe::DataCallbackResult::Continue;
        }else{
            LOG("recording stopped.");
            return oboe::DataCallbackResult::Stop;
        }

    }

    //if timing, need to have player object to calculate note accuracy
    void AudioDetector::assignTimer(std::shared_ptr<Player> player) {
        timer = std::move(player);
        if (timer != nullptr && timer->isRunning()){
            _isTiming = true;
        }else {
            _isTiming = false;
        }
    }
}