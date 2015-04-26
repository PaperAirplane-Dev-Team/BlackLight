/*
 * Copyright (C) 2015 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.papdt.blacklight.support.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;

/**
 * An Adapter which adds HeaderView
 */
public abstract class HeaderViewAdapter<VH extends HeaderViewAdapter.ViewHolder> extends Adapter<VH> {
	private View mHeader = null;
	private RecyclerView mRecyclerView;
	protected RecyclerView.OnScrollListener mListener;
	protected List<RecyclerView.OnScrollListener> mListeners = new ArrayList<RecyclerView.OnScrollListener>();
	
	public HeaderViewAdapter(RecyclerView v) {
		mRecyclerView = v;
		mRecyclerView.setOnScrollListener((mListener = new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView rv, int newState) {
				for (RecyclerView.OnScrollListener listener : mListeners) {
					listener.onScrollStateChanged(rv, newState);
				}
			}
			
			@Override
			public void onScrolled(RecyclerView rv, int dx, int dy) {
				for (RecyclerView.OnScrollListener listener : mListeners) {
					listener.onScrolled(rv, dx, dy);
				}
			}
		}));
	}

	public void setHeaderView(View header) {
		mHeader = header;
		notifyDataSetChanged();
	}

	public View getHeaderView() {
		return mHeader;
	}
	
	public boolean hasHeaderView() {
		return mHeader != null;
	}
	
	public void addOnScrollListener(RecyclerView.OnScrollListener listener) {
		mListeners.add(listener);
	}
	
	public void notifyDataSetChangedAndClone() {
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		int count = getCount();

		if (mHeader != null) {
			count++;
		}

		return count;
	}

	@Override
	public int getItemViewType(int position) {
		if (mHeader != null) {
			if (position == 0) {
				return Integer.MIN_VALUE;
			} else {
				position--;
			}
		}

		return getViewType(position);
	}

	@Override
	public long getItemId(int position) {
		if (mHeader != null) {
			if (position == 0) {
				return 0;
			} else {
				position--;
			}
		}

		return getItemViewId(position);
	}

	@Override
	public void onViewRecycled(VH h) {
		if (!h.isHeader) {
			doRecycleView(h);
		}
	}

	@Override
	public VH onCreateViewHolder(ViewGroup parent, int viewType) {
		if (mHeader != null && viewType == Integer.MIN_VALUE) {
			return doCreateHeaderHolder(mHeader);
		} else {
			return doCreateViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(VH h, int position) {
		if (mHeader != null && position != 0) {
			position--;
		}

		if (!h.isHeader) {
			doBindViewHolder(h, position);
		}
	}

	abstract int getCount();
	abstract int getViewType(int position);
	abstract long getItemViewId(int position);
	abstract void doRecycleView(VH h);
	abstract VH doCreateViewHolder(ViewGroup parent, int viewType);
	abstract VH doCreateHeaderHolder(View header);
	abstract void doBindViewHolder(VH h, int position);

	public static abstract class ViewHolder extends RecyclerView.ViewHolder {
		public boolean isHeader = false;

		public ViewHolder(View v) {
			super(v);
		}
	}
}
