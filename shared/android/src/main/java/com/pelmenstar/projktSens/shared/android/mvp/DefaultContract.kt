package com.pelmenstar.projktSens.shared.android.mvp

import android.content.Context
import android.os.Bundle

/**
 * An ordinary interface of MVP app architecture
 */
interface DefaultContract {
    /**
     * An MVP presenter, which delegates all logical operations and brings changes to the [View]
     */
    interface Presenter<in TView : View> {
        /**
         * Attaches [TView] to presenter. If presenter already has attached view, call [detach] before.
         *
         * @throws IllegalStateException if some view has been already attached
         */
        fun attach(view: TView)

        /**
         * Detaches [TView] from presenter. After this method returns, presenter has no access to [TView]
         */
        fun detach()

        /**
         * Restores saved state from [Bundle]
         */
        fun restoreState(state: Bundle)

        /**
         * Saves state, which can be restored, to [Bundle]
         */
        fun saveState(outState: Bundle)
    }

    /**
     * An MVP view, which delegates all view-related operations
     */
    interface View {
        /**
         * Android context of application
         */
        val context: Context
    }
}