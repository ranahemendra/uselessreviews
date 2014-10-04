package com.totspot.uselessreviews;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.totspot.uselessreviews.data.DataModel;
import com.totspot.uselessreviews.data.FeedItem;

/**
 * A fragment representing a single FeedItem detail screen.
 * This fragment is either contained in a {@link FeedItemListActivity}
 * in two-pane mode (on tablets) or a {@link FeedItemDetailActivity}
 * on handsets.
 */
public class FeedItemDetailFragment extends Fragment {
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
            ((TextView) rootView.findViewById(R.id.feeditem_detail_rating)).setText(mFeedItem.getDouble(FeedItem.AGGREGATE_RATING) + "");
            ((TextView) rootView.findViewById(R.id.feeditem_detail_title)).setText(mFeedItem.getString(FeedItem.TITLE));
            ((TextView) rootView.findViewById(R.id.feeditem_detail_description)).setText(mFeedItem.getString(FeedItem.DESCRIPTION));
        }

        return rootView;
    }
}
