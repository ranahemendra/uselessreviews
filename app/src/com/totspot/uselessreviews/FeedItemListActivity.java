package com.totspot.uselessreviews;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.Parse;
import com.totspot.uselessreviews.data.DataModel;

/**
 * An activity representing a list of FeedItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link FeedItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FeedItemListFragment} and the item details
 * (if present) is a {@link FeedItemDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link FeedItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class FeedItemListActivity extends Activity
        implements FeedItemListFragment.Callbacks {
	
	private static final String LOG_TAG = "FeedImteListActivity";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Parse.initialize(this, "TZzwOsqfD2rQPtX1CyeELXFZ83Gz9LL346EMLKI8", "TcERHF0I8qnDeoy2byKJp66ywrT32sosr8LdL5pU");
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        
        setContentView(R.layout.activity_feeditem_list);

        if (findViewById(R.id.feeditem_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((FeedItemListFragment) getFragmentManager()
                    .findFragmentById(R.id.feeditem_list))
                    .setActivateOnItemClick(true);
        }
        
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground(new SaveCallback() {
//			
//			@Override
//			public void done(ParseException arg0) {
//				// TODO Auto-generated method stub
//		        Toast.makeText(FeedItemListActivity.this, "Saved in background", Toast.LENGTH_LONG).show();
//			}
//		});
        
        Log.d(LOG_TAG, "Initializing the data model.");
        DataModel.getInstance();
        
        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link FeedItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(FeedItemDetailFragment.ARG_ITEM_ID, id);
            FeedItemDetailFragment fragment = new FeedItemDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.feeditem_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, FeedItemDetailActivity.class);
            detailIntent.putExtra(FeedItemDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}