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
static constexpr float HalfPi = 0x1.921fb6p0f;
static constexpr float Shift = 0x1.8p+23f;
static constexpr uint32_t AbsMask = 0x7fffffff;

float32x2_t v_cosf(float32x2_t x) {
	float32x2_t n, r, r2, y;
	uint32x2_t odd, cmp;

	r = vreinterpret_f32_u32 (vreinterpret_u32_f32 (x) & AbsMask);
	cmp =  (vreinterpret_u32_f32 (r) >= vreinterpret_u32_f32 (vdup_n_f32(RangeVal)));

	/* n = rint((|x|+pi/2)/pi) - 0.5 */
	n = vfma_f32(vdup_n_f32(Shift), vdup_n_f32(InvPi), r + HalfPi);
	odd = vreinterpret_u32_f32 (n) << 31;
	n -= Shift;
	n -= 0.5f;

	/* r = |x| - n*pi  (range reduction into -pi/2 .. pi/2) */
	r = vfma_f32(r, vdup_n_f32(-Pi1), n);
	r = vfma_f32(r, vdup_n_f32(-Pi2), n);
	r = vfma_f32(r, vdup_n_f32(-Pi3), n);

	/* y = sin(r) */
	r2 = r * r;
	y = vfma_f32 (vdup_n_f32(A7), vdup_n_f32(A9), r2);
	y = vfma_f32 (vdup_n_f32(A5), y, r2);
	y = vfma_f32 (vdup_n_f32(A3), y, r2);
	y = vfma_f32 (r, y * r2, r);

	/* sign fix */
	y = vreinterpret_f32_u32 (vreinterpret_u32_f32 (y) ^ odd);

	if(cmp[0] != 0) {
		y[0] = cosf(x[0]);
	}

	if(cmp[1] != 0) {
		y[1] = cosf(x[1]);
	}

	return y;
}

#endif
