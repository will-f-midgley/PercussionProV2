//
// Created by Kuba on 13/03/2025.
//

#include "Sample.h"
#include "Log.h"

namespace percussionapp {
    void Sample::sendCurrentSamplesToBuff(int16_t *outputBuffer, int numFrames, bool stereoStream) {

        //reset buffer to 0
        memset(outputBuffer, 0, sizeof(int16_t) * numFrames);

        if (_isPlaying) {
            for (int i = 0; i < numFrames; i++) {
                outputBuffer[i] = file.samples[0][currentFrame];
                currentFrame++;
                //reset if reached end
                if (currentFrame >= sampleSize) {
                    currentFrame = 0;
                    _isPlaying = false;
                    break;
                }
            }
        }
    }

    void Sample::printSampleInfo() {
        int sampleRate = file.getSampleRate();
        int bitDepth = file.getBitDepth();

        int numSamples = file.getNumSamplesPerChannel();
        double lengthInSeconds = file.getLengthInSeconds();

        int numChannels = file.getNumChannels();
        bool isMono = file.isMono();
        bool isStereo = file.isStereo();
        LOG("sampleRate: %d", sampleRate);
        LOG("bitDepth: %d", bitDepth);
        LOG("numSamples: %d", numSamples);
        LOG("lengthInSeconds: %f", lengthInSeconds);
        LOG("numChannels: %d", numChannels);
        if (isMono) LOG("mono");
        if (isStereo) LOG("stereo");
    }

    bool Sample::isPlaying() const {
        return _isPlaying;
    }

    void Sample::play() {
        _isPlaying = true;
    }

} // percussionapp