package com.mishga.uselessreviews;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.view.MenuItem;
import com.mishga.uselessreviews.R;


/**
 * An activity representing a single FeedItem detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link FeedItemListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link FeedItemDetailFragment}.
 */
public class FeedItemDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeditem_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(false);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(FeedItemDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(FeedItemDetailFragment.ARG_ITEM_ID));
            FeedItemDetailFragment fragment = new FeedItemDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.feeditem_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, FeedItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
