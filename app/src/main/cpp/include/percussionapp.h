//
// Created by Kuba on 25/03/2025.
//

#pragma once

#ifndef PERCUSSIONAPP_PERCUSSIONAPP_H
#define PERCUSSIONAPP_PERCUSSIONAPP_H

namespace percussionapp {
    constexpr int FRAMES_PER_CALLBACK = 256;
    constexpr int FFT_INPUT_SIZE = 2048;
    constexpr int FFT_WINDOWS = ((FFT_INPUT_SIZE / 2) + 1);
}


#endif //PERCUSSIONAPP_PERCUSSIONAPP_H
