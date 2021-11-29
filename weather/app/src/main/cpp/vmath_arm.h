//
// Created by pelme on 15.10.2021.
//

#ifndef PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_VMATH_ARM_H_
#define PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_VMATH_ARM_H_

#if defined(__ARM_NEON)
#include <arm_neon.h>
#include "config.h"

float32x2_t v_asinf(float32x2_t x);
float32x2_t v_sinf(float32x2_t x);
float32x4_t v_sinfq(float32x4_t x);

float32x2_t v_acosf(float32x2_t x);
float32x2_t v_cosf(float32x2_t x);

float32x2_t v_sincosf_fast(float x);

#endif

#endif //PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_VMATH_ARM_H_
