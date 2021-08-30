package com.pelmenstar.projktSens.shared.android.ui.settings

import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.RoundRectDrawable
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.getChars

internal class GroupInflateOptions(context: Context) {
    val backgroundColor: Int
    val backgroundRadius: Float
    val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    val padding: Int

    init {
        val res = context.resources
        val theme = context.theme

        val groupBgValue = TypedValue()
        val resolved = theme.resolveAttribute(R.attr.settings_blockBackgroundColor, groupBgValue, true)

        backgroundColor = if(resolved) {
            groupBgValue.data
        } else {
            ResourcesCompat.getColor(res, R.color.settings_defaultGroupBackground, theme)
        }
        backgroundRadius = res.getDimension(R.dimen.settings_groupBackgroundRadius)

        padding = res.getDimensionPixelOffset(R.dimen.settings_groupPadding)
    }
}

internal fun createGroupLayout(context: Context, group: SettingGroup, options: GroupInflateOptions): ViewGroup {
    return inflateSettings(context, group.items).apply {
        layoutParams = options.layoutParams

        setPadding(options.padding)

        background = RoundRectDrawable(options.backgroundColor, options.backgroundRadius, true)
    }
}

internal fun inflateSettings(context: Context, settings: Array<out Setting<*>>): ViewGroup {
    val res = context.resources
    val body1 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

    return GridLayout(context).apply {
        frameLayoutParams(MATCH_PARENT, MATCH_PARENT)

        columnCount = 2
        rowCount = settings.size

        val settingNameSpec = GridLayout.spec(0, GridLayout.START)
        val viewSpec = GridLayout.spec(1, GridLayout.END)

        for (i in settings.indices) {
            val setting = settings[i]

            val rowSpec = GridLayout.spec(i)
            TextView {
                gridLayoutParams(
                    rowSpec,
                    columnSpec = settingNameSpec
                )

                applyTextAppearance(body1)

                val name = res.getString(setting.nameId)
                val nameLength = name.length

                val buffer = CharArray(nameLength + 1)
                name.getChars(0, nameLength, buffer, 0)
                buffer[nameLength] = ':'

                setText(buffer, 0, buffer.size)
            }

            addView(setting.createView(context).apply {
                gridLayoutParams(
                    rowSpec,
                    columnSpec = viewSpec
                )
            })
        }
    }
}