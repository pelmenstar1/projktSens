@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.text.method.TransformationMethod
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.obtainStyledAttributes
import java.util.*

class TextAppearance(context: Context, @StyleRes id: Int) {
    private val textColor: ColorStateList?
    private val textColorHint: ColorStateList?
    private val textColorLink: ColorStateList?
    private val allCaps: AllCapsTransformationMethod?

    @ColorInt
    private val shadowColor: Int
    private val shadowDx: Float
    private val shadowDy: Float
    private val shadowRadius: Float
    private val letterSpacing: Float
    private val textSize: Float
    private val font: Typeface?

    init {
        val theme = context.theme

        theme.obtainStyledAttributes(id, R.styleable.TextAppearance) { a ->
            textSize = a.getDimensionPixelOffset(R.styleable.TextAppearance_android_textSize, 0).toFloat()
            textColor = a.getColorStateList(R.styleable.TextAppearance_android_textColor)
            textColorHint = a.getColorStateList(R.styleable.TextAppearance_android_textColorHint)
            textColorLink = a.getColorStateList(R.styleable.TextAppearance_android_textColorLink)

            allCaps = if (a.getBoolean(R.styleable.TextAppearance_textAllCaps, false)) {
                AllCapsTransformationMethod(context)
            } else {
                null
            }

            shadowColor = a.getColor(R.styleable.TextAppearance_android_shadowColor, Color.TRANSPARENT)
            shadowDx = a.getFloat(R.styleable.TextAppearance_android_shadowDx, 0f)
            shadowDy = a.getFloat(R.styleable.TextAppearance_android_shadowDy, 0f)
            shadowRadius = a.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0f)

            theme.obtainStyledAttributes(id, R.styleable.MaterialTextAppearance) { materialAttrs ->
                letterSpacing = materialAttrs.getFloat(R.styleable.MaterialTextAppearance_android_letterSpacing, Float.NaN)
            }

            val fontFamilyIndex = if (a.hasValue(R.styleable.TextAppearance_fontFamily))
                R.styleable.TextAppearance_fontFamily
            else
                R.styleable.TextAppearance_android_fontFamily

            val fontFamilyResourceId = a.getResourceId(fontFamilyIndex, 0)
            val fontFamily = a.getString(fontFamilyIndex)

            val textStyle = a.getInt(R.styleable.TextAppearance_android_textStyle, Typeface.NORMAL)

            var font: Typeface? = null
            if(fontFamilyResourceId != 0) {
                try {
                    val family = ResourcesCompat.getFont(context, fontFamilyResourceId)

                    if (family != null) {
                        font = Typeface.create(family, textStyle)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading font $fontFamily", e)
                }
            }

            if (font == null && fontFamily != null) {
                font = Typeface.create(fontFamily, textStyle)
            }

            // Try resolving typeface if specified otherwise fallback to Typeface.DEFAULT.
            if (font == null) {
                val typeface = a.getInt(R.styleable.TextAppearance_android_typeface, TYPEFACE_SANS)

                font = Typeface.create(when (typeface) {
                    TYPEFACE_SANS -> Typeface.SANS_SERIF
                    TYPEFACE_SERIF -> Typeface.SERIF
                    TYPEFACE_MONOSPACE -> Typeface.MONOSPACE
                    else -> Typeface.DEFAULT
                }, textStyle)
            }

            this.font = font
        }
    }

    fun apply(textView: TextView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

        if (textColor != null) {
            textView.setTextColor(textColor)
        }

        if (shadowColor != Color.TRANSPARENT) {
            textView.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)
        }

        if (textColorHint != null) {
            textView.setHintTextColor(textColorHint)
        }

        if (textColorLink != null) {
            textView.setLinkTextColor(textColorLink)
        }

        if (!letterSpacing.isNaN()) {
            textView.letterSpacing = letterSpacing
        }

        if (font != null) {
            textView.typeface = font
        }

        val allCaps = allCaps
        if (allCaps != null) {
            textView.transformationMethod = allCaps
        }
    }

    private class AllCapsTransformationMethod(context: Context) : TransformationMethod {
        private val locale: Locale

        init {
            val conf = context.resources.configuration

            locale = if(Build.VERSION.SDK_INT >= 24) {
                conf.locales[0]
            } else {
                conf.locale
            }
        }

        override fun getTransformation(source: CharSequence?, view: View?): CharSequence? {
            if(source == null) {
                return null
            }

            return source.toString().uppercase(locale)
        }

        override fun onFocusChanged(
            view: View?, sourceText: CharSequence?, focused: Boolean,
            direction: Int, previouslyFocusedRect: Rect?
        ) {
        }

    }

    companion object {
        private const val TAG = "TextAppearance"

        // Enums from AppCompatTextHelper.
        private const val TYPEFACE_SANS = 1
        private const val TYPEFACE_SERIF = 2
        private const val TYPEFACE_MONOSPACE = 3
    }
}

inline fun TextView.applyTextAppearance(a: TextAppearance) {
    a.apply(this)
}

inline fun TextView.applyTextAppearance(@StyleRes id: Int) {
    applyTextAppearance(TextAppearance(context, id))
}