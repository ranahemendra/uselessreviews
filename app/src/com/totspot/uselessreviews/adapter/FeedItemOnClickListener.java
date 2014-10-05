package com.totspot.uselessreviews.adapter;

import android.view.View;
import android.view.View.OnClickListener;

import com.parse.ParseObject;
import com.totspot.uselessreviews.FeedItemListActivity;
import com.totspot.uselessreviews.FeedItemListFragment;
import com.totspot.uselessreviews.R;

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
