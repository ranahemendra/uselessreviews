/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.totspot.uselessreviews.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.parse.ParseObject;
import com.totspot.uselessreviews.R;
import com.totspot.uselessreviews.data.DataModel;
import com.totspot.uselessreviews.data.FeedItem;
import com.totspot.uselessreviews.data.GetPicutreCallback;

/**
 * A concrete BaseAdapter that is backed by an array of arbitrary
 * objects.  By default this class expects that the provided resource id references
 * a single TextView.  If you want to use a more complex layout, use the constructors that
 * also takes a field id.  That field id should reference a TextView in the larger layout
 * resource.
 *
 * <p>However the TextView is referenced, it will be filled with the toString() of each object in
 * the array. You can add lists or arrays of custom objects. Override the toString() method
 * of your objects to determine what text will be displayed for the item in the list.
 *
 * <p>To use something other than TextViews for the array display, for instance, ImageViews,
 * or to have some of data besides toString() results fill the views,
 * override {@link #getView(int, View, ViewGroup)} to return the type of view you want.
 */
public class ItemCommentsListViewAdapter extends BaseAdapter implements Filterable {
	private static final String LOG_TAG = "FeedItemListViewAdapter";

	public static int MAX_PICTURES_TO_FETCH_IN_ONE_SHOT = 10;

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<ParseObject> mObjects;
    
    private Map<String, ParseObject> mOriginalValuesMap;
    
    private LruCache<String, Bitmap> mFeedPicturesCache;
    
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;    
        
    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private int mResource;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private int mDropDownResource;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    private Context mContext;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<ParseObject> mOriginalValues;
    
    private ArrayFilter mFilter;

    private LayoutInflater mInflater;
    
	private Random mRand = new Random();

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    public ItemCommentsListViewAdapter(Context context) {
        init(context, new ArrayList<ParseObject>());
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    public ItemCommentsListViewAdapter(Context context, List<ParseObject> objects) {
        init(context, objects);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(ParseObject object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
                mOriginalValuesMap.put(object.getObjectId(), object);
//                if (mFeedPictures.size() <= 10 && mPendingPictureFetches <= 10) {
//                	fetchPicture(object);
//                }
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }
//
//    private synchronized void fetchPicture(final ParseObject po) {
//    	mPendingPictureFetches++;
//		ParseFile file = po.getParseFile(FeedItem.BIGPIC);
//		file.getDataInBackground(new GetDataCallback() {
//			
//			@Override
//			public void done(byte[] data, ParseException arg1) {
//				if (data != null) {
//					mFeedPictures.put(po.getObjectId(), data);
//				}
//				
//				synchronized (FeedItemListViewAdapter.this) {
//					mPendingPictureFetches--;
//				}
//			}
//		});
//		
//
//		
//	}

	/**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends ParseObject> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
                for (ParseObject po: collection) {
                	mOriginalValuesMap.put(po.getObjectId(), po);
                }
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(ParseObject ... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
                for (ParseObject po: items) {
                	mOriginalValuesMap.put(po.getObjectId(), po);
                }
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(ParseObject object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
                mOriginalValuesMap.put(object.getObjectId(), object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(ParseObject object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
                mOriginalValuesMap.remove(object.getObjectId());                
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
                mOriginalValuesMap.clear();
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super ParseObject> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    private void init(Context context, List<ParseObject> objects) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = mDropDownResource = R.layout.list_feeditem_activated_1;
        mObjects = objects;
        mOriginalValuesMap = new HashMap<String, ParseObject>();
        for (ParseObject po : objects) {
        	mOriginalValuesMap.put(po.getObjectId(), po);
        }
    }

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * {@inheritDoc}
     */
    public int getCount() {
        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    public ParseObject getItem(int position) {
        return mObjects.get(position);
    }
    
    private Bitmap getImageForItem(ParseObject po) {
    	Bitmap image = mFeedPicturesCache.get(po.getObjectId());
    	
    	if (image == null) {
    		DataModel.getInstance().fetchPictureForFeedItem(po, new GetPicutreCallback() {
				
				@Override
				public void done(Bitmap bitmap) {
					// TODO Update the cell with the image in the view.
					if (bitmap != null && mNotifyOnChange) {
						notifyDataSetChanged();
					}
				}
			});
    	}
    	
    	return image;
	}

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(ParseObject item) {
        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView, 
    		ViewGroup parent, int layout) {
    	
        View view;

        if (convertView == null) {
            view = mInflater.inflate(layout, parent, false);
        } else {
            view = convertView;
        }

        ParseObject item = getItem(position);

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new FeedItemRatingChangeListener(item));
        updateRatingBar(item, ratingBar);

        FeedItemOnClickListener listener = new FeedItemOnClickListener(item); 
        ImageView image = (ImageView) view.findViewById(R.id.image);
        image.setOnClickListener(listener);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setOnClickListener(listener);
        
        // Set the image, if one exists.
        Bitmap bm = getImageForItem(item);
        if (bm != null) {
        	image.setImageBitmap(bm);
        }
        
        title.setText(item.getString(FeedItem.TITLE));

        return view;
    }
    
	private void updateRatingBar(ParseObject item, RatingBar ratingBar) {
    	LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
    	Log.d(LOG_TAG, "Checking if user " + DataModel.getInstance().getLoggedInUser().getUsername() + 
    			" has rated item " + item.getString(FeedItem.TITLE));
        if (DataModel.getInstance().loggedInUserHasRated(item)) {
        	stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
            float userRating = DataModel.getInstance().getUserRating(item);
            if (userRating < 0) {
            	Log.e(LOG_TAG, "WHAT THE HECK!!!");
            	userRating = 0; 
            }
            ratingBar.setRating(userRating); 
        } else {
        	stars.getDrawable(2).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            float aggrRating = (float) item.getDouble(FeedItem.AGGREGATE_RATING);
            ratingBar.setRating(aggrRating); 
        }
	}

	/**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

    /**
     * {@inheritDoc}
     */
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<ParseObject>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<ParseObject> list;
                synchronized (mLock) {
                    list = new ArrayList<ParseObject>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<ParseObject> values;
                synchronized (mLock) {
                    values = new ArrayList<ParseObject>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<ParseObject> newValues = new ArrayList<ParseObject>();

                for (int i = 0; i < count; i++) {
                    final ParseObject value = values.get(i);
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<ParseObject>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
    
	public ParseObject getObjectById(String id) {
		return mOriginalValuesMap.get(id);
	}

	public void addPicture(ParseObject po, Bitmap bitmap) {
		initLruCacheIfNeeded();
		
		if (getBitmapFromMemCache(po.getObjectId()) == null) {
			Log.d(LOG_TAG , "Adding a picture for feed item " + po.getObjectId() + ". Picture size: " + 
					bitmap.getByteCount());
	        mFeedPicturesCache.put(po.getObjectId(), bitmap);
	    } else {
			Log.d(LOG_TAG , "We already have picture for " + po.getObjectId() + ". Ignoring request " +
					"to add picture to the cache.");
	    }
	}
	
	private synchronized void initLruCacheIfNeeded() {
		if (mFeedPicturesCache == null) {
			Log.d(LOG_TAG , "Creating LRU cache for images of size " + cacheSize);
			mFeedPicturesCache = new LruCache<String, Bitmap>(cacheSize) {
		        @Override
		        protected int sizeOf(String key, Bitmap bitmap) {
		            // The cache size will be measured in kilobytes rather than
		            // number of items.
		            return bitmap.getByteCount() / 1024;
		        }
		    };
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		initLruCacheIfNeeded();
	    return mFeedPicturesCache.get(key);
	}
	
	public boolean isLruCacheNearlyFull() {
		initLruCacheIfNeeded();
		return (mFeedPicturesCache.size() >= cacheSize * 0.9);
	}

	public Bitmap getPictureById(String key) {
		initLruCacheIfNeeded();
		return mFeedPicturesCache.get(key);
	}
}
