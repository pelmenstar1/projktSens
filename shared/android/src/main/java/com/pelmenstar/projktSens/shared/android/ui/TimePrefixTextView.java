package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
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

    @Nullable
    private CharacterStyle timeStyle;

    private char @NotNull [] textCache = EmptyArray.CHAR;

    public TimePrefixTextView(@NotNull Context context) {
        this(context, null, android.R.attr.textViewStyle, 0);
    }

    public TimePrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle, 0);
    }

    public TimePrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePrefixTextView(
            @NotNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimePrefixTextView, defStyleAttr, defStyleRes);

            try {
                String tempPrefix = a.getString(R.styleable.TimePrefixTextView_prefix);
                if (tempPrefix != null) {
                    prefix = tempPrefix;
                }

                time = a.getInt(R.styleable.TimePrefixTextView_time, 0);

                if (!ShortTime.isValid(time)) {
                    throw new IllegalStateException("Invalid attributes");
                }

                updateText();
            } finally {
                a.recycle();
            }
        }
    }

    @Nullable
    public CharacterStyle getTimeStyle() {
        return timeStyle;
    }

    public void setTimeStyle(@Nullable CharacterStyle style) {
        timeStyle = style;
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(@NotNull String prefix) {
        this.prefix = prefix;

        updateText();
    }

    @TimeInt
    public int getTime() {
        return time;
    }

    public void setTime(@TimeInt int time) {
        if (this.time == time) {
            return;
        }

        if (!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        this.time = time;

        updateText();
    }

    private void updateText() {
        char[] buffer = textCache;

        int prefixLength = prefix.length();
        int bufferLength = prefixLength + 10;

        if(bufferLength > textCache.length) {
            textCache = buffer = new char[bufferLength];
        }

        prefix.getChars(0, prefixLength, buffer, 0);
        buffer[prefixLength] = ':';
        buffer[prefixLength + 1] = ' ';

        int timeStart = prefixLength + 2;
        ShortTime.writeToCharBuffer(time, buffer, timeStart);

        if(timeStyle != null) {
            String str = new String(buffer, 0, bufferLength);

            SpannableString s = new SpannableString(str);
            s.setSpan(timeStyle, timeStart, bufferLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            setText(s, BufferType.SPANNABLE);
        } else {
            setText(buffer, 0, bufferLength);
        }
    }
}
