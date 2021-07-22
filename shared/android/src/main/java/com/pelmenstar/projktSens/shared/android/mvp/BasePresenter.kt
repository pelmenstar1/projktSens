package com.pelmenstar.projktSens.shared.android.mvp

import android.content.Context
import android.os.Bundle
import java.util.concurrent.atomic.AtomicReference

/**
 * Incomplete implementation of [DefaultContract.Presenter].
 * No code should be executed before [BasePresenter.attach], or after [BasePresenter.detach]
 */
abstract class BasePresenter<TView : DefaultContract.View> : DefaultContract.Presenter<TView> {
    private val _viewRef = AtomicReference<TView?>()

    protected val view: TView
        get() = _viewRef.get() ?: throw RuntimeException("Presenter is not attached to any view")

    override fun attach(view: TView) {
        if(!_viewRef.compareAndSet(null, view)) {
            throw IllegalStateException("Presenter already has attached view")
        }
    }

    override fun detach() {
        _viewRef.set(null)
    }

    val context: Context
        get() = view.context

    override fun saveState(outState: Bundle) {}
    override fun restoreState(state: Bundle) {}
}