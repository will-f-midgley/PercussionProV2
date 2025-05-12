//
// Created by Kuba on 24/04/2025.
//


#ifndef PERCUSSIONAPP_AUDIOSTREAMWRAPPER_H
#define PERCUSSIONAPP_AUDIOSTREAMWRAPPER_H

#include <oboe/Oboe.h>
#include "AudioHandler.h"

namespace percussionapp {

    class AudioStreamWrapper {

    public:
        virtual bool start(std::shared_ptr<AudioHandler> audioSink);

        virtual bool stop();

        virtual bool isRunning() { return _isRunning; };

        void printStreamInfo();

    protected:
        bool openAndStartStream(oboe::AudioStreamBuilder builder);

        std::shared_ptr<AudioHandler> audioHandler;
        bool _isRunning = false;
        std::shared_ptr<oboe::AudioStream> aStream;
    };

} // percussionapp

#endif //PERCUSSIONAPP_AUDIOSTREAMWRAPPER_H
