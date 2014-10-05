package com.mishga.uselessreviews;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mishga.uselessreviews.R;
import com.mishga.uselessreviews.adapter.FeedItemRatingChangeListener;
import com.mishga.uselessreviews.data.DataModel;
import com.mishga.uselessreviews.data.FeedItem;
import com.mishga.uselessreviews.data.GetPicutreCallback;
import com.parse.ParseObject;

/**
 * A fragment representing a single FeedItem detail screen.
 * This fragment is either contained in a {@link FeedItemListActivity}
 * in two-pane mode (on tablets) or a {@link FeedItemDetailActivity}
 * on handsets.
 */
public class FeedItemDetailFragment extends Fragment {
	private static final String LOG_TAG = "FeeditemDetailFragment";
	
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ParseObject mFeedItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FeedItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
        	mFeedItem = DataModel.getInstance().getFeedItemById(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feeditem_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mFeedItem != null) {
        	createViewFromResource(rootView, mFeedItem);
        }

        return rootView;
    }
    
    private void createViewFromResource(View rootView, ParseObject item) {
    	
    	((FeedItemDetailActivity) rootView.getContext()).getActionBar().setTitle(item.getString(FeedItem.TITLE));	
        ((TextView) rootView.findViewById(R.id.feeditem_detail_description)).setText(mFeedItem.getString(FeedItem.DESCRIPTION));
    	
        RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.feeditem_detail_ratingBar);
        ratingBar.setOnRatingBarChangeListener(new FeedItemRatingChangeListener(item));
        updateRatingBar(item, ratingBar);

        final ImageView image = (ImageView) rootView.findViewById(R.id.feeditem_detail_image);
		DataModel.getInstance().fetchPictureForFeedItem(item, new GetPicutreCallback() {

			@Override
			public void done(Bitmap bitmap) {
				// TODO Update the cell with the image in the view.
				if (bitmap != null) {
		        	image.setImageBitmap(bitmap);
				}
			}
		});
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
    
}
