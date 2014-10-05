package com.totspot.uselessreviews.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.parse.ParseObject;
import com.totspot.uselessreviews.data.DataModel;
import com.totspot.uselessreviews.data.FeedItem;

public class FeedItemRatingChangeListener implements OnRatingBarChangeListener {
	private static final String LOG_TAG = "FeedItemRatingChangeListener";
	
	private ParseObject mFeedItem;
	
	public FeedItemRatingChangeListener(ParseObject item) {
		mFeedItem = item;
	}
	
	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		if (!fromUser) {
			return;
		}

		Log.d(LOG_TAG, "Rating " + rating + " given to item " + mFeedItem.getString(FeedItem.TITLE));
		LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
		stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

		DataModel.getInstance().setUserRating(mFeedItem, rating);
	}
}
