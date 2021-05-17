package com.pelmenstar.projktSens.shared.android.ui

import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

/**
 * [AppCompatActivity] that makes user able to click back button in [androidx.appcompat.app.ActionBar]
 */
open class HomeButtonSupportActivity : AppCompatActivity {
    constructor() : super()
    constructor(@LayoutRes resId: Int) : super(resId)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}