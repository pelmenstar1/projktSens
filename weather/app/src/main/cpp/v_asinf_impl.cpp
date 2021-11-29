#ifdef __ARM_NEON
#include <arm_neon.h>

#include <cmath>

static const double
	pio2 = 1.570796326794896558e+00;

static const float
/* coefficients for R(x^2) */
pS0 = 1.6666586697e-01,
	pS1 = -4.2743422091e-02,
	pS2 = -8.6563630030e-03,
	qS1 = -7.0662963390e-01;

static float32x2_t RV(float32x2_t z) {
	float32x2_t p = z * (pS0 + z * (pS1 + z * pS2));
	float32x2_t q = 1.0f + z * qS1;
	return p / q;
}

static float R(float z) {
	float p, q;
	p = z * (pS0 + z * (pS1 + z * pS2));
	q = 1.0f + z * qS1;
	return p / q;
}

float32x2_t v_asinf(float32x2_t x) {
	uint32x2_t hx = vreinterpret_u32_f32(x);
	uint32x2_t ix = hx & 0x7fffffff;

	uint32x2_t overCond = ix >= 0x3f800000;
	if ((overCond[0] & overCond[1]) != 0) {
		uint32x2_t eqCond = ix == 0x3f800000;
		if ((eqCond[0] & eqCond[1]) != 0) {
			return vfma_f32(vdup_n_f32(0x1p-120f), x, vdup_n_f32(pio2));
		} else {
			float32x2_t result = vdup_n_f32(NAN);
#define FALLBACK(i) if(eqCond[i] != 0) { result[i] = fma(x[i], pio2, 0x1p-120f); }
			FALLBACK(0)
			FALLBACK(1)
#undef FALLBACK

			return result;
		}
	} else if ((overCond[0] | overCond[1]) != 0) {
		return {asinf(x[0]), asinf(x[1])};
	}

	uint32x2_t lessCond = ix < 0x3f000000;
	if ((lessCond[0] & lessCond[1]) != 0) {
		uint32x2_t c = (ix < 0x39800000) & (ix >= 0x00800000);
		if ((c[0] & c[1]) != 0) {
			return x;
		} else if ((c[0] | c[1]) != 0) {
			float32x2_t res = x;
#define FALLBACK(i) if(c[i] != 0) { res[i] = (x[i] + x[i] * R(x[i] * x[i])); }

			FALLBACK(0)
			FALLBACK(1)
#undef FALLBACK

			return res;
		} else {
			return x + x * RV(x * x);
		}
	} else if ((lessCond[0] | lessCond[1]) != 0) {
		return {asinf(x[0]), asinf(x[1])};
	}

	float32x2_t z = (1 - vabs_f32(x)) * 0.5f;
	float32x2_t s = vsqrt_f32(z);
	x = pio2 - 2 * (s + s * RV(z));

	uint32x2_t c = (hx >> 31) != 0;
	if(c[0] != 0) {
		x[0] = -x[0];
	}
	if(c[1] != 0) {
		x[1] = -x[1];
	}

	return x;
}

#endif

