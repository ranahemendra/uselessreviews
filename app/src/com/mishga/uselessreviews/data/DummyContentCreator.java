package com.mishga.uselessreviews.data;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.RefreshCallback;
import com.mishga.uselessreviews.R;
import com.mishga.uselessreviews.UselessReviewsApplication;

public class DummyContentCreator {
	private static final String LOG_TAG = "DummyContentCreator";
	
	private static final String[] USERNAMES = new String[] {"vikrant", "vijay", "hemendra", "pascal"};
	private static final int[] RESOURCE_IDS = new int[] {
		R.drawable.car1,
		R.drawable.car2,
		R.drawable.car3,
		R.drawable.car4,
		R.drawable.car5,
		R.drawable.car6,
		R.drawable.car7,
		R.drawable.car8,
		R.drawable.car9,
		R.drawable.car10
	};
	private static final String PASSWORD = "foo";
	private static Random rand = new Random();
	
	private static byte[] sPicture;
	
	public static String[] getCurrentUserAndPassword() {
		String[] userAndPassword = new String[2];
		
		int index = getRandomIndex(USERNAMES.length);
		String username = USERNAMES[index];
		Log.d(LOG_TAG, "Username is " + username);
		userAndPassword[0] = username;
		userAndPassword[1] = PASSWORD;
		
		return userAndPassword;
	}
	
	/**
	 * Initialized the data model.
	 */
	public static void initDummyDataInBackground() {
		// Do in the background.
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				DummyContentCreator.createDummyContent();
				return null;
			}
	    };
	    task.execute();
	}
	
	public static void createDummyContent() {
		final ParseObject feed = new ParseObject("FeedItem");
		ParseRelation<ParseUser> relation = feed.getRelation("user");
		relation.add(DataModel.getInstance().getLoggedInUser());
		feed.put(FeedItem.TITLE, "Title " + System.currentTimeMillis());
		feed.put(FeedItem.DESCRIPTION, System.currentTimeMillis() + "Lorem ipsum dolor sit amet, " +
				"consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et " +
				"dolore magna aliqua.");
		ParseFile file = new ParseFile(getPicture());
		feed.put(FeedItem.BIGPIC, file);
		feed.put(FeedItem.AGGREGATE_RATING, 0d);
		feed.put(FeedItem.RATING_COUNT, 0);

		try {
			feed.save();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		
		// After saving, refresh the data model.
		// DataModel.getInstance().refreshFeedItems();
	}
	
	private static float getRandomRating() {
		float minX = 0.0f;
		float maxX = 5.0f;

		Random rand = new Random();
		return rand.nextFloat() * (maxX - minX) + minX;	
	}

	// Private utility methods.
	private static int getRandomIndex(int length) {
		return rand.nextInt(length);
	}
		
	public static byte[] getPicture() {
		if (sPicture != null) {
			return sPicture; 
		}
		
		Resources res = getContext().getResources();
		int id = RESOURCE_IDS[getRandomIndex(RESOURCE_IDS.length)];
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
