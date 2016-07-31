package bigmikehoncho.com.rssreader;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import bigmikehoncho.com.rssreader.content.Data;
import bigmikehoncho.com.rssreader.content.Article;

public class MainActivity extends AppCompatActivity implements FeedListFragment.OnListFragmentInteractionListener,
		RssLoaderFragment.NetworkRequestListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String TAG_LOADER_FRAGMENT = "loader";
	private static final String TAG_FEED_LIST_FRAGMENT = "feedList";

	private RssLoaderFragment mLoaderFragment;
	private FeedListFragment mFeedListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager fm = getSupportFragmentManager();
		mLoaderFragment = (RssLoaderFragment) fm.findFragmentByTag(TAG_LOADER_FRAGMENT);
		mFeedListFragment = (FeedListFragment) fm.findFragmentByTag(TAG_FEED_LIST_FRAGMENT);

		if (mLoaderFragment == null) {
			// RssLoaderFragment has not been retained so we must create it anew
			mLoaderFragment = new RssLoaderFragment();

			fm.beginTransaction()
					.add(mLoaderFragment, TAG_LOADER_FRAGMENT)
					.commit();
		}
		if (mFeedListFragment == null) {
			// FeedListFragment has not been retained so we must create it anew
			mFeedListFragment = FeedListFragment.newInstance(new ArrayList<Article>(), 1);
			fm.beginTransaction()
					// It's almost always a good idea to use .replace instead of .add so that
					// you never accidentally layer multiple Fragments on top of each other
					// unless of course that's your intention
					.replace(R.id.container, mFeedListFragment, TAG_FEED_LIST_FRAGMENT)
					.commit();
		}
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		if(savedInstanceState == null){
			// If just starting the activity load the pages
			onLoadNext();
		}
	}

	/* BEGIN Callbacks from FeedListFragment.OnListFragmentInteractionListener*/
	@Override
	public void onListFragmentInteraction(Article article) {
		Log.i(TAG, "onListFragmentInteraction");

	}

	@Override
	public void onLoadNext() {
		Log.i(TAG, "onLoadNext: " + Data.getInstance().mCurrentPage);

		mLoaderFragment.startTask(Constants.URL + Constants.URL_PAGE + Data.getInstance().mCurrentPage);
	}
	/* END Callbacks from FeedListFragment.OnListFragmentInteractionListener*/

	/* BEGIN Callbacks from NetworkRequestListener*/
	@Override
	public void onRequestStarted() {
		Log.i(TAG, "onRequestStarted");

	}

	@Override
	public void onRequestProgressUpdate(int progress) {
		Log.i(TAG, "onRequestProgressUpdate");

	}

	@Override
	public void onRequestFinished(ArrayList<Article> articles, int result) {
		Log.i(TAG, "onRequestFinished: " + articles);
		
		if(result == RssLoaderFragment.NetworkRequestListener.RESULT_ERROR_CONNECTION){
			mFeedListFragment.error(getString(R.string.error_connection));
			return;
		}
		
		if(result == RssLoaderFragment.NetworkRequestListener.RESULT_ERROR_NETWORK_NOT_AVAILABLE){
			mFeedListFragment.error(getString(R.string.error_network_unavailable));
			return;
		}
		
		if(articles == null || articles.isEmpty()){
			mFeedListFragment.noMoreFeeds();
			return;
		}

		// Increase page so we grab the next one on the next load
		Data.getInstance().mCurrentPage++;
		Data.getInstance().mListArticles.addAll(articles);

		mFeedListFragment.addFeeds(articles);
	}
	/* END Callbacks from NetworkRequestListener*/
}
