//
// Created by Kuba on 05/03/2025.
//

#include "Player.h"
#include <oboe/Oboe.h>
#include "Log.h"
#include <fstream>
#include "Patterns.h"

namespace percussionapp {
    void Player::changeStyle(int _style) {
        style = _style;
        if (style == 0) {
            currentPattern = tumbaoBeats;
        } else if (style == 1) {
            currentPattern = mozambiqueBeats;
        } else if (style == 2) {
            currentPattern = guaguancoBeats;
            backingEventsBeats = rumbaClaveEventsBeats;
        } else if (style == 3) {
            currentPattern = merengueBeats;
        } else {
            LOG("INVALID PATTERN: %d", style);
            currentPattern = tumbaoBeats;
        }
    }

    bool Player::start(std::shared_ptr<AudioHandler> audioSink) {
        audioHandler = std::move(audioSink);
        LOG("player starting");
        if (style == 2) {
            backingEventsBeats = rumbaClaveEventsBeats;
        } else {
            backingEventsBeats = claveEventsBeats;
        }
        oboe::AudioStreamBuilder builder;
        builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
        builder.setSharingMode(oboe::SharingMode::Exclusive);
        builder.setFormat(oboe::AudioFormat::I16);
        if (stereo) {
            builder.setChannelCount(2);
        } else {
            builder.setChannelCount(1);
        }
        builder.setSampleRate(SAMPLE_RATE); //48k
        builder.setDirection(oboe::Direction::Output);
        builder.setFormatConversionAllowed(true);
        builder.setSampleRateConversionQuality(oboe::SampleRateConversionQuality::Medium);
        //builder.setFramesPerDataCallback(96); //MAGIC NUMBER -- CHANGE!! (96 default for some devices?)
        std::shared_ptr<oboe::AudioStreamDataCallback> thisPlayer(
                static_cast<oboe::AudioStreamDataCallback *>(this));
        builder.setDataCallback(thisPlayer);
        LOG("calling parent start function...");
        _isPlaying = true;
        bool success = openAndStartStream(builder);
        patternStartMs =
                duration_cast<std::chrono::milliseconds>(
                        std::chrono::high_resolution_clock::now().time_since_epoch()).count();
        return success;
    }

    bool Player::stop() {
        AudioStreamWrapper::stop();
        return true;
    }

    oboe::DataCallbackResult
    Player::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {

        if (_isPlaying) {
            updateCurrentTime();
            updateCurrentBar();
            auto *outputBuffer = static_cast<int16_t *>(audioData);
            //std::string temp = ("current pattern event: " + std::to_string(currentPatternEvent));
            long long currentTimeFromPatternStart = currentTimeMs - patternStartMs;

            //if time to next miss has passed, inform the UI and update the pattern event
            if (currentTimeFromPatternStart > nextMissMs) {
                //LOG("%lld ",currentTimeFromPatternStart);
                //LOG("SKIP at %d!", (int) nextMissMs);
                //-2 = SKIP
                audioHandler->onSound(-2);
                updateCurrentPatternEvent();

            }

            //ensure that we aren't repeating the clave - the final clave event occurs before timefrompatternstart is reset
            if (currentBackingEventIndex < backingEventsBeats.size() &&
                (currentTimeFromPatternStart >=
                 beatsToMillis(backingEventsBeats[currentBackingEventIndex]))) {

                clave->play();
                currentBackingEventIndex++;
            }

            // send remaining buffer to sample to render the sound
            clave->sendCurrentSamplesToBuff((outputBuffer), (numFrames), stereo);

        } else if (!_isRunning) {
            LOG("playing stopped.");
            return oboe::DataCallbackResult::Stop;
        } else {
            //fill buffer with 0s - not doing this can cause some audio glitches if the player is paused
            auto *outputBuffer = static_cast<int16_t *>(audioData);
            for (int i = 0; i < numFrames; i++) {
                outputBuffer[i] = 0;
            }
        }
        return oboe::DataCallbackResult::Continue;
    }

    //0 = correct time
    //-1 = miss
    //-2 = skip
    //1 = early
    //2 = late
    int Player::checkNoteOnTime(int type, long long timePlayed) {
        updateCurrentBar();
        int patternLengthMs = (clavePatternLengthBeats *
                               (int) beatLengthMilliseconds);

        long long timePlayedFromPatternStart = (timePlayed - patternStartMs - latency) % patternLengthMs;
        float temp = currentPattern[currentPatternEvent][1];
        LOG("%f", temp);
        //sometimes the current note is in the next iteration of the pattern, so we need to take the mod
        int currentEventInBar = currentPatternEvent % currentPattern.size();

        float correctTime = beatsToMillis(currentPattern[currentEventInBar][0]) + 125.0;
        //LOG("time: %lld", timePlayedFromPatternStart);
        //LOG("correct time: %f ", correctTime);
        //LOG("((int)correctTime + patternLengthMs) mod patternLengthMs: %d",((int)correctTime + patternLengthMs) % patternLengthMs + 1);
        //LOG("correctTime + patternLengthMs: %f" , correctTime + patternLengthMs);
        //LOG("difference = %f for %d", (correctTime - timePlayedFromPatternStart), currentEventInBar);
        LOG("My time - %lld , correct time - %f", timePlayedFromPatternStart, correctTime);
        // ON TIME
        if ((timePlayedFromPatternStart <= correctTime &&
             timePlayedFromPatternStart > correctTime - earlyOffsetMs) ||
            (timePlayedFromPatternStart > correctTime &&
             timePlayedFromPatternStart < correctTime + lateOffsetMs)
            //IF AT 0!
            || (timePlayedFromPatternStart <= correctTime + patternLengthMs &&
            timePlayedFromPatternStart > correctTime + patternLengthMs - earlyOffsetMs))
        {
            //LOG("correctTime = %d", correctTime);
            //LOG("EVENT INDEX: %d",eventIndex);
            updateCurrentPatternEvent();
            LOG("ON TIME");
            return 0;
        }
        //EARLY
        if ( timePlayedFromPatternStart <  ((int)correctTime - earlyOffsetMs + patternLengthMs ) % patternLengthMs //ensures no negatives
            && timePlayedFromPatternStart > ((int)correctTime - earlyOffsetMs - missOffsetMs + patternLengthMs ) % patternLengthMs) {
            //LOG("correctTime = %d", correctTime);
            updateCurrentPatternEvent();
            //LOG("EARLY");
            return 1;
        }
        //LATE
        if (timePlayedFromPatternStart > correctTime + lateOffsetMs
            && timePlayedFromPatternStart < correctTime + lateOffsetMs + missOffsetMs) {
            //("correctTime = %d", correctTime);
            updateCurrentPatternEvent();
            //LOG("LATE");
            return 2;
        }

        //LOG("MISS");
        return -1;
    }

    void Player::updateCurrentTime() {
        currentTimeMs = duration_cast<std::chrono::milliseconds>
                (std::chrono::high_resolution_clock::now().time_since_epoch()).count();
    }

    void Player::changeDifficulty(int newDifficulty) {
        difficulty = newDifficulty;
        switch (difficulty) {
            case 1:
                earlyOffsetMs = 80;
                lateOffsetMs = 80;
                missOffsetMs = 100;
                LOG("1 NOW");
                break;
            case 2:
                earlyOffsetMs = 70;
                lateOffsetMs = 70;
                missOffsetMs = 95;
                LOG("2 NOW");
                break;
            case 3:
                earlyOffsetMs = 60;
                lateOffsetMs = 60;
                missOffsetMs = 90;
                LOG("3 NOW");
                break;
            default:
                earlyOffsetMs = 70;
                lateOffsetMs = 70;
                missOffsetMs = 95;
                LOG("DEFAULT NOW");
        }
    }

    void Player::updateCurrentPatternEvent() {
        currentPatternEvent++;
        //If we haven't reached end of pattern, update the next time before miss
        if (currentPatternEvent < currentPattern.size()) {
            nextMissMs = (beatsToMillis(currentPattern[currentPatternEvent][0]) +
                          lateOffsetMs + missOffsetMs);
        } else //if end of pattern reached, wait for the bar to reset nextMissMs to 0 (+ latency)
        {
            nextMissMs = beatsToMillis(clavePatternLengthBeats);
        }
    }

    void Player::updateCurrentBar() {
        long long currentTimeFromPatternStart = currentTimeMs - patternStartMs;
        //reset pattern if end reached
        if (currentTimeFromPatternStart >= beatsToMillis(clavePatternLengthBeats)) {
            currentBar = 1;

            patternStartMs = duration_cast<std::chrono::milliseconds>
                    (std::chrono::high_resolution_clock::now().time_since_epoch()).count();

            currentBackingEventIndex = 0;
            currentPatternEvent = currentPatternEvent % currentPattern.size();
            nextMissMs = (beatsToMillis(currentPattern[currentPatternEvent][0]) +
                          lateOffsetMs + missOffsetMs);

            //if 2nd bar reached, move to 2nd bar and emit event
        } else if (currentBar == 1 &&
                   currentTimeFromPatternStart > beatsToMillis(clavePatternLengthBeats) / 2) {
            currentBar = 2;
        }

        audioHandler->onNewBar(currentBar);
        updateCurrentTime();
    }

    void Player::toggleMetronome(bool metronomeOn) {

        if (metronomeOn) {
            backingEventsBeats = metronomeEventsBeats;
        } else {
            //if guaguanco, rumba clave
            if (style == 2) {
                backingEventsBeats = rumbaClaveEventsBeats;
            } else {
                backingEventsBeats = claveEventsBeats;
            }
        }
    }

    float Player::beatsToMillis(float beats) const {
        return beats * beatLengthMilliseconds;
    }

} // percussionapp

