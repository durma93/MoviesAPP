package com.example.durma.moviesapp;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by durma on 30.1.18..
 */

public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

    GridLayoutManager gridLayoutManager;

    public PaginationScrollListener(GridLayoutManager gridLayoutManager) {
        this.gridLayoutManager = gridLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = gridLayoutManager.getChildCount();
        int totalItemCount = gridLayoutManager.getItemCount();
        int firstVisiblePosion = gridLayoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()){

            if ((visibleItemCount + firstVisiblePosion) >= totalItemCount
                    && firstVisiblePosion >= 0
                    && totalItemCount >= getTotalPageCount()) {
                loadMoreItems();
            }
        }

    }

    public abstract boolean isLoading();

    public abstract int getTotalPageCount();

    public abstract boolean isLastPage();

    public abstract void loadMoreItems();

}
