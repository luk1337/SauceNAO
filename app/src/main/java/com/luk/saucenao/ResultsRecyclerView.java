package com.luk.saucenao;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ResultsRecyclerView extends RecyclerView {

    private View mEmptyView;

    private AdapterDataObserver mDataObserver = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            super.onChanged();

            updateEmptyView();
        }
    };

    public ResultsRecyclerView(Context context) {
        super(context);
    }

    public ResultsRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResultsRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(mDataObserver);
        }

        if (adapter != null) {
            adapter.registerAdapterDataObserver(mDataObserver);
        }

        super.setAdapter(adapter);
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (mEmptyView != null && getAdapter() != null) {
            boolean showEmptyView = getAdapter().getItemCount() == 0;

            mEmptyView.setVisibility(showEmptyView ? VISIBLE : GONE);
            setVisibility(showEmptyView ? GONE : VISIBLE);
        }
    }
}