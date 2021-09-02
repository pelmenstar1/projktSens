package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.content.Context
import android.content.Intent
import android.graphics.drawable.RotateDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
import androidx.viewpager2.widget.ViewPager2
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent

class FirstStartActivity : AppCompatActivity(), FirstStartContract.View {
    override val context: Context
        get() = this

    private lateinit var screenTitleView: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: FirstStartAdapter

    private lateinit var prevScreenButton: Button
    private lateinit var nextScreenButton: Button

    private lateinit var presenter: FirstStartContract.Presenter

    private var isFirstScreen = false
    private var isLastScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar { hide() }

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
        viewPagerAdapter.views = p.screenViews
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

    private fun createContent(): View {
        val context = this
        val res = resources
        val theme = theme

        return FrameLayout(context) {
            val actionButtonMargin =
                res.getDimensionPixelOffset(R.dimen.firstStartActivity_actionButtonMargin)
            val actionButtonSize =
                res.getDimensionPixelSize(R.dimen.firstStartActivity_actionButtonSize)

            LinearLayout {
                orientation = LinearLayout.VERTICAL

                screenTitleView = TextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.CENTER_HORIZONTAL
                        topMargin =
                            res.getDimensionPixelOffset(R.dimen.firstStartActivity_titleTopMargin)
                    }

                    applyTextAppearance(R.style.TextAppearance_MaterialComponents_Headline5)
                }


                viewPager = addApply(ViewPager2(context)) {
                    linearLayoutParams(MATCH_PARENT, MATCH_PARENT) {
                        setMargins(res.getDimensionPixelOffset(R.dimen.firstStartActivity_screenPadding))
                    }

                    registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            presenter.onScreenChangedByUser(position)
                            screenTitleView.text = presenter.getScreenTitleAt(position)
                        }
                    })

                    viewPagerAdapter = FirstStartAdapter()
                    adapter = viewPagerAdapter
                }
            }

            val nextDrawable = ResourcesCompat.getDrawable(res, R.drawable.ic_next, theme)!!

            prevScreenButton = Button {
                frameLayoutParams(actionButtonSize, actionButtonSize) {
                    gravity = Gravity.START or Gravity.BOTTOM

                    leftMargin = actionButtonMargin
                    bottomMargin = actionButtonMargin
                }

                background = RotateDrawable().apply {
                    drawable = nextDrawable.constantState?.newDrawable()
                    rotation = 180f
                }

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

                background = nextDrawable
                setOnClickListener { nextScreenOrFinish() }
            }
        }
    }

    override fun setPosition(position: Int, screen: FirstStartScreen<*>, withAnimation: Boolean) {
        viewPager.setCurrentItem(position, withAnimation)
        screenTitleView.text = resources.getText(screen.getTitleId())
    }

    override fun setCurrentScreenFlags(first: Boolean, last: Boolean) {
        prevScreenButton.visibility = if(first) View.INVISIBLE else View.VISIBLE

        isFirstScreen = first
        isLastScreen = last
    }

    override fun setCurrentStateValid(value: Boolean) {
        nextScreenButton.isEnabled = value
    }

    private fun nextScreenOrFinish() {
        val p = presenter
        if (isLastScreen) {
           finishActivityWithOkResult()
        } else {
            p.nextScreen()
        }
    }

    private fun finishActivityWithOkResult() {
        presenter.onFinish()
        setResult(RESULT_OK)
        finish()
    }

    private fun previousScreenOrFinish() {
        val p = presenter
        if (isFirstScreen) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            p.previousScreen()
        }
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, FirstStartActivity::class.java)
        }
    }
}