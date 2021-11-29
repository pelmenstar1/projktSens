//
// Created by pelme on 16.10.2021.
//

#include "AstroMoonInfoProvider.h"

#ifdef __ARM_NEON
#include <arm_neon.h>
#endif

#include <cmath>
#include "vmath_arm.h"

static constexpr float D2R = M_PI / 180.0f;
static constexpr float EPOCH = 2444238.5f;
static constexpr float ELONGE = 278.833540f;
static constexpr float ELONGP = 282.596403f;
static constexpr float ECCENT = 0.016718f;
static constexpr float MMLONG = 64.975464f;
static constexpr float MMLONGP = 349.383063f;

float getMoonPhase(uint32_t year, uint32_t month, uint32_t day);

jfloat AstroMoonInfoProvider_nGetMoonPhase(JNIEnv *env, jclass, jint year, jint month, jint day) {
	return getMoonPhase(year, month, day);
}

uint64_t toJulianDate(uint32_t year, uint32_t month, uint32_t day) {
	if (month > 2) {
		month -= 3;
	} else {
		month += 9;
		year--;
	}

	uint64_t c = year / 100L;
	year -= 100L * c;

	return day + (c * 146097L) / 4 + (year * 1461L) / 4 + (month * 153L + 2) / 5 + 1721119L;
}

float fixAngle(float a) {
	return fma(-360.0f, floorf(a * (1 / 360.0f)), a);
}

float kepler(float m) {
	float e, delta;

	e = m = D2R * m;

	do {
		float sinE;
		float cosE;

		sincosf(e, &sinE, &cosE);

		delta = fma(-ECCENT, sinE, e) - m;
		e -= delta / fma(-ECCENT, cosE, 1.0f);
	} while (abs(delta) > 0.000001f);

	return e;
}

#ifdef __ARM_NEON

float getMoonPhase(uint32_t year, uint32_t month, uint32_t day) {
	float dayF = toJulianDate(year, month, day) - EPOCH;

	float n = fixAngle((360.0f / 365.2422f) * dayF);
	float m = fixAngle(n + ELONGE - ELONGP);
	float sinM = sinf(m * D2R);

	float ec = kepler(m);
	ec = 1.01686011182f * tanf(ec * 0.5f);
	ec = 2.0f * D2R * atanf(ec);

	float lambdaSun = fixAngle(ec + ELONGP);
	float ml = fixAngle(fma(13.1763966f, dayF, MMLONG));
	float mm = fixAngle(ml - fma(0.1114041f, dayF, MMLONGP));
	float ev = 1.2739f * sinf(D2R * 2.0f * (ml - lambdaSun) - mm);

	float ae = 0.1858f * sinM;
	float a3 = 0.37f * sinM;

	float mmp = mm + ev - ae - a3;
	float32x2_t mEc_a4_vec = vdup_n_f32(mmp) * float32x2_t {D2R, 2.0f * D2R };
	mEc_a4_vec = v_sinf(mEc_a4_vec);
	mEc_a4_vec *= float32x2_t { 6.2886f, 0.214f };

	float mEc = mEc_a4_vec[0];
	float a4 = mEc_a4_vec[1];

	float lP = ml + ev + mEc - ae + a4;
	float lPP = fma(0.6583f, sinf(2.0f * D2R * (lP - lambdaSun)), lP);

	float moonAge = lPP - lambdaSun;

	return fma(-0.5f, cosf(moonAge * D2R), 0.5f);
}
#elif __x86_64__
float getMoonPhase(uint32_t year, uint32_t month, uint32_t day) {
	float dayF = toJulianDate(year, month, day) - EPOCH;

	float n = fixAngle((360.0f / 365.2422f) * dayF);
	float m = fixAngle(n + ELONGE - ELONGP);
	float sinM = sinf(m * D2R);

	float ec = kepler(m);
	ec = 1.01686011182f * tanf(ec * 0.5f);
	ec = 2.0f * D2R * atanf(ec);

	float lambdaSun = fixAngle(ec + ELONGP);
	float ml = fixAngle(fma(13.1763966f, dayF, MMLONG));
	float mm = fixAngle(ml - fma(0.1114041f, dayF, MMLONGP));
	float ev = 1.2739f * sinf(D2R * 2.0f * (ml - lambdaSun) - mm);

	float ae = 0.1858f * sinM;
	float a3 = 0.37f * sinM;

	float mmp = mm + ev - ae - a3;
	float mEc = 6.2886f * sinf(D2R * mmp);
	float a4 = 0.214f * sinf(D2R * 2.0f * mmp);
	float lP = ml + ev + mEc - ae + a4;
	float lPP = fma(0.6583f, sinf(2.0f * D2R * (lP - lambdaSun)), lP);

	float moonAge = lPP - lambdaSun;

	return fma(-0.5f, cosf(moonAge * D2R), 0.5f);
}
#endif

static JNINativeMethod gMethods[] = {
	{
		"nGetMoonPhase",
		"(III)F",
		(void*)AstroMoonInfoProvider_nGetMoonPhase
	}
};

void register_AstroMoonInfoProvider(JNIEnv* env) {
	jclass c = env->FindClass("com/pelmenstar/projktSens/weather/app/astro/AstroMoonInfoProvider");
	env->RegisterNatives(c, gMethods, 1);
}