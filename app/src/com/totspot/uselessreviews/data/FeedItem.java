package com.totspot.uselessreviews.data;

import com.parse.ParseFile;
import com.parse.ParseUser;

public class FeedItem {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String BIGPIC = "bigPic";
	public static final String AGGREGATE_RATING = "aggregateRating";
	public static final String RATING_COUNT = "ratingCount";

	private String id;
	private ParseUser user;
	private String title;
	private String description;
	private float aggregateRating;
	private int ratingCount;

}
