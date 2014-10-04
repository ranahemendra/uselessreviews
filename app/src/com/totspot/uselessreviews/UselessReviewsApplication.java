package com.totspot.uselessreviews;

import android.app.Application;
import android.content.Context;

public class UselessReviewsApplication extends Application {

    private static UselessReviewsApplication instance;

	public UselessReviewsApplication() {
		// TODO Auto-generated constructor stub
	}
	
    public static UselessReviewsApplication getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
