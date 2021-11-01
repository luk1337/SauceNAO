package com.luk.saucenao

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ResultsRecyclerView : RecyclerView {
    var emptyView: View? = null

    private val dataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            updateEmptyView()
        }
    }

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    override fun setAdapter(adapter: Adapter<*>?) {
        if (this.adapter != null) {
            this.adapter!!.unregisterAdapterDataObserver(dataObserver)
        }
        adapter?.registerAdapterDataObserver(dataObserver)
        super.setAdapter(adapter)
        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (adapter != null) {
            val showEmptyView = adapter!!.itemCount == 0
            emptyView?.visibility = if (showEmptyView) VISIBLE else GONE
            visibility = if (showEmptyView) GONE else VISIBLE
        }
    }
}