package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.google.android.material.textview.MaterialTextView;
import com.pelmenstar.projktSens.shared.android.R;
import com.pelmenstar.projktSens.shared.time.ShortTime;
import com.pelmenstar.projktSens.shared.time.TimeInt;

import org.jetbrains.annotations.NotNull;

/**
 * View that displays time to user
 */
public final class TimeTextView extends MaterialTextView {
    @TimeInt
    private int time = -1;

    private final char[] textCache = new char[8];

    public TimeTextView(@NotNull Context context) {
        this(context, null, android.R.attr.textViewStyle, 0);
    }

    public TimeTextView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle, 0);
    }

    public TimeTextView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimeTextView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeTextView, defStyleAttr, defStyleRes);

            try {
                int time = a.getInt(R.styleable.TimePrefixTextView_prefix, 0);
                if (!ShortTime.isValid(time)) {
                    throw new IllegalStateException("Invalid attributes");
                }

                setTimeInternal(time);
            } finally {
                a.recycle();
            }

            a.recycle();
        } else {
            setTimeInternal(0);
        }
    }

    /**
     * Gets time
     */
    @TimeInt
    public int getTime() {
        return time;
    }

    /**
     * Sets time
     *
     * @throws IllegalArgumentException if {@code time} is invalid ({@code time < 0 || time >= TimeConstants.SECONDS_IN_DAY})
     */
    public void setTime(@TimeInt int time) {
        if (this.time == time) {
            return; // no sense to rewrite the same time to text buffer
        }

        if (!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        setTimeInternal(time);
    }

    private void setTimeInternal(@TimeInt int time) {
        this.time = time;

        ShortTime.writeToCharBuffer(time, textCache, 0);
        setText(textCache, 0, 8);
    }
}
