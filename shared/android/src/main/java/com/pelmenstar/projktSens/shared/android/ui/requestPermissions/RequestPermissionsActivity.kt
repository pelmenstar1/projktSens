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
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.shared.EmptyArray
import com.pelmenstar.projktSens.shared.add
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.ext.Intent
import com.pelmenstar.projktSens.shared.android.ext.getIntArrayNotNull
import com.pelmenstar.projktSens.shared.android.ext.getParcelableExtraNotNull
import com.pelmenstar.projktSens.shared.android.ui.requireIntent

class RequestPermissionsActivity : AppCompatActivity(R.layout.activity_request_permissions) {
    private lateinit var permContext: RequestPermissionsContext
    private lateinit var currentPermission: RequestPermissionInfo

    private lateinit var descriptionView: TextView
    private lateinit var whyTextView: TextView

    private var currentPermissionIndex = 0
    private var grantedPermissionIndices = EmptyArray.INT
    private var deniedPermissionIndices = EmptyArray.INT

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
            grantedPermissionIndices =
                savedInstanceState.getIntArrayNotNull(STATE_GRANTED_PERMISSION_INDICES)
            deniedPermissionIndices =
                savedInstanceState.getIntArrayNotNull(STATE_DENIED_PERMISSION_INDICES)
        }

        currentPermission = permContext[currentPermissionIndex]
        showPermission(currentPermission)
    }

    private fun initViews() {
        descriptionView = findViewById(R.id.requestPermissions_userDescription)
        whyTextView = findViewById(R.id.requestPermissions_whyText)

        findViewById<Button>(R.id.requestPermissions_request).setOnClickListener {
            requestCurrentPermission()
        }
        findViewById<Button>(R.id.requestPermissions_dontRequest).setOnClickListener {
            doNotRequestCurrentPermission()
        }
        findViewById<Button>(R.id.requestPermissions_whyButton).setOnClickListener {
            showWhyTextForCurrentPermission()
        }
    }

    private fun showWhyTextForCurrentPermission() {
        val res = resources

        whyTextView.apply {
            text = res.getText(currentPermission.whyTextId)
            visibility = View.VISIBLE
        }
    }

    private fun hideWhyText() {
        whyTextView.visibility = View.GONE
    }

    private fun doNotRequestCurrentPermission() {
        deniedPermissionIndices = deniedPermissionIndices.add(currentPermissionIndex)
        nextPermission()
    }

    private fun requestCurrentPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            throw RuntimeException("Sdk int < 23")
        }

        requestPermissions(
            currentPermission.modePermissions.androidPermissions,
            PERMISSION_REQUEST_CODE
        )
    }

    private fun nextPermission() {
        val pr = permContext

        if (currentPermissionIndex >= pr.count - 1) {
            finishRequestingPermissions()
        } else {
            val perm = pr[currentPermissionIndex++]
            currentPermission = perm

            hideWhyText()

            if (!shouldRequestPermission(this, perm)) {
                nextPermission()
            } else {
                showPermission(perm)
            }
        }
    }

    private fun showPermission(requestPermissionInfo: RequestPermissionInfo) {
        val res = resources
        descriptionView.text = res.getText(requestPermissionInfo.userDescriptionId)
    }

    private fun finishRequestingPermissions() {
        val data = Intent().apply {
            putExtra(RETURN_DATA_GRANTED_PERMISSION_INDICES, grantedPermissionIndices)
            putExtra(RETURN_DATA_DENIED_PERMISSION_INDICES, deniedPermissionIndices)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_PERMISSION_INDEX, currentPermissionIndex)
        outState.putIntArray(STATE_GRANTED_PERMISSION_INDICES, grantedPermissionIndices)
        outState.putIntArray(STATE_DENIED_PERMISSION_INDICES, deniedPermissionIndices)
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

        if (actuallyGranted) {
            grantedPermissionIndices = grantedPermissionIndices.add(currentPermissionIndex)
        } else {
            deniedPermissionIndices = deniedPermissionIndices.add(currentPermissionIndex)
        }

        nextPermission()
    }

    companion object {
        private const val TAG = "RequestPermsActivity"
        private const val EXTRA_PERM_CONTEXT = "RequestPermissionsActivity:permContext"

        const val RETURN_DATA_GRANTED_PERMISSION_INDICES =
            "RequestPermissionsActivity.returnData.grantedPermissions"
        const val RETURN_DATA_DENIED_PERMISSION_INDICES =
            "RequestPermissionsActivity.returnData.deniedPermissions"

        private const val STATE_PERMISSION_INDEX =
            "RequestPermissionsActivity:state_permission_index"
        private const val STATE_GRANTED_PERMISSION_INDICES =
            "RequestPermissionsActivity:state_grantedPermissionIndices"
        private const val STATE_DENIED_PERMISSION_INDICES =
            "RequestPermissionsActivity:state_deniedPermissionIndices"

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

            for (i in 0 until permContext.count) {
                if (shouldRequestPermission(context, permContext[i], pid, uid)) {
                    return true
                }
            }

            return false
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

            when (val action = modePermissions.mode) {
                ModePermissionArray.MODE_EVERY -> {
                    for (s in modePermissions.androidPermissions) {
                        if (context.checkPermission(
                                s,
                                pid,
                                uid
                            ) == PackageManager.PERMISSION_DENIED
                        ) {
                            return true
                        }
                    }

                    return false
                }

                ModePermissionArray.MODE_ANY -> {
                    for (s in modePermissions.androidPermissions) {
                        if (context.checkPermission(
                                s,
                                pid,
                                uid
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
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