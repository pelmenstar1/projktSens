#include "AstroSunInfoProvider.h"

#include <cmath>

#ifdef __ARM_NEON
#include "arm_neon.h"
#include "vmath_arm.h"
#endif

static constexpr float D2R = (float)M_PI / 180.0f;
static constexpr float R2D = 180.0f / (float)M_PI;
static constexpr float ZENITH = 90.8888f;
static constexpr float COS_ZENITH = -0.015511;
static constexpr float HOURS_PER_DEGREE = 1.0f / 15.0f;

float int32ToFloat(uint32_t i) {
	union {
	  float f;
	  uint32_t i;
	} val{};
	val.i = i;

	return val.f;
}

float getLatitude(uint64_t location) {
	return int32ToFloat((uint32_t)location);
}

float getLongitude(uint64_t location) {
	return int32ToFloat((uint32_t)(location >> 32));
}

#ifdef __ARM_NEON
uint32x2_t getSunriseSunsetTimeRangeNeon(uint32_t dayOfYear,
										 float latitude,
										 float longitude);
#else
uint32_t getSunriseSunsetTimeInternal(uint32_t dayOfYear,
									  float latitude,
									  float longitude,
									  bool sunrise);
#endif

jlong AstroSunInfoProvider_nGetSunriseSunsetTimeRange(JNIEnv *env,
													  jclass,
													  jint dayOfYear,
													  jlong location) {
	float latitude = getLatitude(location);
	float longitude = getLongitude(location);

#ifdef __ARM_NEON
	uint32x2_t sunriseSunset = getSunriseSunsetTimeRangeNeon(dayOfYear, latitude, longitude);

	return ((uint64_t)sunriseSunset[1] << 32) | (uint64_t)sunriseSunset[0];
#else
	uint32_t sunrise = getSunriseSunsetTimeInternal(dayOfYear, latitude, longitude, true);
	uint32_t sunset = getSunriseSunsetTimeInternal(dayOfYear, latitude, longitude, false);

	return ((uint64_t)sunset << 32) | (uint64_t)sunrise;
#endif
}

#if defined(__ARM_NEON)

#define DEFINE_V_ALIGN_N(n, nb) \
float32x2_t v_align##n(float32x2_t val) { \
	uint32x2_t vnb = vdup_n_u32(nb); \
	val -= vreinterpret_f32_u32((val > (float)(n)) & vnb); \
	val += vreinterpret_f32_u32((val < 0) & vnb); \
	return val; \
}

DEFINE_V_ALIGN_N(360, 0x43b40000)
DEFINE_V_ALIGN_N(24, 0x41c00000)

#undef DEFINE_V_ALIGN_N

uint32x2_t getSunriseSunsetTimeRangeNeon(uint32_t dayOfYear,
										 float latitude,
										 float longitude) {
	float latRad = latitude * D2R;
	float lnHour = longitude * HOURS_PER_DEGREE;

	float32x2_t dHours = { 6.0f, 18.0f };

	float32x2_t t = vfma_f32(
		vdup_n_f32((float)dayOfYear),
		(dHours - lnHour),
		vdup_n_f32(0.04166666666f)
		);
	float32x2_t m = vfma_f32(vdup_n_f32(-3.289f), t, vdup_n_f32(0.9856f));
	float32x4_t mq = { m[0], m[0], m[1], m[1] };

	float32x4_t sinMDM = mq * float32x4_t{D2R, D2R * 2.0f, D2R, D2R * 2.0f};
	sinMDM = v_sinfq(sinMDM);
	sinMDM *= float32x4_t{ 1.916f, 0.02f, 1.916f, 0.02f };
	float32x2_t sinMDMsum = { vaddv_f32(vget_low_f32(sinMDM)), vaddv_f32(vget_high_f32(sinMDM)) };

	float32x2_t l = v_align360(m + sinMDMsum + 282.634f);

	float32x2_t lRad = l * D2R;
	float32x2_t ra = { tanf(lRad[0]), tanf(lRad[1]) };
	ra *= 0.91764f;

	ra[0] = atanf(ra[0]);
	ra[1] = atanf(ra[1]);

	ra = v_align360(R2D * ra);

	float32x4_t lRaQVec = vcombine_f32(l, ra) * (1.0f / 90.0f);

	lRaQVec[0] = floorf(lRaQVec[0]);
	lRaQVec[1] = floorf(lRaQVec[1]);
	lRaQVec[2] = floorf(lRaQVec[2]);
	lRaQVec[3] = floorf(lRaQVec[3]);

	lRaQVec *= 90;

	ra += { lRaQVec[0] - lRaQVec[2], lRaQVec[1] - lRaQVec[3] };
	ra *= HOURS_PER_DEGREE;

	float32x2_t sinDec = 0.39782f * v_sinf(lRad);
	float32x2_t cosDec = v_cosf(v_asinf(sinDec));

	float32x2_t sinCosLat = v_sincosf_fast(latRad);
	float32x2_t cosH = (vfma_f32(vdup_n_f32(COS_ZENITH), -sinDec, vdup_n_f32(sinCosLat[0]))) / (cosDec * sinCosLat[1]);

	float32x2_t h = {
		fma(-R2D, acosf(cosH[0]), 360.0f),
		R2D * acosf(cosH[1])
	};

	float32x2_t hour = vfma_f32(ra, h, vdup_n_f32(HOURS_PER_DEGREE)) +
		vfma_f32(vdup_n_f32(-6.622f), vdup_n_f32(-0.06571f), t);

	hour = v_align24(hour);

	return vcvt_u32_f32((hour - lnHour) * 3600.0f);
}

#elif __x86_64__

#define DEFINE_ALIGN_N(n) \
float align##n(float val) { \
    if(val > (n)) {         \
    	val -= (n);         \
    }						\
    if(val < 0) { 			\
		val += (n); 		\
    } 						\
    return val; 			\
}

DEFINE_ALIGN_N(360)
DEFINE_ALIGN_N(24)

#undef DEFINE_ALIGN_N

uint32_t getSunriseSunsetTimeInternal(uint32_t dayOfYear,
									  float latitude,
									  float longitude,
									  bool sunrise) {
	float latRad = latitude * D2R;
	float lnHour = longitude * HOURS_PER_DEGREE;

	float dHours = sunrise ? 6.0f : 18.0f;
	float t = fma((dHours - lnHour), 0.04166666666f, (float)dayOfYear);
	float m = fma(0.9856f, t, -3.289f);

	float l = align360(m + 1.916f * sinf(m * D2R) + 0.02f * sinf(m * D2R * 2.0f) + 282.634f);
	float lRad = l * D2R;

	float ra = align360(R2D * atanf(0.91764f * tanf(lRad)));

	float lQ = floorf(l * (1 / 90.0f)) * 90.0f;
	float raQ = floorf(ra * (1 / 90.0f)) * 90.0f;

	ra += lQ - raQ;
	ra *= HOURS_PER_DEGREE;

	float sinDec = 0.39782f * sinf(lRad);
	float cosDec = cosf(asin(sinDec));

	float sinLat;
	float cosLat;

	sincosf(latRad, &sinLat, &cosLat);

	float cosH = (COS_ZENITH - sinDec * sinLat) / (cosDec * cosLat);

	float acosH = acosf(cosH);
	float h;

	if (sunrise) {
		h = fma(-R2D, acosH, 360.0f);
	} else {
		h = R2D * acosH;
	}

	float hour = fma(h, HOURS_PER_DEGREE, ra) + fma(-0.06571f, t, - 6.622f);
	hour = align24(hour);

	auto utcTime = (uint32_t)((hour - lnHour) * 3600.0f);

	return utcTime;
}
#endif

static JNINativeMethod gMethods[] = {
	{
		"nGetSunriseSunsetTimeRangeUtc", "(IJ)J",
	 	(void *)AstroSunInfoProvider_nGetSunriseSunsetTimeRange
	}
};

void register_AstroSunInfoProvider(JNIEnv *env) {
	jclass c = env->FindClass("com/pelmenstar/projktSens/weather/app/astro/AstroSunInfoProvider");
	env->RegisterNatives(c, gMethods, 1);
}


