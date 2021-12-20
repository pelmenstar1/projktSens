cmake_minimum_required(VERSION 3.10.2)

project("weather-app")

add_library(weather-app
        SHARED
        app.h
        app.cpp
        AstroSunInfoProvider.h
        AstroSunInfoProvider.cpp
        AstroMoonInfoProvider.h
        AstroMoonInfoProvider.cpp
        vmath_arm.h

        v_sinf_impl.cpp
        v_cosf_impl.cpp
        v_asinf_impl.cpp
        )

set(BUILD_TESTING OFF)
#add_subdirectory(libs/cpu_features)

target_link_libraries(weather-app)
