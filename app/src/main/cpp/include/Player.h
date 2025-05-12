//
// Created by Kuba on 05/03/2025.
//

#ifndef PERCUSSIONAPP_PLAYER_H
#define PERCUSSIONAPP_PLAYER_H

#include <oboe/Oboe.h>
#include <android/asset_manager_jni.h>

#include <utility>
#include "Sample.h"
#include "AudioHandler.h"
#include "AudioStreamWrapper.h"

namespace percussionapp {

    const int SAMPLE_RATE = 48000;

    class Player : public AudioStreamWrapper, oboe::AudioStreamDataCallback {

    public:
        explicit Player(std::shared_ptr<Sample> claveSample) {
            clave = std::move(claveSample);
        }

        bool start(std::shared_ptr<AudioHandler> audioSink) override;

        bool stop() override;

        void changeStyle(int style);

        bool isPlaying() const { return _isPlaying; }

        void startPlaying() { _isPlaying = true; }

        void stopPlaying() { _isPlaying = false; }

        int checkNoteOnTime(int type, long long noteTime);

        void changeDifficulty(int newDifficulty);

        void changeLatency(int newLatency) { latency = newLatency; }

        void changeTempo(int newTempo) {
            BPM = (float) newTempo;
            beatLengthMilliseconds = (60.0f) * 1000 / BPM;
        }

        void toggleMetronome(bool metronomeOn);

    private:
        oboe::DataCallbackResult
        onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) override;

        //bool metronomePlayingInsteadOfClave = false;
        bool stereo = false;

        void updateCurrentTime();

        void updateCurrentPatternEvent();

        void updateCurrentBar();

        std::shared_ptr<Sample> clave;

        bool _isPlaying = false;
        bool ready = false;
        int style = 0; //default is tumbao
        long long currentTimeMs = 0;
        long long patternStartMs = 0;
        std::vector<std::array<float, 2>> currentPattern;
        //0 = tumbao
        //1 = mozambique
        //2 = guaguanco
        //3 = merengue
        int currentBackingEventIndex = 0;


        float BPM = 120;

        float beatLengthMilliseconds = (60.0f) * 1000 / BPM; //500 ms for 120bpm
        float barLengthBeats = 4;
        float barLengthMilliseconds = beatLengthMilliseconds * 4; //2000ms for 120bpm
        int clavePatternLengthBeats = barLengthBeats * 2;

        std::vector<float> backingEventsBeats;

        int currentPatternEvent = 0;
        int currentBar = 1;
        int difficulty = 1;

        int latency = 150;
        int earlyOffsetMs = 60;
        int lateOffsetMs = earlyOffsetMs;
        int missOffsetMs = 100;

        float nextMissMs = lateOffsetMs;

        float beatsToMillis(float beats) const;
    };

} // percussionapp

#endif //PERCUSSIONAPP_PLAYER_H
