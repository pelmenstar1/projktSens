#ifndef PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_ASTROSUNINFOPROVIDER_H_
#define PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_ASTROSUNINFOPROVIDER_H_

#include <jni.h>

jlong AstroSunInfoProvider_nGetSunriseSunsetTimeRange(JNIEnv* env, jclass, jint dayOfYear, jlong location);

void register_AstroSunInfoProvider(JNIEnv* env);

#endif //PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_ASTROSUNINFOPROVIDER_H_
