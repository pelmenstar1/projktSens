package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.pelmenstar.projktSens.shared.android.ui.MATCH_PARENT

class FirstStartAdapter : RecyclerView.Adapter<FirstStartAdapter.ViewHolder>() {
    var views: Array<out View>? = null

    override fun getItemCount(): Int = views?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = FrameLayout(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(private val layout: FrameLayout) : RecyclerView.ViewHolder(layout) {
        fun bind(position: Int) {
            val views = views

            if(views != null) {
                layout.removeAllViewsInLayout()
                layout.addView(views[position])
            }
        }
    }
}