package com.pelmenstar.projktSens.shared.android.ui.initScreen

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.pelmenstar.projktSens.shared.android.Message
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.TransparentDrawable
import com.pelmenstar.projktSens.shared.android.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

/**
 * [DialogFragment] which is responsible for long-term initializing some components of application
 */
open class InitScreen(private val initContext: InitContext) : DialogFragment() {
    private lateinit var errorView: MaterialTextView
    private lateinit var errorMnemonicView: MaterialTextView
    private lateinit var taskNameView: MaterialTextView
    private lateinit var retryButton: MaterialButton
    private lateinit var continueButton: MaterialButton
    private lateinit var circleView: TransitionView

    private val taskIndex = AtomicInteger()
    private var currentTask: InitTask? = null
    private var initJob: Job? = null

    private val pauseMutex = Mutex()

    /**
     * Calls when initializing ends
     */
    var onInitEnded: Runnable? = null

    private val mainThread: MainThreadHandler = MainThreadHandler(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        dialog?.window?.setBackgroundDrawable(TransparentDrawable.INSTANCE)

        isCancelable = false
        val view = createContent(context)

        runTasks()

        return view
    }

    private fun createContent(context: Context): View {
        val res = context.resources

        val headline4 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline4)
        val body1 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

        val retryButtonSize = res.getDimensionPixelSize(R.dimen.initScreen_retryButtonSize)
        val retryButtonTopMargin = res.getDimensionPixelSize(R.dimen.initScreen_retryButton_topMargin)
        val transitionCircleSize = res.getDimensionPixelSize(R.dimen.initScreen_transitionCircleSize)

        return LinearLayout(context) {
            orientation = LinearLayout.VERTICAL

            TransitionView {
                linearLayoutParams(transitionCircleSize, transitionCircleSize) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                startAnimation()
                circleView = this
            }

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(headline4)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                taskNameView = this
            }

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(headline4)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                visibility = View.GONE

                errorView = this
            }

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(body1)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                visibility = View.GONE

                errorMnemonicView = this
            }

            Button {
                linearLayoutParams(retryButtonSize, retryButtonSize) {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = retryButtonTopMargin
                }

                background = ResourcesCompat.getDrawable(res, R.drawable.ic_retry, context.theme)
                visibility = View.GONE
                setOnClickListener { retryFailedTask() }

                retryButton = this
            }

            Button {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                text = res.getText(R.string.continueWithoutThisTask)
                visibility = View.GONE
                setOnClickListener { continueWithoutFailedTask() }

                continueButton = this
            }
        }
    }

    private fun resumeTaskRunnerThread() {
        if(pauseMutex.isLocked) {
            pauseMutex.unlock()
        }
    }

    private fun changeVisualState(vs: Int) {
        when (vs) {
            VS_NORMAL -> {
                taskNameView.visibility = View.VISIBLE
                continueButton.visibility = View.GONE
                errorView.visibility = View.GONE
                errorMnemonicView.visibility = View.GONE
                retryButton.visibility = View.GONE
            }
            VS_ERROR_NO_CONTINUE -> {
                continueButton.visibility = View.GONE
                taskNameView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                retryButton.visibility = View.VISIBLE
            }
            VS_ERROR_WITH_CONTINUE -> {
                taskNameView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                retryButton.visibility = View.VISIBLE
                continueButton.visibility = View.VISIBLE
            }
        }
    }

    private fun postShowError(cause: Exception?) {
        mainThread.sendMessage(Message {
            what = MSG_SHOW_ERROR
            obj = cause
        })
    }

    private fun showError(cause: Exception?) {
        val task = currentTask ?: return

        val vs = if (task.isRequired) VS_ERROR_NO_CONTINUE else VS_ERROR_WITH_CONTINUE
        changeVisualState(vs)

        errorView.text = initContext.messageMapper.getErrorMessage(task.id)

        if (cause != null) {
            errorMnemonicView.run {
                visibility = View.VISIBLE
                text = cause.javaClass.simpleName
            }
        }
    }

    private fun retryFailedTask() {
        changeVisualState(VS_NORMAL)

        resumeTaskRunnerThread()
    }

    private fun continueWithoutFailedTask() {
        changeVisualState(VS_NORMAL)

        taskIndex.incrementAndGet()
        resumeTaskRunnerThread()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        exitProcess(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        endInit()
    }

    private fun postSetTaskName(name: CharSequence) {
        mainThread.sendMessage(Message {
            what = MSG_SET_TASK_NAME
            obj = name
        })
    }

    private fun setTaskName(name: CharSequence) {
        taskNameView.text = name
    }

    private fun postEndInit() {
        mainThread.sendMessage(Message { what = MSG_END_INIT })
    }

    private fun endInit() {
        circleView.stopAnimation()

        initJob?.cancel()
        initJob = null

        onInitEnded?.run()
        onInitEnded = null

        if (!isStateSaved) {
            dismiss()
        }

        mainThread.removeCallbacksAndMessages(null)
        mainThread.initScreen = null
    }

    private fun runTasks() {
        initJob = GlobalScope.launch(Dispatchers.Default) {
            val tasks = initContext.tasks
            val msgMapper = initContext.messageMapper

            while (isActive) {
                val index = taskIndex.get()
                if (index >= tasks.size) {
                    break
                }

                val task = tasks[index]
                val taskId = task.id
                currentTask = task

                postSetTaskName(msgMapper.getTaskName(taskId))

                var error: Exception? = null
                try {
                    val result: InitTask.Result

                    withTimeout(task.timeout.toLong()) {
                        result = task.run()
                    }

                    if(result == InitTask.Result.Ok) {
                        taskIndex.incrementAndGet()
                        continue
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "task $taskId fails", e)
                    error = e
                }

                postShowError(error)

                pauseMutex.lock()
            }
            postEndInit()
        }
    }

    private class MainThreadHandler(@JvmField var initScreen: InitScreen?) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val screen = initScreen ?: return

            when (msg.what) {
                MSG_SET_TASK_NAME -> screen.setTaskName(msg.obj as CharSequence)
                MSG_END_INIT -> screen.endInit()
                MSG_SHOW_ERROR -> screen.showError(msg.obj as Exception?)
            }
        }
    }

    companion object {
        private const val TAG = "InitScreen"

        private const val MSG_SET_TASK_NAME = 0
        private const val MSG_END_INIT = 1
        private const val MSG_SHOW_ERROR = 3

        private const val VS_NORMAL = 0
        private const val VS_ERROR_NO_CONTINUE = 1
        private const val VS_ERROR_WITH_CONTINUE = 2
    }

}