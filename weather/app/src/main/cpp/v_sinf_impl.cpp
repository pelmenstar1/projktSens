#ifdef __ARM_NEON
#include <arm_neon.h>
#include <cmath>

static constexpr float Pi1 = 0x1.921fb6p+1f;
static constexpr float Pi2 = -0x1.777a5cp-24f;
static constexpr float Pi3 = -0x1.ee59dap-49f;
static constexpr float A3 = -0x1.555548p-3f;
static constexpr float A5 = 0x1.110df4p-7f;
static constexpr float A7 = -0x1.9f42eap-13f;
static constexpr float A9 = 0x1.5b2e76p-19f;
static constexpr float RangeVal = 0x1p20f;
static constexpr float InvPi = 0x1.45f306p-2f;
static constexpr float Shift = 0x1.8p+23f;
static constexpr uint32_t AbsMask = 0x7fffffff;

#define FALLBACK_TO_SCALAR(i) if(cmp[i] != 0) y[i] = sinf(x[i])

float32x2_t v_sinf(float32x2_t x) {
	float32x2_t n, r, r2, y;
	uint32x2_t sign, odd, cmp;

	r = vabs_f32(x);
	sign = vreinterpret_u32_f32(x) & ~AbsMask;
	cmp = (vreinterpret_u32_f32(r) >= vreinterpret_u32_f32(vdup_n_f32(RangeVal)));

	/* n = rint(|x|/pi) */
	n = vfma_f32(vdup_n_f32(Shift), vdup_n_f32(InvPi), r);
	odd = vreinterpret_u32_f32(n) << 31;
	n -= Shift;

	/* r = |x| - n*pi  (range reduction into -pi/2 .. pi/2) */
	r = vfma_f32(r, vdup_n_f32(-Pi1), n);
	r = vfma_f32(r, vdup_n_f32(-Pi2), n);
	r = vfma_f32(r, vdup_n_f32(-Pi3), n);

	/* y = sin(r) */
	r2 = r * r;

	y = vfma_f32(vdup_n_f32(A7), vdup_n_f32(A9), r2);
	y = vfma_f32(vdup_n_f32(A5), y, r2);
	y = vfma_f32(vdup_n_f32(A3), y, r2);
	y = vfma_f32(r, y * r2, r);

	/* sign fix */
	y = vreinterpret_f32_u32(vreinterpret_u32_f32(y) ^ sign ^ odd);

	FALLBACK_TO_SCALAR(0);
	FALLBACK_TO_SCALAR(1);

	return y;
}

float32x4_t v_sinfq(float32x4_t x) {
	float32x4_t n, r, r2, y;
	uint32x4_t sign, odd, cmp;

	r = vabsq_f32(x);
	sign = vreinterpretq_u32_f32(x) & ~AbsMask;
	cmp = (vreinterpretq_u32_f32(r) >= vreinterpretq_u32_f32(vdupq_n_f32(RangeVal)));

	/* n = rint(|x|/pi) */
	n = vfmaq_f32(vdupq_n_f32(Shift), vdupq_n_f32(InvPi), r);
	odd = vreinterpretq_u32_f32(n) << 31;
	n -= Shift;

	/* r = |x| - n*pi  (range reduction into -pi/2 .. pi/2) */
	r = vfmaq_f32(r, vdupq_n_f32(-Pi1), n);
	r = vfmaq_f32(r, vdupq_n_f32(-Pi2), n);
	r = vfmaq_f32(r, vdupq_n_f32(-Pi3), n);

	/* y = sin(r) */
	r2 = r * r;

	y = vfmaq_f32(vdupq_n_f32(A7), vdupq_n_f32(A9), r2);
	y = vfmaq_f32(vdupq_n_f32(A5), y, r2);
	y = vfmaq_f32(vdupq_n_f32(A3), y, r2);
	y = vfmaq_f32(r, y * r2, r);

	/* sign fix */
	y = vreinterpretq_f32_u32(vreinterpretq_u32_f32(y) ^ sign ^ odd);

	FALLBACK_TO_SCALAR(0);
	FALLBACK_TO_SCALAR(1);
	FALLBACK_TO_SCALAR(2);
	FALLBACK_TO_SCALAR(3);

	return y;
}

#endif
