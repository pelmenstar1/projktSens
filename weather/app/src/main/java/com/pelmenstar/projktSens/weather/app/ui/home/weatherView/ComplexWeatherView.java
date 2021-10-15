package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;

import com.pelmenstar.projktSens.shared.time.ShortTime;
import com.pelmenstar.projktSens.shared.time.TimeConstants;
import com.pelmenstar.projktSens.shared.time.TimeInt;
import com.pelmenstar.projktSens.weather.app.R;
import com.pelmenstar.projktSens.weather.models.WeatherInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ComplexWeatherView extends View {
    public static final int STATE_DAY = 0;
    public static final int STATE_NIGHT = 1;

    private static final int DEFAULT_SUNRISE_TIME = 6 * TimeConstants.SECONDS_IN_HOUR;
    private static final int DEFAULT_SUNSET_TIME = 21 * TimeConstants.SECONDS_IN_HOUR;

    private final float weatherBlockMarginTopBottom;
    private final float sunriseSunsetArcHeight;

    @NotNull
    private final WeatherBlockSubcomponent weatherBlockSubcomponent;

    @NotNull
    private final RetryGetLocationSubcomponent retryGetLocationSubcomponent;

    @NotNull
    private final RequestLocationSubcomponent requestLocationSubcomponent;

    @NotNull
    private final SunriseSunsetArcSubcomponent sunriseSunsetArcSubcomponent;

    private final @NotNull Subcomponent @NotNull [] subcomponents;

    // if we left it zero, in constructor setState() would fail,
    // because 0 is STATE_DAY
    private int state = -1;
    private boolean isLocationLoaded;
    private boolean canLoadLocation = true;

    public ComplexWeatherView(@NotNull Context context) {
        this(context, null, 0, 0);
    }

    public ComplexWeatherView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ComplexWeatherView(
            @NotNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr
    ) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ComplexWeatherView(
            @NotNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Shadows is used in WeatherBlockSubcomponent.
        // But hardware acceleration for drawing shapes (except text) is supported only from API 28
        // So for any lower API level, hardware acceleration should be disabled
        if(Build.VERSION.SDK_INT < 28) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        Resources res = context.getResources();
        float density = res.getDisplayMetrics().density;

        weatherBlockMarginTopBottom = res.getDimension(R.dimen.weatherView_blockTopBottomMargin);
        sunriseSunsetArcHeight = res.getDimension(R.dimen.sunriseSunsetArc_height);
        float requestGpsSize = res.getDimension(R.dimen.weatherView_retrySize);

        weatherBlockSubcomponent = new WeatherBlockSubcomponent(context);
        retryGetLocationSubcomponent = new RetryGetLocationSubcomponent(context);

        sunriseSunsetArcSubcomponent = new SunriseSunsetArcSubcomponent(context);
        sunriseSunsetArcSubcomponent.setHeight(sunriseSunsetArcHeight);

        requestLocationSubcomponent = new RequestLocationSubcomponent(context);
        requestLocationSubcomponent.setSize(requestGpsSize, requestGpsSize);

        subcomponents = new Subcomponent[]{
                weatherBlockSubcomponent,
                retryGetLocationSubcomponent,
                requestLocationSubcomponent,
                sunriseSunsetArcSubcomponent
        };

        setState(STATE_DAY);
    }

    private void setState(int state) {
        if (this.state != state) {
            this.state = state;

            for (Subcomponent sc : subcomponents) {
                sc.setDayState(state);
            }

            updateComponentPositionsAffectedByDayState(getWidth(), getHeight());
            invalidate();
        }
    }

    @TimeInt
    public int getSunrise() {
        return sunriseSunsetArcSubcomponent.getSunrise();
    }

    @TimeInt
    public int getSunset() {
        return sunriseSunsetArcSubcomponent.getSunset();
    }

    public void setSunriseSunset(@TimeInt int sunrise, @TimeInt int sunset) {
        sunriseSunsetArcSubcomponent.setSunriseSunset(sunrise, sunset);

        updateDayState();
        invalidate();
    }

    @TimeInt
    public int getTime() {
        return sunriseSunsetArcSubcomponent.getTime();
    }

    public void setTime(@TimeInt int time) {
        sunriseSunsetArcSubcomponent.setTime(time);

        updateDayState();
        invalidate();
    }

    private void updateDayState() {
        int time = sunriseSunsetArcSubcomponent.getTime();
        int sunrise = sunriseSunsetArcSubcomponent.getSunrise();
        int sunset = sunriseSunsetArcSubcomponent.getSunset();

        int alignedSunrise = ShortTime.defaultIfNone(sunrise, DEFAULT_SUNRISE_TIME);
        int alignedSunset = ShortTime.defaultIfNone(sunset, DEFAULT_SUNSET_TIME);

        if (time >= alignedSunrise && time <= alignedSunset) {
            setState(STATE_DAY);
        } else {
            setState(STATE_NIGHT);
        }
    }

    public boolean isLocationLoaded() {
        return isLocationLoaded;
    }

    public void setLocationLoaded(boolean value) {
        isLocationLoaded = value;

        invalidateLocRelVisibility();
        invalidate();
    }

    public boolean canLoadLocation() {
        return canLoadLocation;
    }

    public void setCanLoadLocation(boolean value) {
        canLoadLocation = value;

        invalidateLocRelVisibility();
        invalidate();
    }

    private void invalidateLocRelVisibility() {
        if (canLoadLocation) {
            requestLocationSubcomponent.setVisible(false);
            retryGetLocationSubcomponent.setVisible(!isLocationLoaded);
        } else {
            retryGetLocationSubcomponent.setVisible(false);
            requestLocationSubcomponent.setVisible(true);
        }
    }

    @Nullable
    public RetryGetLocationSubcomponent.OnRetryGetLocationListener getOnRetryGetLocationListener() {
        return retryGetLocationSubcomponent.getOnRetryGetLocationListener();
    }

    public void setOnRetryGetLocationListener(@Nullable RetryGetLocationSubcomponent.OnRetryGetLocationListener listener) {
        retryGetLocationSubcomponent.setOnRetryGetLocationListener(listener);
    }

    @Nullable
    public RequestLocationSubcomponent.RequestLocationPermissionHandler getRequestLocationPermissionHandler() {
        return requestLocationSubcomponent.getRequestLocationPermissionHandler();
    }

    public void setRequestLocationPermissionHandler(@Nullable RequestLocationSubcomponent.RequestLocationPermissionHandler handler) {
        requestLocationSubcomponent.setRequestLocationPermissionHandler(handler);
    }

    public void setWeather(@NotNull WeatherInfo value) {
        weatherBlockSubcomponent.setWeather(value);

        invalidate();
    }

    private void updateComponentPositionsAffectedByDayState(float w, float h) {
        if(w > 0f && h > 0f) {
            float wbHeight = h - sunriseSunsetArcHeight;
            float retryX = w - retryGetLocationSubcomponent.getWidth();
            float reqLocX = w - requestLocationSubcomponent.getWidth();

            sunriseSunsetArcSubcomponent.setWidth(w);

            float blockTopOffset;

            if (state == STATE_DAY) {
                sunriseSunsetArcSubcomponent.setY(0f);

                blockTopOffset = sunriseSunsetArcHeight;

                weatherBlockSubcomponent.setY(sunriseSunsetArcHeight + weatherBlockMarginTopBottom);

                retryGetLocationSubcomponent.setPosition(retryX, sunriseSunsetArcHeight);
                requestLocationSubcomponent.setPosition(reqLocX, sunriseSunsetArcHeight);
            } else {
                sunriseSunsetArcSubcomponent.setY(wbHeight);

                blockTopOffset = 0f;

                weatherBlockSubcomponent.setY(weatherBlockMarginTopBottom);

                retryGetLocationSubcomponent.setPosition(retryX, 0);
                requestLocationSubcomponent.setPosition(reqLocX, 0);
            }

            float blockWidth = w * 0.7f;
            float blockHeight = wbHeight - (weatherBlockMarginTopBottom * 2);

            weatherBlockSubcomponent.setPosition(
                    0.5f * (w - blockWidth),
                    blockTopOffset + weatherBlockMarginTopBottom + 0.5f * (wbHeight - blockHeight)
            );
            weatherBlockSubcomponent.setSize(blockWidth, blockHeight);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateComponentPositionsAffectedByDayState(w, h);
    }

    @Override
    protected void onDraw(@NotNull Canvas c) {
        for (Subcomponent sc : subcomponents) {
            if (sc.isVisible) {
                sc.draw(c);
            }
        }
    }

    @Override
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            performClick();

            float x = event.getX();
            float y = event.getY();

            for (Subcomponent sc : subcomponents) {
                if (sc.isVisible) {
                    float scLeft = sc.x;
                    float scTop = sc.y;
                    float scRight = scLeft + sc.width;
                    float scBottom = scTop + sc.height;

                    if ((x >= scLeft && x <= scRight) && (y >= scTop && y <= scBottom)) {
                        sc.onClick();
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public static abstract class Subcomponent {
        private float x;
        private float y;
        private float width;
        private float height;
        private int dayState = ComplexWeatherView.STATE_DAY;
        private boolean isVisible = true;

        public final float getX() {
            return x;
        }

        public final void setX(float x) {
            this.x = x;
            onPositionChanged(x, y);
        }

        public final float getY() {
            return y;
        }

        public final void setY(float y) {
            this.y = y;
            onPositionChanged(x, y);
        }

        public final float getWidth() {
            return width;
        }

        public final void setWidth(float width) {
            this.width = width;
            onSizeChanged(width, height);
        }

        public final float getHeight() {
            return height;
        }

        public final void setHeight(float height) {
            this.height = height;
            onSizeChanged(width, height);
        }

        public final void setPosition(float x, float y) {
            this.x = x;
            this.y = y;

            onPositionChanged(x, y);
        }

        public final void setSize(float width, float height) {
            this.width = width;
            this.height = height;
            onSizeChanged(width, height);
        }

        public final int getDayState() {
            return dayState;
        }

        public final void setDayState(int dayState) {
            this.dayState = dayState;

            onDayStateChanged(dayState);
        }

        public final boolean isVisible() {
            return isVisible;
        }

        public final void setVisible(boolean visible) {
            isVisible = visible;
        }

        public abstract void draw(@NotNull Canvas canvas);

        protected abstract void onSizeChanged(float width, float height);

        protected abstract void onPositionChanged(float x, float y);

        protected abstract void onDayStateChanged(int newState);

        protected abstract void onClick();
    }
}
