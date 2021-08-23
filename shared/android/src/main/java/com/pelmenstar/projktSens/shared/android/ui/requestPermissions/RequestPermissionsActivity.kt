package com.pelmenstar.projktSens.shared.android.ui.requestPermissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.shared.EmptyArray
import com.pelmenstar.projktSens.shared.add
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.ext.Intent
import com.pelmenstar.projktSens.shared.android.ext.getParcelableExtraNotNull
import com.pelmenstar.projktSens.shared.android.ui.requireIntent

class RequestPermissionsActivity : AppCompatActivity(R.layout.activity_request_permissions) {
    private lateinit var permContext: RequestPermissionsContext
    private lateinit var currentPermission: RequestPermissionInfo

    private lateinit var descriptionView: TextView
    private lateinit var whyTextView: TextView

    private var currentPermissionIndex = 0
    private var packedPermissionStates = EmptyArray.LONG

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < 23) {
            throw RuntimeException("Sdk int < 23")
        }

        super.onCreate(savedInstanceState)

        initViews()

        val intent = requireIntent()
        permContext = intent.getParcelableExtraNotNull(EXTRA_PERM_CONTEXT)

        if (savedInstanceState != null) {
            currentPermissionIndex = savedInstanceState.getInt(STATE_PERMISSION_INDEX, 0)
            packedPermissionStates = savedInstanceState.getLongArray(STATE_PERMISSION_STATES)
        }

        currentPermission = permContext[currentPermissionIndex]
        showPermission(currentPermission)
    }

    @RequiresApi(23)
    private fun initViews() {
        descriptionView = findViewById(R.id.requestPermissions_userDescription)
        whyTextView = findViewById(R.id.requestPermissions_whyText)

        findViewById<Button>(R.id.requestPermissions_requestButton).setOnClickListener {
            currentPermission.request(this, PERMISSION_REQUEST_CODE)
        }

        findViewById<Button>(R.id.requestPermissions_whyButton).setOnClickListener {
            showWhyTextForCurrentPermission()
        }
    }

    private fun showWhyTextForCurrentPermission() {
        whyTextView.apply {
            text = resources.getText(currentPermission.whyTextId)
            visibility = View.VISIBLE
        }
    }

    private fun hideWhyText() {
        whyTextView.visibility = View.GONE
    }

    private fun nextPermission() {
        val pr = permContext

        if (currentPermissionIndex >= pr.size - 1) {
            finishRequestingPermissions()
        } else {
            val permission = pr[currentPermissionIndex++]
            currentPermission = permission

            hideWhyText()

            if (!shouldRequestPermission(this, permission)) {
                nextPermission()
            } else {
                showPermission(permission)
            }
        }
    }

    private fun showPermission(requestPermissionInfo: RequestPermissionInfo) {
        descriptionView.text = resources.getText(requestPermissionInfo.userDescriptionId)
    }

    private fun finishRequestingPermissions() {
        val data = Intent().apply {
            putExtra(RETURN_DATA_PERMISSION_STATES, packedPermissionStates)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_PERMISSION_INDEX, currentPermissionIndex)
        outState.putLongArray(STATE_PERMISSION_STATES, packedPermissionStates)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val permInfo = permContext[currentPermissionIndex]
        val modePermissions = permInfo.modePermissions

        val actuallyGranted = if (modePermissions.mode == ModePermissionArray.MODE_ANY) {
            grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        } else {
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }

        val state = if(actuallyGranted) {
            PackageManager.PERMISSION_GRANTED
        } else {
            PackageManager.PERMISSION_DENIED
        }

        packedPermissionStates = packedPermissionStates.add(PackedPermissionState.create(permInfo.id, state))

        nextPermission()
    }

    companion object {
        private const val TAG = "RequestPermsActivity"
        private const val EXTRA_PERM_CONTEXT = "RequestPermissionsActivity:permContext"

        const val RETURN_DATA_PERMISSION_STATES =
            "RequestPermissionsActivity.returnData.permissionStates"

        private const val STATE_PERMISSION_INDEX =
            "RequestPermissionsActivity.state.permission_index"
        private const val STATE_PERMISSION_STATES =
            "RequestPermissionsActivity.state.permissionStates"

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

            return permContext.any { shouldRequestPermission(context, it, pid, uid) }
        }

        private fun shouldRequestPermission(
            context: Context,
            permission: RequestPermissionInfo
        ): Boolean {
            return shouldRequestPermission(context, permission, Process.myPid(), Process.myUid())
        }

        private fun shouldRequestPermission(
            context: Context,
            permission: RequestPermissionInfo,
            pid: Int,
            uid: Int
        ): Boolean {
            val modePermissions = permission.modePermissions

            return when (val action = modePermissions.mode) {
                ModePermissionArray.MODE_EVERY -> {
                    modePermissions.any {
                        context.checkPermission(it, pid, uid) == PackageManager.PERMISSION_DENIED
                    }
                }

                ModePermissionArray.MODE_ANY -> {
                    modePermissions.all {
                        context.checkPermission(it, pid, uid) == PackageManager.PERMISSION_DENIED
                    }
                }
                else -> {
                    Log.e(TAG, "Invalid action: $action")

                    true
                }
            }
        }
    }
}