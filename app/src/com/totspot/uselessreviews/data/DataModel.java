package com.totspot.uselessreviews.data;

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class DataModel {
	private static final String LOG_TAG = "DataModel";
	// Static instance.
	private static DataModel sInstance;
	
	// The user of the app.
	private ParseUser me;
	
	// The feed items to display to the user.
	private List<ParseObject> feedItems;
		
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
	    
	    refreshFeedItems();
	}
	
	public void refreshFeedItems() {
	    // Let's now fetch part of the feed from the server.
	    ParseQuery<ParseObject> query = ParseQuery.getQuery("FeedItem");
	    query.addDescendingOrder("createdAt");
	    
	    Log.d(LOG_TAG, "Fetching feed item list.");
	    query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> feedItemPOs, ParseException arg1) {
				Log.i(LOG_TAG, "Fetched " + feedItemPOs.size() + " records for feed items");
				feedItems = feedItemPOs;
				
//				for (ParseObject po: feedItems) {
//					try {
//						ParseFile bigPic = (ParseFile) po.get(FeedItem.BIGPIC);
//						byte[] data;
//						data = bigPic.getData();
//						Log.d(LOG_TAG, "Pulled a picture of size " + data.length + 
//								" for feed item with id " + po.getObjectId());
//					} catch (ParseException e) {
//						Log.e(LOG_TAG, e.getMessage(), e);
//					}
//				}
				
				Log.d(LOG_TAG, "Feed items: " + feedItems);
			}
		});
	    
	    
	}
}
