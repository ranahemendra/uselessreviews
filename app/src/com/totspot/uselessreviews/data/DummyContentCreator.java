package com.totspot.uselessreviews.data;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.totspot.uselessreviews.R;
import com.totspot.uselessreviews.UselessReviewsApplication;

public class DummyContentCreator {
	private static final String LOG_TAG = "DummyContentCreator";
	
	private static final String[] USERNAMES = new String[] {"vikrant", "vijay", "hemendra", "pascal"};
	private static final String PASSWORD = "foo";
	
	private static ParseUser me;

	private static byte[] sPicture;
	
	public synchronized static ParseUser getMe() {
		if (me != null) {
			return me;
		}
		
		int index = getRandomIndex();
		String username = USERNAMES[index];
		Log.d(LOG_TAG, "Me is " + username);
		
		try {
			ParseUser.logIn(username, PASSWORD);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		
		me = ParseUser.getCurrentUser();
		return me;
	}
	
	public static void createDummyContent() {
		final ParseObject feed = new ParseObject("FeedItem");
		ParseRelation<ParseUser> relation = feed.getRelation("user");
		relation.add(me);
		feed.put(FeedItem.TITLE, "Title " + System.currentTimeMillis());
		feed.put(FeedItem.DESCRIPTION, System.currentTimeMillis() + "Lorem ipsum dolor sit amet, " +
				"consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et " +
				"dolore magna aliqua.");
		ParseFile file = new ParseFile(getPicture());
		feed.put(FeedItem.BIGPIC, file);
		feed.put(FeedItem.AGGREGATE_RATING, getRandomRating());
		feed.put(FeedItem.RATING_COUNT, 0);

		try {
			feed.save();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, e.getMessage(), e);
		}
	}
	
	private static float getRandomRating() {
		float minX = 0.0f;
		float maxX = 5.0f;

		Random rand = new Random();
		return rand.nextFloat() * (maxX - minX) + minX;	
	}

	// Private utility methods.
	private static int getRandomIndex() {
		Random rand = new Random();
		return rand.nextInt(USERNAMES.length);
	}
		
	public static byte[] getPicture() {
		if (sPicture != null) {
			return sPicture;
		}
		
		Resources res = getContext().getResources();
		int id = R.drawable.car;
		Bitmap b = BitmapFactory.decodeResource(res, id);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] bytes = stream.toByteArray();
		sPicture = bytes;
		
		return sPicture;
		
//		File sdcard = Environment.getExternalStorageDirectory();
//		File file = new File(sdcard,"Pictures/Screenshots/Screenshot_2014-06-24-10-24-46.png");
		
//		Log.d(LOG_TAG, "Found the file " + file);
//		
//		try {
//			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			byte[] bytes = new byte[1024];
//			int read = 0;
//			while ((read = bis.read(bytes)) != -1) {
//				bos.write(bytes, 0, read);
//			}
//
//			bytes = bos.toByteArray();
//			Log.d(LOG_TAG, "Size of file is " + bytes.length);
//			sPicture = bytes;
//			return sPicture;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			Log.e(LOG_TAG, e.getMessage(), e);
//		}
//		
//		return null;
	}
	
	private static Context getContext() {
		return UselessReviewsApplication.getInstance().getContext();
	}
}
