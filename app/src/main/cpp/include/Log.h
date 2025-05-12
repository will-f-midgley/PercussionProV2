//
// Created by Kuba on 12/02/2025.
//
#pragma once

#include <android/log.h>

#ifndef NDEBUG
#define LOG(args...) __android_log_print(android_LogPriority::ANDROID_LOG_DEBUG, "AudioHandler", args)
#else
#define LOG(args...)
#endif
