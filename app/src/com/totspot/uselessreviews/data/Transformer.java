package com.totspot.uselessreviews.data;

import com.parse.ParseObject;

public class Transformer {
	public static Object transform(ParseObject object, Class clazz) {
		if (clazz.isAssignableFrom(FeedItem.class)) {
			return getFeedItem(object);
		}
		return null;
	}
	
	private static FeedItem getFeedItem(ParseObject object) {
		FeedItem feedItem = new FeedItem();
//		User user = new User();
//		user.setId(object.getObjectId());
//		user.setUsername(object.getString(User.USERNAME));
//		
//		return user;
		return null;
	}
}
