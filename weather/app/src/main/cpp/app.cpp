#include "app.h"
#include "AstroSunInfoProvider.h"
#include "AstroMoonInfoProvider.h"

jint JNI_OnLoad(JavaVM *vm, void *) {
	JNIEnv *env;
	if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
		return JNI_ERR;
	}

	register_AstroSunInfoProvider(env);
	register_AstroMoonInfoProvider(env);

	return JNI_VERSION_1_6;
}