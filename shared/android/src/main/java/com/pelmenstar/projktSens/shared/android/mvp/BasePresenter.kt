package com.pelmenstar.projktSens.shared.android.mvp

import android.content.Context
import android.os.Bundle

/**
 * Incomplete implementation of [DefaultContract.Presenter]
 */
abstract class BasePresenter<TView : DefaultContract.View> : DefaultContract.Presenter<TView> {
    @Volatile
    private var _view: TView? = null

    protected val view: TView
        get() = synchronized(this) {
            return _view ?: throw RuntimeException("Presenter is not attached to any view")
        }

    override fun attach(view: TView) {
        synchronized(this) {
            if(_view != null) {
                throw IllegalStateException("Presenter already has attached view")
            }

            _view = view
        }
    }

    override fun detach() {
        _view = null
    }

    val context: Context
        get() = view.context

    override fun saveState(outState: Bundle) {}
    override fun restoreState(state: Bundle) {}
}