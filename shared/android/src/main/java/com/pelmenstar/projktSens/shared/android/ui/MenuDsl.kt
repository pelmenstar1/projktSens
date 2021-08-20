@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android.ui

import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class MenuBuilder(private val menu: Menu) {
    fun item(
        id: Int = 0,
        @StringRes titleRes: Int,
        showsAsAction: Int,
        @DrawableRes iconRes: Int = 0,
        onClick: MenuItem.OnMenuItemClickListener
    ) {
        menu.add(0, id, 0, titleRes).apply {
            setOnMenuItemClickListener(onClick)
            setShowAsAction(showsAsAction)

            if (iconRes != 0) {
                setIcon(iconRes)
            }
        }
    }

    fun item(
        id: Int = 0,
        title: CharSequence,
        showsAsAction: Int,
        icon: Drawable? = null,
        onClick: MenuItem.OnMenuItemClickListener
    ) {
        menu.add(0, id, 0, title).apply {
            setOnMenuItemClickListener(onClick)
            setShowAsAction(showsAsAction)

            if (icon != null) {
                setIcon(icon)
            }
        }
    }
}

inline fun Menu.add(block: MenuBuilder.() -> Unit) {
    MenuBuilder(this).also(block)
}