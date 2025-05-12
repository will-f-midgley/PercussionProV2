//
// Created by Kuba on 24/04/2025.
//

#ifndef PERCUSSIONAPP_PATTERNS_H
#define PERCUSSIONAPP_PATTERNS_H

#include <vector>
#include <array>

std::vector<float> claveEventsBeats{0,
                                    (1.5f),
                                    (3),
                                    (5),
                                    (6)};
std::vector<float> rumbaClaveEventsBeats{0,
                                         (1.5f),
                                         (3.5f),
                                         (5),
                                         (6)};
std::vector<float> metronomeEventsBeats{0, (1),
                                        (2),
                                        (3),
                                        (4),
                                        (5),
                                        (6),
                                        (7)};

std::vector<std::array<float, 2>> tumbaoBeats{{0,    2},
                                              {0.5f, 3},
                                              {1.0f, 1},
                                              {1.5f, 3},
                                              {2,    2},
                                              {2.5f, 3},
                                              {3,    0},
                                              {3.5f, 0},
                                              {4,    2},
                                              {4.5f, 3},
                                              {5,    1},
                                              {5.5f, 0},
                                              {6,    0},
                                              {6.5f, 3},
                                              {7,    0},
                                              {7.5f, 0}
};

std::vector<std::array<float, 2>> guaguancoBeats{{0.5f, 1},
                                                 {1,    1},
                                                 {1.5f, 2},
                                                 {2.5f, 3},
                                                 {3,    0},
                                                 {3.5f, 1},
                                                 {4,    0},
                                                 {5,    1},
                                                 {5.5f, 0},
                                                 {6.5f, 3},
                                                 {7,    0},
                                                 {7.5f, 1}
};
std::vector<std::array<float, 2>> merengueBeats{{0,    1},
                                                {1.5f, 3},
                                                {2,    1},
                                                {3,    0},
                                                {4,    0},
                                                {4.5f, 0},
                                                {5.5f, 3},
                                                {6,    1},
                                                {7,    0},
};
std::vector<std::array<float, 2>> mozambiqueBeats{{0,    0},
                                                  {1,    1},
                                                  {1.5f, 0},
                                                  {2,    0},
                                                  {2.5f, 0},
                                                  {3,    0},
                                                  {3.5f, 1},
                                                  {4.5f, 1},
                                                  {5,    1},
                                                  {5.5f, 0},
                                                  {6,    0},
                                                  {6.5f, 0},
                                                  {7,    0},
                                                  {7.5f, 1}
};
#endif //PERCUSSIONAPP_PATTERNS_H
