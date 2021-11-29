#ifdef __ARM_NEON

#include <arm_neon.h>
#include <cmath>

float32x2_t acosf(float32x2_t x) {
	return { acosf(x[0]), acosf(x[1]) };
}

#endif

