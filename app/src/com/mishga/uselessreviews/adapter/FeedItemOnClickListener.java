package com.mishga.uselessreviews.adapter;

import android.view.View;
import android.view.View.OnClickListener;

import com.parse.ParseObject;
import com.mishga.uselessreviews.FeedItemListActivity;
import com.mishga.uselessreviews.FeedItemListFragment;
import com.mishga.uselessreviews.R;

public class FeedItemOnClickListener implements OnClickListener {
	private ParseObject mFeedItem;
//	private FeedItemListActivity mContext;
	
	public FeedItemOnClickListener(ParseObject item) {
		mFeedItem = item;
//		mContext = (FeedItemListActivity) context;
	}

	@Override
	public void onClick(View v) {
		FeedItemListFragment fragment = (FeedItemListFragment) ((FeedItemListActivity) v.getContext()).getFragmentManager()
				.findFragmentById(R.id.feeditem_list);
		fragment.onListItemClicked(mFeedItem);
	}

}
