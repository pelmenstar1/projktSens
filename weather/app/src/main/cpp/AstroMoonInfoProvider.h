#ifndef PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_ASTROMOONINFOPROVIDER_H_
#define PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_ASTROMOONINFOPROVIDER_H_

#include <jni.h>

jfloat AstroMoonInfoProvider_nGetMoonPhase(JNIEnv* env, jclass, jint year, jint month, jint day);

void register_AstroMoonInfoProvider(JNIEnv* env);

#endif //PROJKT_SENS_WEATHER_APP_SRC_MAIN_CPP_ASTROMOONINFOPROVIDER_H_
