package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import androidx.core.view.setMargins
import com.pelmenstar.projktSens.shared.android.SlideBitmapView
import com.pelmenstar.projktSens.shared.android.setFloatValuesArray
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent

class FirstStartActivity : AppCompatActivity(), FirstStartContract.View {
    override val context: Context
        get() = this

    private lateinit var screenTitleView: TextView
    private lateinit var currentScreenPlaceholder: ViewGroup
    private lateinit var prevScreenButton: Button
    private lateinit var nextScreenButton: Button
    private var slideView: SlideBitmapView? = null

    private lateinit var presenter: FirstStartContract.Presenter

    private var isFirstScreen = false
    private var isLastScreen = false

    private var lastTouchX = 0f

    private var slideBitmap: Bitmap? = null
    private var slideBitmapCanvas: Canvas? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar { hide() }

        setContentView(createContent())

        val component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        presenter = component.firstStartPresenter().also {
            it.attach(this)
            if(savedInstanceState != null) {
                it.restoreState(savedInstanceState)
            }
            it.afterRestoredFromSavedState()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        slideBitmap?.recycle()
        slideBitmap = null

        slideBitmapCanvas = null
        slideView = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
            }

            MotionEvent.ACTION_UP -> {
                val delta = event.x - lastTouchX
                val slideMinDist = (currentScreenPlaceholder.width / 3).toFloat()

                if(delta > 0f && delta >= slideMinDist) {
                    presenter.previousScreen()
                } else if(delta < 0f && -delta >= slideMinDist) {
                    nextScreenOrFinish()
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        previousScreenOrFinish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        presenter.saveState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.detach()
    }

    private fun initSlideComponentsIfNull() {
        val placeholder = currentScreenPlaceholder

        if(slideBitmap == null) {
            val bitmapWidth = placeholder.width * 2
            val bitmapHeight = placeholder.height

            val bitmap =  Bitmap.createBitmap(
                bitmapWidth, bitmapHeight,
                Bitmap.Config.ARGB_8888,
            )

            slideBitmap = bitmap
            slideBitmapCanvas = Canvas(bitmap)
        }

        if(slideView == null) {
            slideView = SlideBitmapView(this).apply {
                frameLayoutParams(MATCH_PARENT, MATCH_PARENT)

                bitmap = slideBitmap
            }
        }
    }

    private fun createContent(): View {
        val context = this
        val res = resources
        val theme = theme

        return FrameLayout(context) {
            val actionButtonMargin = res.getDimensionPixelOffset(R.dimen.firstStartActivity_actionButtonMargin)
            val actionButtonSize = res.getDimensionPixelSize(R.dimen.firstStartActivity_actionButtonSize)

            LinearLayout {
                orientation = LinearLayout.VERTICAL

                screenTitleView = TextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.CENTER_HORIZONTAL
                        topMargin = res.getDimensionPixelOffset(R.dimen.firstStartActivity_titleTopMargin)
                    }

                    applyTextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline5)
                }

                currentScreenPlaceholder = FrameLayout {
                    linearLayoutParams(MATCH_PARENT, MATCH_PARENT) {
                        setMargins(resources.getDimensionPixelOffset(R.dimen.firstStartActivity_screenPadding))
                    }
                }
            }

            prevScreenButton = Button {
                frameLayoutParams(actionButtonSize, actionButtonSize) {
                    gravity = Gravity.START or Gravity.BOTTOM

                    leftMargin = actionButtonMargin
                    bottomMargin = actionButtonMargin
                }

                background = ResourcesCompat.getDrawable(res, R.drawable.ic_previous, theme)
                setOnClickListener {
                    presenter.previousScreen()
                }
            }

            nextScreenButton = Button {
                frameLayoutParams(actionButtonSize, actionButtonSize) {
                    gravity = Gravity.END or Gravity.BOTTOM

                    rightMargin = actionButtonMargin
                    bottomMargin = actionButtonMargin
                }

                background = ResourcesCompat.getDrawable(res, R.drawable.ic_next, theme)
                setOnClickListener { nextScreenOrFinish() }
            }
        }
    }

    override fun setScreenTitle(title: String) {
        screenTitleView.text = title
    }

    override fun setScreenView(view: View, oldPosition: Int, newPosition: Int) {
        val oldView = currentScreenPlaceholder.getChildAt(0)
        if(oldView != null) {
            setScreenViewWithAnimation(oldView, view, oldPosition, newPosition)
        } else {
            currentScreenPlaceholder.removeAllViewsInLayout()
            currentScreenPlaceholder.addView(view)
        }
    }

    override fun setCurrentScreenFlags(first: Boolean, last: Boolean) {
        prevScreenButton.isEnabled = !first

        isFirstScreen = first
        isLastScreen = last
    }

    override fun setCurrentStateValid(value: Boolean) {
        nextScreenButton.isEnabled = value
    }

    private fun nextScreenOrFinish() {
        val p = presenter
        if(isLastScreen) {
            p.onFinish()
            setResult(RESULT_OK)
            finish()
        } else {
            p.nextScreen()
        }
    }

    private fun previousScreenOrFinish() {
        val p = presenter
        if(isFirstScreen) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            p.previousScreen()
        }
    }

    private fun setScreenViewWithAnimation(
        oldView: View, newView: View,
        oldPosition: Int, newPosition: Int
    ) {
        val placeholder = currentScreenPlaceholder

        if (oldPosition == newPosition) {
            placeholder.removeAllViewsInLayout()
            placeholder.addView(newView)
            return
        }

        initSlideComponentsIfNull()
        val slideView = slideView!!

        placeholder.removeAllViewsInLayout()
        placeholder.addView(slideView)

        val placeholderWidth = placeholder.width
        val placeholderHeight = placeholder.height

        val widthSpec = View.MeasureSpec.makeMeasureSpec(placeholderWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(placeholderHeight, View.MeasureSpec.EXACTLY)

        oldView.measure(widthSpec, heightSpec)
        newView.measure(widthSpec, heightSpec)

        oldView.layout(0, 0, oldView.measuredWidth, oldView.measuredHeight)
        newView.layout(0, 0, newView.measuredWidth, newView.measuredHeight)

        val slideToLeft = newPosition > oldPosition
        updateSlideBitmap(oldView, newView, slideToLeft)

        val values = if (slideToLeft) {
            floatArrayOf(0f, -placeholderWidth.toFloat())
        } else {
            floatArrayOf(-placeholderWidth.toFloat(), 0f)
        }

        ValueAnimator().apply {
            addUpdateListener {
                slideView.offsetX = it.animatedValue as Float
            }

            setFloatValuesArray(values)

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    placeholder.removeAllViewsInLayout()
                    placeholder.addView(newView)
                }

                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}

            })
            duration = 200

            start()
        }
    }

    private fun updateSlideBitmap(
        oldView: View, newView: View,
        moveToLeft: Boolean
    ) {
        val width = currentScreenPlaceholder.width

        val canvas = slideBitmapCanvas!!
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        if(moveToLeft) {
            oldView.draw(canvas)

            canvas.withTranslation(x = width.toFloat()) {
                newView.draw(canvas)
            }
        } else {
            // then move to right
            newView.draw(canvas)
            canvas.withTranslation(x = width.toFloat()) {
                oldView.draw(canvas)
            }
        }
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, FirstStartActivity::class.java)
        }
    }
}