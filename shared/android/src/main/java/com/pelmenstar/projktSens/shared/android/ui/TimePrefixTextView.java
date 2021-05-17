package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;


import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;

import com.google.android.material.textview.MaterialTextView;
import com.pelmenstar.projktSens.shared.EmptyArray;
import com.pelmenstar.projktSens.shared.android.R;
import com.pelmenstar.projktSens.shared.time.ShortTime;
import com.pelmenstar.projktSens.shared.time.TimeInt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * View that displays time with prefix to user
 */
public final class TimePrefixTextView extends MaterialTextView {
    @NotNull
    private String prefix = "";

    @TimeInt
    private int time;

    @NotNull
    private char[] textCache = EmptyArray.CHAR;

    public TimePrefixTextView(@NotNull Context context) {
        this(context, null, android.R.attr.textViewStyle, 0);
    }

    public TimePrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle, 0);
    }

    public TimePrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimePrefixTextView, defStyleAttr, defStyleRes);
            String prefix = a.getString(R.styleable.TimePrefixTextView_prefix);
            if(prefix != null) {
                setPrefix(prefix);
            }

            int time = a.getInt(R.styleable.TimePrefixTextView_time, 0);
            if(!ShortTime.isValid(time)) {
                throw new IllegalStateException("Invalid attributes");
            }

            setTimeInternal(time);

            a.recycle();
        }
    }

    /**
     * Gets prefix of view, not null
     */
    @NotNull
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets prefix, not null
     */
    public void setPrefix(@NotNull String prefix) {
        String oldPrefix = this.prefix;
        if(oldPrefix.length() != prefix.length()) {
            textCache = new char[prefix.length() + 10];
            char[] text = textCache;

            prefix.getChars(0, prefix.length(), text, 0);
            text[prefix.length()] = ':';
            text[prefix.length() + 1] = ' ';
            ShortTime.writeToCharBuffer(time, text, prefix.length() + 2);

            setText(text, 0, text.length);
        }

        this.prefix = prefix;
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
        if(this.time == time) {
            return;
        }

        if(!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        setTimeInternal(time);
    }

    private void setTimeInternal(@TimeInt int time)  {
        this.time = time;

        char[] text = textCache;
        ShortTime.writeToCharBuffer(time, text, prefix.length() + 2);

        setText(text, 0, text.length);
    }
}
