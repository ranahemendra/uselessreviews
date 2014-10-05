package com.totspot.uselessreviews.data;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.totspot.uselessreviews.adapter.FeedItemListViewAdapter;

public class DataModel {
	private static final String LOG_TAG = "DataModel";
	// Static instance.
	private static DataModel sInstance;
	
	// The user of the app.
	private ParseUser me;
	
	// The adapter that keeps our data.
	private FeedItemListViewAdapter mListAdapter;
		
	/**
	 * Private constructor for this singleton.
	 */
	private DataModel() {
		init();
	}
	
	/**
	 * Accessor of this singleton.
	 * @return
	 */
	public synchronized static DataModel getInstance() {
		if (sInstance == null) {
			sInstance = new DataModel();
		}
		
		return sInstance;
	}
	
	/**
	 * Initialized the data model.
	 */
	private void init() {
		// TODO: Get rid of this code once you have enough feed items.
		// Do in the background.
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				me = DummyContentCreator.getMe();
				Log.d(LOG_TAG, "Logged in as " + me.getUsername());
				
				DummyContentCreator.createDummyContent();
				
				return null;
			}
	    };
	    task.execute();
	}
	
	/**
	 * Fetch a fresh list of feed items from the server and update the adapter 
	 * with the data that is received.
	 */
	public void refreshFeedItems() {
	    // Let's now fetch part of the feed from the server.
	    ParseQuery<ParseObject> query = ParseQuery.getQuery("FeedItem");
	    query.addDescendingOrder("createdAt");
	    
	    Log.d(LOG_TAG, "Fetching feed item list.");
	    query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> feedItems, ParseException arg1) {
				Log.i(LOG_TAG, "Fetched " + feedItems.size() + " records for feed items");
				for (ParseObject po: feedItems) {
					mListAdapter.remove(po);
					mListAdapter.add(po);
				}
				
				Log.d(LOG_TAG, "Starting to fetch pictures for " + feedItems.size() + " feed items.");

				int count = 0;
				for (ParseObject po: feedItems) {
					if (count == FeedItemListViewAdapter.MAX_PICTURES_TO_FETCH_IN_ONE_SHOT) {
						// We have loaded enough items in memory, let's stop pulling more data.
						break;
					}
					
					count++;
					final int thisCount = count;
					final ParseObject thisPO = po;
					fetchPictureForFeedItem(po, new GetPicutreCallback() {

						@Override
						public void done(Bitmap bitmap) {
							// Do nothing.
							if (bitmap == null) {
								Log.d(LOG_TAG, "Item " + thisCount + " of id " + thisPO.getObjectId() +  
										" and title " + thisPO.getString(FeedItem.TITLE) + 
										" didn't have an associated picture");	
							} else {
								Log.d(LOG_TAG, "Pulled a picture for item " + thisCount + " of size " + 
										bitmap.getByteCount() + " for feed item with id " + thisPO.getObjectId());
							}
						}
					});
				}
				
				Log.d(LOG_TAG, "Feed items: " + feedItems);
			}
		});
	}

	public void setListAdapter(FeedItemListViewAdapter listAdapter) {
		mListAdapter = listAdapter;
	}

	public ParseObject getFeedItemById(String id) {
		return mListAdapter.getObjectById(id);
	}
	
	public void fetchPictureForFeedItem(ParseObject po, GetPicutreCallback callback) {
		Bitmap picture = mListAdapter.getPictureById(po.getObjectId());
		if (picture == null) {
			fetchAndCachePicture(po, callback);
		} else {
			callback.done(picture);
		}
	}

	private void fetchAndCachePicture(final ParseObject po, final GetPicutreCallback callback) {
		ParseFile bigPic = (ParseFile) po.get(FeedItem.BIGPIC);
		bigPic.getDataInBackground(new GetDataCallback() {

			@Override
			public void done(byte[] data, ParseException arg1) {
				if (data != null && data.length != 0) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					mListAdapter.addPicture(po, bitmap);
					callback.done(bitmap);
				} else {
					callback.done(null);
				}

			}
		});
	}
}
