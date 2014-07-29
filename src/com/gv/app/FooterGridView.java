/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gv.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import java.util.ArrayList;

/**
 * A {@link GridView} that supports adding header rows in a
 * very similar way to {@link ListView}.
 * See {@link FooterGridView#addFooterView(View, Object, boolean)}
 */
public class FooterGridView extends GridView {
    private static final String TAG = "HeaderGridView";
    
    private int mNumColsCompat = AUTO_FIT;
    private int mRequestedHorizontalSpacing;
    private int mRequestedNumColumns;
    private int mRequestedColumnWidth;

    /**
     * A class that represents a fixed view in a list, for example a header at the top
     * or a footer at the bottom.
     */
    private static class FixedViewInfo {
        /** The view to add to the grid */
        public View view;
        public ViewGroup viewContainer;
        /** The data backing the view. This is returned from {@link ListAdapter#getItem(int)}. */
        public Object data;
        /** <code>true</code> if the fixed view should be selectable in the grid */
        public boolean isSelectable;
    }

    private ArrayList<FixedViewInfo> mFooterViewInfos = new ArrayList<FixedViewInfo>();

    private void initHeaderGridView() {
        super.setClipChildren(false);
    }

    public FooterGridView(Context context) {
        super(context);
        initHeaderGridView();
    }

    public FooterGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderGridView();
    }

    public FooterGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initHeaderGridView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isHR()) {
        	final int available = MeasureSpec.getSize(widthMeasureSpec) - 
        			getPaddingLeft() - getPaddingRight();
        	onFigureColumns(available);
        }
        ListAdapter adapter = getAdapter();
        if (adapter != null && adapter instanceof FooterViewGridAdapter) {
            ((FooterViewGridAdapter) adapter).setNumColumns(getNumColumnsCompat());
        }
    }
    
    private static boolean isHR() {
    	return android.os.Build.VERSION.SDK_INT >= 
    			android.os.Build.VERSION_CODES.HONEYCOMB;
    }
    
    private int getNumColumnsCompat() {
    	if (isHR()) {
    		return getNumColumnsHC();
    	}
    	return mNumColsCompat;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    private int getNumColumnsHC() {
    	return getNumColumns();
    }

    @Override
    public void setHorizontalSpacing(int horizontalSpacing) {
    	super.setHorizontalSpacing(horizontalSpacing);
        if (horizontalSpacing != mRequestedHorizontalSpacing) {
        	mRequestedHorizontalSpacing = horizontalSpacing;
        }
    }
    
    @Override
    public void setColumnWidth(int columnWidth) {
    	super.setColumnWidth(columnWidth);
        if (columnWidth != mRequestedColumnWidth) {
            mRequestedColumnWidth = columnWidth;
        }
    }
    
    @Override
    public void setNumColumns(int numColumns) {
    	super.setNumColumns(numColumns);
        if (numColumns != mRequestedNumColumns) {
            mRequestedNumColumns = numColumns;
        }
    }
    
    private void onFigureColumns(int availableSpace) {
        final int requestedHorizontalSpacing = mRequestedHorizontalSpacing;
        final int requestedColumnWidth = mRequestedColumnWidth;
        
        if (mRequestedNumColumns == AUTO_FIT) {
            if (requestedColumnWidth > 0) {
                // Client told us to pick the number of columns
            	mNumColsCompat = (availableSpace + requestedHorizontalSpacing) /
                        (requestedColumnWidth + requestedHorizontalSpacing);
            } else {
                // Just make up a number if we don't have enough info
            	mNumColsCompat = 2;
            }
        } else {
            // We picked the columns
        	mNumColsCompat = mRequestedNumColumns;
        }
        
        if (mNumColsCompat <= 0) {
        	mNumColsCompat = 1;
        }
    }

    @Override
    public void setClipChildren(boolean clipChildren) {
       // Ignore, since the header rows depend on not being clipped
    }

    /**
     * Add a fixed view to appear at the top of the grid. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so HeaderGridView can wrap
     * the supplied cursor with one that will also account for header views.
     *
     * @param v The view to add.
     * @param data Data to associate with this view
     * @param isSelectable whether the item is selectable
     */
    public void addFooterView(View v, Object data, boolean isSelectable) {
        ListAdapter adapter = getAdapter();

        if (adapter != null && ! (adapter instanceof FooterViewGridAdapter)) {
            throw new IllegalStateException(
                    "Cannot add header view to grid -- setAdapter has already been called.");
        }

        FixedViewInfo info = new FixedViewInfo();
        FrameLayout fl = new FullWidthFixedViewLayout(getContext());
        fl.addView(v);
        info.view = v;
        info.viewContainer = fl;
        info.data = data;
        info.isSelectable = isSelectable;
        mFooterViewInfos.add(info);

        // in the case of re-adding a header view, or adding one later on,
        // we need to notify the observer
        if (adapter != null) {
            ((FooterViewGridAdapter) adapter).notifyDataSetChanged();
        }
    }

    /**
     * Add a fixed view to appear at the top of the grid. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so HeaderGridView can wrap
     * the supplied cursor with one that will also account for header views.
     *
     * @param v The view to add.
     */
    public void addFooterView(View v) {
        addFooterView(v, null, true);
    }

    public int getHeaderViewCount() {
        return mFooterViewInfos.size();
    }

    /**
     * Removes a previously-added header view.
     *
     * @param v The view to remove
     * @return true if the view was removed, false if the view was not a header
     *         view
     */
    public boolean removeFooterView(View v) {
        if (mFooterViewInfos.size() > 0) {
            boolean result = false;
            ListAdapter adapter = getAdapter();
            if (adapter != null && ((FooterViewGridAdapter) adapter).removeFooter(v)) {
                result = true;
            }
            removeFixedViewInfo(v, mFooterViewInfos);
            return result;
        }
        return false;
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        for (int i = 0; i < len; ++i) {
            FixedViewInfo info = where.get(i);
            if (info.view == v) {
                where.remove(i);
                break;
            }
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mFooterViewInfos.size() > 0) {
            FooterViewGridAdapter hadapter = new FooterViewGridAdapter(mFooterViewInfos, adapter);
            int numColumns = getNumColumnsCompat();
            if (numColumns > 1) {
                hadapter.setNumColumns(numColumns);
            }
            super.setAdapter(hadapter);
        } else {
            super.setAdapter(adapter);
        }
    }

    private class FullWidthFixedViewLayout extends FrameLayout {
        public FullWidthFixedViewLayout(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int targetWidth = FooterGridView.this.getMeasuredWidth()
                    - FooterGridView.this.getPaddingLeft()
                    - FooterGridView.this.getPaddingRight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(targetWidth,
                    MeasureSpec.getMode(widthMeasureSpec));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * ListAdapter used when a HeaderGridView has header views. This ListAdapter
     * wraps another one and also keeps track of the header views and their
     * associated data objects.
     *<p>This is intended as a base class; you will probably not need to
     * use this class directly in your own code.
     */
    private static class FooterViewGridAdapter implements WrapperListAdapter, Filterable {

        // This is used to notify the container of updates relating to number of columns
        // or headers changing, which changes the number of placeholders needed
        private final DataSetObservable mDataSetObservable = new DataSetObservable();

        private final ListAdapter mAdapter;
        private int mNumColumns = 1;

        // This ArrayList is assumed to NOT be null.
        ArrayList<FixedViewInfo> mFooterViewInfos;

        boolean mAreAllFixedViewsSelectable;

        private final boolean mIsFilterable;
        
        private int mNormalViewHeight;

        public FooterViewGridAdapter(ArrayList<FixedViewInfo> headerViewInfos, ListAdapter adapter) {
            mAdapter = adapter;
            mIsFilterable = adapter instanceof Filterable;

            if (headerViewInfos == null) {
                throw new IllegalArgumentException("headerViewInfos cannot be null");
            }
            mFooterViewInfos = headerViewInfos;

            mAreAllFixedViewsSelectable = areAllListInfosSelectable(mFooterViewInfos);
        }

        public int getFootersCount() {
            return mFooterViewInfos.size();
        }

        @Override
        public boolean isEmpty() {
            return (mAdapter == null || mAdapter.isEmpty()) && getFootersCount() == 0;
        }

        public void setNumColumns(int numColumns) {
            if (numColumns < 1) {
                throw new IllegalArgumentException("Number of columns must be 1 or more");
            }
            if (mNumColumns != numColumns) {
                mNumColumns = numColumns;
                notifyDataSetChanged();
            }
        }

        private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
            if (infos != null) {
                for (FixedViewInfo info : infos) {
                    if (!info.isSelectable) {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean removeFooter(View v) {
            for (int i = 0; i < mFooterViewInfos.size(); i++) {
                FixedViewInfo info = mFooterViewInfos.get(i);
                if (info.view == v) {
                    mFooterViewInfos.remove(i);

                    mAreAllFixedViewsSelectable = areAllListInfosSelectable(mFooterViewInfos);

                    mDataSetObservable.notifyChanged();
                    return true;
                }
            }

            return false;
        }

        @Override
        public int getCount() {
            if (mAdapter != null) {
//                return getFootersCount() * mNumColumns + mAdapter.getCount();
            	return getfooterAndPlaceHolders() + mAdapter.getCount();
            } else {
                return getFootersCount() * mNumColumns;
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            if (mAdapter != null) {
                return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
            } else {
                return true;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            /*// Header (negative positions will throw an ArrayIndexOutOfBoundsException)
            int numHeadersAndPlaceholders = getFootersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                return (position % mNumColumns == 0)
                        && mHeaderViewInfos.get(position / mNumColumns).isSelectable;
            }

            // Adapter
            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.isEnabled(adjPosition);
                }
            }*/
        	
        	if (mAdapter != null) {
            	final int adapterCount = mAdapter.getCount();
            	if (position >= adapterCount) {
            		// * * *
            		// * *
            		// - - -
            		int lastPos = getLastPos(adapterCount);
            		if (position > lastPos) {
            			int realPos = position - (lastPos+1);
                        return (position % mNumColumns == 0)
                                && mFooterViewInfos.get(realPos / mNumColumns).isSelectable;
            		} else {
            			return false;
            		}
            	} else {
            		return mAdapter.isEnabled(position);
            	}
        	} else {
                return (position % mNumColumns == 0)
                        && mFooterViewInfos.get(position / mNumColumns).isSelectable;
        	}

//            throw new ArrayIndexOutOfBoundsException(position);
        }

		private int getLastPos(final int adapterCount) {
			if (adapterCount == 0 || mNumColumns == 1)
				return adapterCount - 1;
			
			int rows = Math.round(adapterCount / mNumColumns) + 1;
			int lastPos = rows * mNumColumns - 1;
			return lastPos;
		}

        @Override
        public Object getItem(int position) {
            // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
/*            int numHeadersAndPlaceholders = getFootersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders) {
                if (position % mNumColumns == 0) {
                    return mHeaderViewInfos.get(position / mNumColumns).data;
                }
                return null;
            }

            // Adapter
            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItem(adjPosition);
                }
            }

            throw new ArrayIndexOutOfBoundsException(position);*/
        	
        	if (mAdapter != null) {
            	final int adapterCount = mAdapter.getCount();
            	if (position >= adapterCount) {
            		if (position % mNumColumns == 0) {
            			int realPos = position - adapterCount;
            			return mFooterViewInfos.get(realPos / mNumColumns).data;
            		}
            		return null;
            	}
            	return mAdapter.getItem(position);
        	} else {
        		if (position % mNumColumns == 0) {
        			return mFooterViewInfos.get(position/mNumColumns).data;
        		} else {
        			return null;
        		}
        	}
        }

        @Override
        public long getItemId(int position) {
            /*int numHeadersAndPlaceholders = getFootersCount() * mNumColumns;
            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                int adjPosition = position - numHeadersAndPlaceholders;
                int adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemId(adjPosition);
                }
            }*/
        	if (mAdapter != null) {
        		if (position < mAdapter.getCount()) {
        			return mAdapter.getItemId(position);
        		}
        	}
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            if (mAdapter != null) {
                return mAdapter.hasStableIds();
            }
            return false;
        }
        
        private int getSuppItemCount() {
        	int remain = mNumColumns - mAdapter.getCount() % mNumColumns;
        	return remain;
        }
        
        private int getfooterAndPlaceHolders() {
        	return getSuppItemCount() + getFootersCount() * mNumColumns;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
            /*int numHeadersAndPlaceholders = getFootersCount() * mNumColumns ;
            if (position < numHeadersAndPlaceholders) {
                View headerViewContainer = mHeaderViewInfos
                        .get(position / mNumColumns).viewContainer;
                if (position % mNumColumns == 0) {
                    return headerViewContainer;
                } else {
                    if (convertView == null) {
                        convertView = new View(parent.getContext());
                    }
                    // We need to do this because GridView uses the height of the last item
                    // in a row to determine the height for the entire row.
                    convertView.setVisibility(View.INVISIBLE);
                    convertView.setMinimumHeight(headerViewContainer.getHeight());
                    return convertView;
                }
            }

            // Adapter
            final int adjPosition = position - numHeadersAndPlaceholders;
            int adapterCount = 0;
            if (mAdapter != null) {
                adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getView(adjPosition, convertView, parent);
                }
            }*/
        	
        	final int adapterCount = mAdapter != null ? mAdapter.getCount() : 0;
        	final int lastPos = getLastPos(adapterCount);
        	if (position >= adapterCount) {
    			int excludePosition = position - (lastPos + 1);
        		if (position % mNumColumns == 0 && position > lastPos) {
                    View footerViewContainer = mFooterViewInfos
                            .get(excludePosition / mNumColumns).viewContainer;
                    return footerViewContainer;
        		} else {
        			if (convertView == null) {
        				convertView = new View(parent.getContext());
        			}
    				convertView.setVisibility(View.INVISIBLE);
    				int wrapperHeight = mNormalViewHeight;
    				convertView.setMinimumHeight(wrapperHeight);
    				return convertView;
        		}
        	}
    		final View normalView = mAdapter.getView(position, convertView, parent);
    		if (mNormalViewHeight == 0) {
    			measureNormalViewHeight(normalView);
    		}
    		return normalView;
//            throw new ArrayIndexOutOfBoundsException(position);
        }

		private void measureNormalViewHeight(final View normalView) {
			// we need to do this because we need to extend
			// previous cell height if we are at the tailing
			// of the last normal row
			mNormalViewHeight = normalView.getLayoutParams().height;
			if (mNormalViewHeight < 0) {
				normalView.measure(0, 0);
				mNormalViewHeight = normalView.getMeasuredHeight();
			}
			mNormalViewHeight = Math.max(0, mNormalViewHeight);
		}

        @Override
        public int getItemViewType(int position) {
            /*int numHeadersAndPlaceholders = getFootersCount() * mNumColumns;
            if (position < numHeadersAndPlaceholders && (position % mNumColumns != 0)) {
                // Placeholders get the last view type number
                return mAdapter != null ? mAdapter.getViewTypeCount() : 1;
            }
            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                int adjPosition = position - numHeadersAndPlaceholders;
                int adapterCount = mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemViewType(adjPosition);
                }
            }*/
        	
        	/*if (mAdapter != null) {
        		final int adapterCount = mAdapter.getCount();
        		if (position >= adapterCount && (position % mNumColumns) != 0) {
        			return mAdapter.getViewTypeCount();
        		}
        		return mAdapter.getItemViewType(position);
        	}*/
        	
        	final int adapterCount = mAdapter != null ? mAdapter.getCount() : 0;
        	if (position >= adapterCount && position % mNumColumns != 0) {
        		return adapterCount == 0 ? 1 : mAdapter.getViewTypeCount();
        	}
        	
        	if (mAdapter != null && position < adapterCount) {
        		return mAdapter.getItemViewType(position);
        	}
        	
            return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
        }

        @Override
        public int getViewTypeCount() {
            if (mAdapter != null) {
                return mAdapter.getViewTypeCount() + 1;
            }
            return 2;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.unregisterObserver(observer);
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public Filter getFilter() {
            if (mIsFilterable) {
                return ((Filterable) mAdapter).getFilter();
            }
            return null;
        }

        @Override
        public ListAdapter getWrappedAdapter() {
            return mAdapter;
        }

        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }
    }
}
