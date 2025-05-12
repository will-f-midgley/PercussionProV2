//
// Created by Kuba on 06/03/2025.
//

#ifndef PERCUSSIONAPP_AUDIODETECTOR_H
#define PERCUSSIONAPP_AUDIODETECTOR_H

#include <fstream>
//#include <queue>
#include <oboe/Oboe.h>
#include "AudioHandler.h"
#include <fftw3.h>
#include "Player.h"

namespace percussionapp {

    class AudioDetector
            : public AudioStreamWrapper,
              public oboe::AudioStreamDataCallback,
              public oboe::AudioStreamErrorCallback {

    public:
        bool start(std::shared_ptr<AudioHandler>) override;

        bool stop() override;

        bool generatePlan();

        bool isTiming() const { return _isTiming; }

        void assignTimer(std::shared_ptr<Player>);

        void startDetecting() { _isDetecting = true; }

        void stopDetecting() { _isDetecting = false; }

        bool isDetecting() { return _isDetecting; }

    private:

        oboe::DataCallbackResult
        onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) override;

        bool onError(oboe::AudioStream *audioStream, oboe::Result r) override;

        int sampleRate = 44100;
        float prevRMSAmplitude = 0;

        bool _isDetecting = false;
        bool _isTiming = false;
        bool _isRunning = false;

        std::shared_ptr<Player> timer;
        std::shared_ptr<AudioHandler> audioHandler;

        fftw_plan plan;
        double *in;
        fftw_complex *out;
        int fftInputIndex = 0;

        //smallest peak is about 0.12 in float
        float minPeakDifference = 0.12f;
        //number of milliseconds before a second note can be detected
        //adjusted from 20 to 50, playing at 20hz is still impossible
        int noteCooldownMilliseconds = 50;
        long long lastPlayedNoteTimeMilliseconds = 0;
    };
} // percussionapp

#endif //PERCUSSIONAPP_AUDIODETECTOR_H
