//
// Created by Kuba on 12/02/2025.
//

#include <oboe/Oboe.h>
#include <mutex>
#include <fstream>
#include <jni.h>

#pragma once

namespace percussionapp {

    class AudioHandler /*: public oboe::AudioStreamDataCallback */{
    public:
        void start(JavaVM *, jobject);

        void stop();

        [[nodiscard]] bool isRunning() const;

        void onSound(int timeCode);

        void onFrequencyUpdate(double *);

        void onNewBar(int bar);

    private:
        JavaVM *vm{};
        jobject kotlinAudioEngine{};
        jclass engineClass{};
        jmethodID onSoundID{};
        jmethodID showWaveID{};
        jmethodID newBarID{};
        bool _isRunning = false;
    };
}
