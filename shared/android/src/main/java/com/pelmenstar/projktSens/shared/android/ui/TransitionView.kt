package com.pelmenstar.projktSens.shared.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.obtainStyledAttributes
import kotlinx.coroutines.*
import java.util.*

/**
 * View that can be used for long-time operations
 */
class TransitionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var animationJob: Job? = null
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Sets or gets shape of view. Value should be one of these constants [TransitionView.SHAPE_CIRCLE], [TransitionView.SHAPE_RECT]
     */
    var shape = determineShape(context, attrs, defStyleAttr, defStyleRes)

    /**
     * Sets or gets current [LinearColorTransition]. If animation has been already started, stop and start to notice visual changes
     */
    @Volatile
    var transition: LinearColorTransition = defaultTransition.copy()

    private fun determineShape(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ): Int {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.TransitionView, defStyleAttr, defStyleRes) { a ->
                if (a.hasValue(R.styleable.TransitionView_shape)) {
                    val aShape = a.getInt(R.styleable.TransitionView_shape, SHAPE_CIRCLE)

                    if (aShape == SHAPE_CIRCLE || aShape == SHAPE_RECT) {
                        return aShape
                    } else {
                        Log.e(TAG, "Illegal attribute 'shape' value ($aShape)")
                    }
                }
            }
        }

        return random.nextInt(2)
    }

    /**
     * Starts animation
     */
    fun startAnimation() {
        if(animationJob != null) {
            Log.e(TAG, "Animation is already running")
        }

        animationJob = GlobalScope.launch(Dispatchers.Default) {
            val trans = transition

            while (isActive) {
                shapePaint.color = trans.nextColor()
                postInvalidate()

                delay(12)
            }
        }
    }

    /**
     * Stops animation
     */
    fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    public override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        when (shape) {
            SHAPE_CIRCLE -> {
                c.drawOval(0f, 0f, w, h, shapePaint)
            }
            SHAPE_RECT -> {
                c.drawRect(0f, 0f, w, h, shapePaint)
            }
        }
    }

    companion object {
        private val random = Random(0)
        private const val TAG = "TransitionCircleView"

        /**
         * Circle
         */
        const val SHAPE_CIRCLE = 0

        /**
         * Rectangle
         */
        const val SHAPE_RECT = 1

        /**
         * Sets or gets default [LinearColorTransition]. When [TransitionView] creates, its transition sets to default
         */
        @JvmStatic
        @Volatile
        var defaultTransition = LinearColorTransition.empty()
    }
}