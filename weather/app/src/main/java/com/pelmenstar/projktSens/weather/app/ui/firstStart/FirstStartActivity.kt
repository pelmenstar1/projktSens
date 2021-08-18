package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
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
    private lateinit var screenPlaceholder: ViewGroup
    private lateinit var prevScreenButton: Button
    private lateinit var nextScreenButton: Button
    private var slideView: SlideBitmapView? = null

    private lateinit var presenter: FirstStartContract.Presenter
    private var screenViews: Array<out View>? = null
    private var screenTitles: Array<out String>? = null

    private var isFirstScreen = false
    private var isLastScreen = false

    private var lastTouchX = 0f

    private var slideBitmap: Bitmap? = null
    private var slideBitmapCanvas: Canvas? = null

    private var backgroundColor = 0
    private var maxScreenHeight = 0

    private var bitmapNeedsToBeInvalided = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar { hide() }

        backgroundColor = surfaceBackgroundColor

        setContentView(createContent())

        val component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        val p = component.firstStartPresenter()
        presenter = p

        p.attach(this)
        if (savedInstanceState != null) {
            p.restoreState(savedInstanceState)
        }

        p.afterRestoredFromSavedState()

        maxScreenHeight = computeMaxHeightOfScreens()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        bitmapNeedsToBeInvalided = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
            }

            MotionEvent.ACTION_UP -> {
                val delta = event.x - lastTouchX
                val slideMinDist = (screenPlaceholder.width / 3).toFloat()

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

    private fun getScreenViewsOrInflate(): Array<out View> {
        var views = screenViews
        if(views == null) {
            views = presenter.inflateAllScreens()
            screenViews = views
        }

        return views
    }

    private fun getScreenTitlesOrCreate(): Array<out String> {
        var titles = screenTitles
        if(titles == null) {
            titles = presenter.getScreenTitles()
            screenTitles = titles
        }

        return titles
    }

    private fun computeMaxHeightOfScreens(): Int {
        val unspecified = View.MeasureSpec.makeMeasureSpec(
            0,
            View.MeasureSpec.UNSPECIFIED
        )

        val views = getScreenViewsOrInflate()

        var maxHeight = -1
        for(view in views) {
            view.measure(unspecified, unspecified)

            val height = view.measuredHeight
            if(height > maxHeight) {
                maxHeight = height
            }
        }

        return maxHeight
    }

    private fun initSlideComponents() {
        var sv = slideView
        val sb = slideBitmap
        val placeholder = screenPlaceholder

        var bitmapRefChanged = false
        if(sv == null) {
            sv = SlideBitmapView(this).apply {
                frameLayoutParams(MATCH_PARENT, MATCH_PARENT)

                slideCoefficient = 2
            }

            bitmapRefChanged = true
            slideView = sv
        }

        if(sb == null) {
            initSlideBitmapThroughRecreating(placeholder.width * 2)
        } else if(bitmapNeedsToBeInvalided) {
            bitmapNeedsToBeInvalided = false

            val oldWidth = sb.width
            val newWidth = placeholder.width * 2

            if(newWidth < oldWidth) {
                initSlideBitmapThroughReconfiguring(newWidth)
            } else {
                initSlideBitmapThroughRecreating(newWidth)
                bitmapRefChanged = true
            }
        }

        if(bitmapRefChanged) {
            sv.bitmap = slideBitmap
        }
    }

    private fun initSlideBitmapThroughRecreating(newWidth: Int) {
        slideBitmap?.recycle()
        val bitmap = Bitmap.createBitmap(
            newWidth, maxScreenHeight,
            Bitmap.Config.RGB_565
        )

        logBitmapInfo(bitmap)

        slideBitmap = bitmap
        slideBitmapCanvas = Canvas(bitmap)
    }

    private fun initSlideBitmapThroughReconfiguring(newWidth: Int) {
        val bitmap = slideBitmap ?: return

        bitmap.reconfigure(newWidth, maxScreenHeight, Bitmap.Config.RGB_565)

        logBitmapInfo(bitmap)

        slideBitmapCanvas = Canvas(bitmap)
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

                screenPlaceholder = FrameLayout {
                    linearLayoutParams(MATCH_PARENT, MATCH_PARENT) {
                        setMargins(res.getDimensionPixelOffset(R.dimen.firstStartActivity_screenPadding))
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

    override fun setPosition(oldPosition: Int, newPosition: Int) {
        val views = getScreenViewsOrInflate()

        val oldView = views[oldPosition]
        val newView = views[newPosition]

        setPositionViewWithAnimation(oldView, newView, oldPosition, newPosition)
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

    private fun setPositionViewWithAnimation(
        oldView: View, newView: View,
        oldPosition: Int, newPosition: Int
    ) {
        val placeholder = screenPlaceholder
        val titleView = screenTitleView
        val titles = getScreenTitlesOrCreate()
        val newTitle = titles[newPosition]

        if (oldPosition == newPosition) {
            placeholder.removeAllViewsInLayout()
            placeholder.addView(newView)
            titleView.text = newTitle

            return
        }

        initSlideComponents()
        val slideView = slideView!!

        placeholder.removeAllViewsInLayout()
        placeholder.addView(slideView)

        val placeholderWidth = placeholder.width

        newView.layout(0, 0, placeholderWidth, maxScreenHeight)

        val slideToLeft = newPosition > oldPosition
        updateSlideBitmap(oldView, newView, slideToLeft)
        slideView.onBitmapChanged()

        val bitmapAnimationValues = if (slideToLeft) {
            floatArrayOf(0f, -placeholderWidth.toFloat())
        } else {
            floatArrayOf(-placeholderWidth.toFloat(), 0f)
        }

        ObjectAnimator().apply {
            target = slideView
            setProperty(SlideBitmapView.OFFSET_X)
            setFloatValuesArray(bitmapAnimationValues)

            addListener(onEnd = {
                placeholder.removeAllViewsInLayout()
                placeholder.addView(newView)
            })
            duration = 200

            start()
        }
    }

    private fun updateSlideBitmap(
        oldView: View, newView: View,
        moveToLeft: Boolean
    ) {
        val width = screenPlaceholder.width

        val canvas = slideBitmapCanvas!!
        canvas.drawColor(backgroundColor)

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
        private const val TAG = "FirstStartActivity"

        private fun logBitmapInfo(b: Bitmap) {
            Log.i(TAG, "b.w=${b.width};b.h=${b.height};b.size=${b.byteCount}")
        }

        fun intent(context: Context): Intent {
            return Intent(context, FirstStartActivity::class.java)
        }
    }
}