package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.textview.MaterialTextView;
import com.pelmenstar.projktSens.shared.EmptyArray;
import com.pelmenstar.projktSens.shared.android.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link AppCompatTextView} with prefix and value
 */
public final class PrefixTextView extends MaterialTextView {
    @NotNull
    private String prefix = "";

    @NotNull
    private String value = "";

    private char @NotNull [] valueBuffer = EmptyArray.CHAR;

    public PrefixTextView(@NotNull Context context) {
        this(context, null, android.R.attr.textViewStyle, 0);
    }

    public PrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle, 0);
    }

    public PrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PrefixTextView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PrefixTextView, defStyleAttr, defStyleRes);

            String tempPrefix = a.getString(R.styleable.PrefixTextView_prefix);

            if (tempPrefix != null) {
                prefix = tempPrefix;
            }

            String tempValue = a.getString(R.styleable.PrefixTextView_value);
            if (tempValue != null) {
                value = tempValue;
            }

            invalidateText();

            a.recycle();
        }
    }

    /**
     * Sets prefix
     */
    public void setPrefix(@NotNull String prefix) {
        this.prefix = prefix;

        invalidateText();
    }

    /**
     * Sets value
     */
    public void setValue(@NotNull String value) {
        this.value = value;

        invalidateText();
    }

    public void setValue(char @NotNull [] valueBuffer) {
        this.valueBuffer = valueBuffer;

        invalidateText();
    }

    public void setPrefixAndValue(@NotNull String prefix, @NotNull String value) {
        this.prefix = prefix;
        this.value = value;

        invalidateText();
    }

    public void setPrefixAndValue(@NotNull String prefix, char @NotNull [] valueBuffer) {
        this.prefix = prefix;
        this.valueBuffer = valueBuffer;

        invalidateText();
    }

    private char[] textCache = EmptyArray.CHAR;

    private void invalidateText() {
        char[] valueBuffer = this.valueBuffer;

        int prefixLength = prefix.length();
        int valueLength;
        if (valueBuffer.length > 0) {
            valueLength = valueBuffer.length;
        } else {
            valueLength = value.length();
        }

        int valueBegin = prefixLength + 2;
        int textLength = valueBegin + valueLength;
        char[] buffer;

        if (textCache.length == textLength) {
            buffer = textCache;
        } else {
            textCache = buffer = new char[textLength];
        }

        prefix.getChars(0, prefixLength, buffer, 0);
        buffer[prefixLength] = ':';
        buffer[prefixLength + 1] = ' ';

        if (valueBuffer.length > 0) {
            System.arraycopy(valueBuffer, 0, buffer, valueBegin, valueBuffer.length);
        } else {
            value.getChars(0, valueLength, buffer, valueBegin);
        }

        setText(buffer, 0, textLength);
    }
}
