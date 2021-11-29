#ifdef __ARM_NEON
#include <arm_neon.h>

#include <cmath>
#include "config.h"

static const float pi63 = 0x1.921FB54442D18p-62;
static const float pio4 = 0x1.921FB54442D18p-1;

struct sincos_t {
  float hpi_inv;
  float hpi;
  float c0, c1, c2, c3, c4;
  float s1, s2, s3;
};

static const float sincos_sign[4] = {
	1.0f, -1.0f, -1.0f, 1.0f
};

static const sincos_t sincosf_table[2] = {
	{
		0x1.45F306DC9C883p-1,
		0x1.921FB54442D18p0,
		0x1p0,
		-0x1.ffffffd0c621cp-2,
		0x1.55553e1068f19p-5,
		-0x1.6c087e89a359dp-10,
		0x1.99343027bf8c3p-16,
		-0x1.555545995a603p-3,
		0x1.1107605230bc4p-7,
		-0x1.994eb3774cf24p-13
	},
	{
		0x1.45F306DC9C883p-1,
		0x1.921FB54442D18p0,
		-0x1p0,
		0x1.ffffffd0c621cp-2,
		-0x1.55553e1068f19p-5,
		0x1.6c087e89a359dp-10,
		-0x1.99343027bf8c3p-16,
		-0x1.555545995a603p-3,
		0x1.1107605230bc4p-7,
		-0x1.994eb3774cf24p-13
	}
};
static const uint32_t inv_pio4[] = {
	0xa2, 0xa2f9, 0xa2f983, 0xa2f9836e,
	0xf9836e4e, 0x836e4e44, 0x6e4e4415, 0x4e441529,
	0x441529fc, 0x1529fc27, 0x29fc2757, 0xfc2757d1,
	0x2757d1f5, 0x57d1f534, 0xd1f534dd, 0xf534ddc0,
	0x34ddc0db, 0xddc0db62, 0xc0db6295, 0xdb629599,
	0x6295993c, 0x95993c43, 0x993c4390, 0x3c439041
};

static constexpr inline uint32_t as_uint(float f) {
	union { float f;uint32_t i; } u = {f};
	return u.i;
}

static constexpr inline float as_float(uint32_t i) {
	union { uint32_t i; float f; } u = {i};
	return u.f;
}

static constexpr inline uint32_t abs_top12(float x) {
	return (as_uint(x) >> 20) & 0x7ff;
}

static constexpr inline uint32_t abs_top12(uint32_t x) {
	return (x >> 20) & 0x7ff;
}

static inline float reduce_fast(float x, const sincos_t *p, int *np) {
	float r = x * p->hpi_inv;
	*np = (int)lround(r);
	return x - round(r) * p->hpi;
}

static inline float reduce_large(uint32_t xi, int *np) {
	const uint32_t *arr = &inv_pio4[(xi >> 26) & 15];
	uint32_t shift = (xi >> 23) & 7;
	xi = ((xi & 0xffffff) | 0x800000) << shift;

	uint64_t res0 = xi * arr[0];
	uint64_t res1 = (uint64_t)xi * arr[4];
	uint64_t res2 = (uint64_t)xi * arr[8];

	res0 = (res2 >> 32) | (res0 << 32);
	res0 += res1;

	uint64_t n = (res0 + (1ULL << 61)) >> 62;
	res0 -= n << 62;

	float x = (int64_t)res0;
	*np = n;
	return x * pi63;
}

static inline void force_eval_float(float x) {
	volatile float y = x;
}

static float32x2_t sincosf_poly(float x, float x2, const sincos_t *p, int n) {
	float32x2_t x2_vec = vdup_n_f32(x2);
	float32x2_t x3_x4_vec = x2_vec * float32x2_t{x, x2};

	float32x2_t s1_c2_vec = vfma_f32(float32x2_t{p->s2, p->c3}, x2_vec, float32x2_t{p->s3, p->c4});

	float c1 = fma(x2, p->c1, p->c0);
	float32x2_t x5_x6 = x2_vec * x3_x4_vec;
	float32x2_t s_c_vec = vfma_f32(float32x2_t{x, c1}, x3_x4_vec, float32x2_t{p->s1, p->c2});
	float32x2_t sin_cos_vec = vfma_f32(s_c_vec, x5_x6, s1_c2_vec);

	if(n & 1) {
		return vrev64_f32(sin_cos_vec);
	} else {
		return sin_cos_vec;
	}
}

float32x2_t v_sincosf_fast(float x) {
	float s;
	int n;
	const sincos_t *p = &sincosf_table[0];

	uint32_t xi = as_uint(x);
	uint32_t x_top12 = abs_top12(xi);

	if (x_top12 < abs_top12(pio4)) {
		float x2 = x * x;

		if (unlikely(x_top12 < abs_top12(0x1p-12f))) {
			if (unlikely(x_top12 < abs_top12(0x1p-126f))) {
				force_eval_float(x2);
			}

			return { x, 1.0f };
		}

		return sincosf_poly(x, x2, p, 0);
	} else if (x_top12 < abs_top12(120.0f)) {
		x = reduce_fast(x, p, &n);
		s = sincos_sign[n & 3];
		p += n & 2;

		float32x2_t res = vdup_n_f32(x) * float32x2_t { s, x };

		return sincosf_poly(res[0], res[1], p, n);
	} else if (likely(x_top12 < abs_top12(INFINITY))) {
		uint32_t sign = xi >> 31;

		x = reduce_large(xi, &n);
		s = sincos_sign[(n + sign) & 3];
		p += (n + sign) & 2;

		float32x2_t res = vdup_n_f32(x) * float32x2_t { s, x };
		return sincosf_poly(res[0], res[1], p, n);
	} else {
		return vdup_n_f32(0);
	}
}

#endif
