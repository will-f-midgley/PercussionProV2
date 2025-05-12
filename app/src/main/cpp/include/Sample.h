//
// Created by Kuba on 13/03/2025.
//

#ifndef PERCUSSIONAPP_SAMPLE_H
#define PERCUSSIONAPP_SAMPLE_H

#include <cstdint>
#include <memory>
#include <Log.h>
#include "libs/AudioFile.h"
#include <vector>

namespace percussionapp {

    class Sample {
    public:

        explicit Sample(const std::vector<uint8_t> &dataVector) {
            AudioFile<int16_t> audioFile;
            file.loadFromMemory(dataVector);
            stereo = file.isStereo();
            LOG("Sample created");
            sampleSize = file.getNumSamplesPerChannel();
            printSampleInfo();
        }

        void sendCurrentSamplesToBuff(int16_t *outputBuffer, int numFrames, bool stereoStream);

        bool isPlaying() const;

        void play();

        void printSampleInfo();

    private:
        bool stereo;
        bool _isPlaying = false;
        int32_t currentFrame = 0;
        AudioFile<int16_t> file;
        size_t sampleSize;
    };

} // percussionapp

#endif //PERCUSSIONAPP_SAMPLE_H
