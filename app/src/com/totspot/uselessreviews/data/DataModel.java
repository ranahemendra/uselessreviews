package com.totspot.uselessreviews.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.totspot.uselessreviews.LoginListener;
import com.totspot.uselessreviews.adapter.FeedItemListViewAdapter;

public class DataModel {
	private static final String LOG_TAG = "DataModel";
	// Static instance.
	private static DataModel sInstance;
	
	// The user of the app.
	private ParseUser me;
	
	// The adapter that keeps our data.
	private FeedItemListViewAdapter mListAdapter;
	
	private Map<String, ParseObject> mRatings;
		
	/**
	 * Private constructor for this singleton.
	 */
	private DataModel() {
		mRatings = new HashMap<String, ParseObject>();
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
	
	public void login(final String username, final String password, final LoginListener listener) {
		
		// Do in the background.
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				Log.i(LOG_TAG, "Trying to log in as " + username);
				try {
					ParseUser.logIn(username, password);
				} catch (ParseException e) {
					Log.e(LOG_TAG, "Failed to log in as " + username, e);
					listener.failed(e);
					return null;
				}
				
				me = ParseUser.getCurrentUser();
				Log.i(LOG_TAG, "Successfully logged in as " + username);
				listener.success();
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
	    ParseQuery<ParseObject> query = ParseQuery.getQuery(FeedItem.OBJECT_NAME);
	    query.addDescendingOrder(FeedItem.CREATED_AT);
	    
	    Log.d(LOG_TAG, "Fetching feed item list.");
	    query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> feedItems, ParseException arg1) {
				Log.i(LOG_TAG, "Fetched " + feedItems.size() + " records for feed items");
				for (ParseObject po: feedItems) {
					mListAdapter.remove(po);
					mListAdapter.add(po);
				}
				Log.d(LOG_TAG, "Feed items: " + feedItems);
				
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
				
				// Fetch the ratings that the logged in user has applied.
			    ParseQuery<ParseObject> query = ParseQuery.getQuery(UserRating.OBJECT_NAME);
			    query.whereEqualTo(UserRating.RATER, getLoggedInUser());

			    Log.d(LOG_TAG, "Fetching ratings by " + getLoggedInUser().getUsername());
			    query.findInBackground(new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> userRatings, ParseException arg1) {
						Log.i(LOG_TAG, "Fetched " + userRatings.size() + " records for user ratings for " + 
								getLoggedInUser().getUsername());
						for (ParseObject po: userRatings) {
							ParseRelation<ParseObject> relation = po.getRelation(UserRating.RATED_FEED_ITEM);
							ParseQuery<ParseObject> query = relation.getQuery();
							ParseObject feedItem;
							try {
								feedItem = query.getFirst();
								mRatings.put(feedItem.getObjectId(), po);
							} catch (ParseException e) {
								Log.e(LOG_TAG, e.getMessage(), e);
							}
						}
					}
			    	
			    });
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

	public ParseUser getLoggedInUser() {
		return me;
	}

	public boolean loggedInUserHasRated(ParseObject item) {
		return (mRatings.get(item.getObjectId()) != null);
	}

	public float getUserRating(ParseObject item) {
		ParseObject userRating = mRatings.get(item.getObjectId());
		if (userRating == null) {
			return -1f;
		}
		
		return (float) userRating.getDouble(UserRating.RATING);
	}

	public void setUserRating(ParseObject item, float rating) {
		if (loggedInUserHasRated(item)) {
			ParseObject userRating = mRatings.get(item.getObjectId());
			double oldRating = userRating.getDouble(UserRating.RATING); 
			userRating.put(UserRating.RATING, rating);
			userRating.saveInBackground();
			
			int ratingCount = item.getInt(FeedItem.RATING_COUNT);
			double aggregateRating = item.getDouble(FeedItem.AGGREGATE_RATING);		
			aggregateRating = ((aggregateRating * ratingCount) + rating - oldRating) / ratingCount;
			item.put(FeedItem.AGGREGATE_RATING, aggregateRating);
			item.saveInBackground();
		} else {
			final ParseObject userRating = new ParseObject(UserRating.OBJECT_NAME);

			ParseRelation<ParseUser> relation = userRating.getRelation(UserRating.RATER);
			relation.add(DataModel.getInstance().getLoggedInUser());
			ParseRelation<ParseObject> ratedItemRelation = userRating.getRelation(UserRating.RATED_FEED_ITEM);
			ratedItemRelation.add(item);		
			userRating.put(UserRating.RATING, rating);
			userRating.saveInBackground();
			mRatings.put(item.getObjectId(), userRating);

			int ratingCount = item.getInt(FeedItem.RATING_COUNT);
			double aggregateRating = item.getDouble(FeedItem.AGGREGATE_RATING);		
			aggregateRating = ((aggregateRating * ratingCount) + rating) / ++ratingCount;
			item.put(FeedItem.AGGREGATE_RATING, aggregateRating);
			item.put(FeedItem.RATING_COUNT, ratingCount);
			item.saveInBackground();
		}
	}
}
