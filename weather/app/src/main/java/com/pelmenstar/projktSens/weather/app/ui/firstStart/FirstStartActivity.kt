package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
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

    private lateinit var presenter: FirstStartContract.Presenter

    private var isLastScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar {
            hide()
        }

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
        }
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
            val actionButtonMargin = res.getDimensionPixelOffset(R.dimen.firstStartActivity_actionButtonMargin)
            val actionButtonSize = res.getDimensionPixelSize(R.dimen.firstStartActivity_actionButtonSize)

            LinearLayout {
                orientation = android.widget.LinearLayout.VERTICAL

                screenTitleView = TextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.CENTER_HORIZONTAL
                        topMargin = res.getDimensionPixelOffset(R.dimen.firstStartActivity_titleTopMargin)
                    }

                    applyTextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline5)
                }

                currentScreenPlaceholder = FrameLayout {
                    linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
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
                setOnClickListener {
                    val p = presenter
                    if(isLastScreen) {
                        p.onFinish()
                        finish()
                    } else {
                        p.nextScreen()
                    }
                }
            }
        }
    }

    override fun setScreenTitle(title: String) {
        screenTitleView.text = title
    }

    override fun setScreenView(view: View) {
        currentScreenPlaceholder.removeAllViews()
        currentScreenPlaceholder.addView(view)
    }

    override fun setCurrentScreenFlags(first: Boolean, last: Boolean) {
        prevScreenButton.isEnabled = !first
        isLastScreen = last
    }

    override fun setCurrentStateValid(value: Boolean) {
        nextScreenButton.isEnabled = value
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, FirstStartActivity::class.java)
        }
    }
}