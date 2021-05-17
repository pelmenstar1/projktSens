package com.pelmenstar.projktSens.shared.android.ui.requestPermissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.shared.android.Intent
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.ui.*

class RequestPermissionsActivity : AppCompatActivity() {
    private lateinit var permContext: RequestPermissionsContext
    private lateinit var currentPermission: RequestPermissionInfo
    private lateinit var descriptionView: TextView

    private var currentPermissionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < 23) {
            throw RuntimeException("Sdk int < 23")
        }

        super.onCreate(savedInstanceState)

        val intent = intent ?: throw IllegalStateException("Intent is null")
        val permContext = intent.getParcelableExtra<RequestPermissionsContext>(EXTRA_PERM_CONTEXT) ?: throw NullPointerException("$EXTRA_PERM_CONTEXT in intent is null")
        this.permContext = permContext

        if (savedInstanceState != null) {
            currentPermissionIndex = savedInstanceState.getInt(STATE_PERMISSION_INDEX, 0)
        }

        setContentView(createContent())

        currentPermission = permContext[currentPermissionIndex]
        showPermission(currentPermission)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PERMISSION_INDEX, currentPermissionIndex)
    }

    private fun requestCurrentPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            throw RuntimeException("Sdk int < 23")
        }

        requestPermissions(currentPermission.modePermissions.androidPermissions, PERMISSION_REQUEST_CODE)
    }

    private fun nextPermission() {
        val pr = permContext

        if (currentPermissionIndex >= pr.count - 1) {
            setResult(RESULT_OK, null)
            finish()
        } else {
            val perm = pr[currentPermissionIndex++]
            currentPermission = perm

            if (!shouldRequestPermission(this, perm)) {
                nextPermission()
            } else {
                showPermission(perm)
            }
        }
    }

    private fun showPermission(requestPermissionInfo: RequestPermissionInfo) {
        descriptionView.text = requestPermissionInfo.userDescription
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        nextPermission()
    }

    private fun createContent(): ViewGroup {
        val res = resources
        val headline5 = TextAppearance(this, R.style.TextAppearance_MaterialComponents_Headline5)
        val actionButtonSideMargin = res.getDimensionPixelOffset(R.dimen.requestPermissions_actionButtonSideMargin)

        return FrameLayout(this) {
            TextView {
                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(headline5)
                descriptionView = this
            }

            Button {
                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.START or Gravity.BOTTOM
                    leftMargin = actionButtonSideMargin
                }

                text = res.getText(R.string.requestPermission)
                setOnClickListener { requestCurrentPermission() }
            }

            Button {
                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.END or Gravity.BOTTOM
                    rightMargin = actionButtonSideMargin
                }

                text = res.getText(R.string.dontRequestPermission)
                setOnClickListener { nextPermission() }
            }
        }
    }

    companion object {
        private const val TAG = "RequestPermsActivity"
        private const val EXTRA_PERM_CONTEXT = "RequestPermissionsActivity:permContext"
        private const val STATE_PERMISSION_INDEX = "RequestPermissionsActivity:state_permission_index"

        private const val PERMISSION_REQUEST_CODE = 2

        fun intent(context: Context, permissionsContext: RequestPermissionsContext): Intent {
            return Intent(context, RequestPermissionsActivity::class.java) {
                putExtra(EXTRA_PERM_CONTEXT, permissionsContext)
            }
        }

        fun shouldStartActivity(context: Context, permContext: RequestPermissionsContext): Boolean {
            if (Build.VERSION.SDK_INT < 23) {
                return false
            }

            val pid = Process.myPid()
            val uid = Process.myUid()

            for(i in 0 until permContext.count) {
                if (shouldRequestPermission(context, permContext[i], pid, uid)) {
                    return true
                }
            }

            return false
        }

        private fun shouldRequestPermission(context: Context, permission: RequestPermissionInfo): Boolean {
            return shouldRequestPermission(context, permission, Process.myPid(), Process.myUid())
        }

        private fun shouldRequestPermission(
            context: Context,
            permission: RequestPermissionInfo,
            pid: Int,
            uid: Int
        ): Boolean {
            val modePermissions = permission.modePermissions

            when(val action = modePermissions.mode) {
                ModePermissionArray.MODE_EVERY -> {
                    for (s in modePermissions.androidPermissions) {
                        if (context.checkPermission(s, pid, uid) == PackageManager.PERMISSION_DENIED) {
                            return true
                        }
                    }

                    return false
                }

                ModePermissionArray.MODE_ANY -> {
                    for (s in modePermissions.androidPermissions) {
                        if (context.checkPermission(s, pid, uid) == PackageManager.PERMISSION_GRANTED) {
                            return false
                        }
                    }
                    return true
                }
                else -> {
                    Log.e(TAG, "Invalid action: $action")

                    return true
                }
            }
        }
    }
}