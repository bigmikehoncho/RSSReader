package bigmikehoncho.com.rssreader;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import bigmikehoncho.com.rssreader.content.Data;
import bigmikehoncho.com.rssreader.content.Article;

public class MainActivity extends AppCompatActivity implements ArticlesFragment.OnListFragmentInteractionListener,
		RssLoaderFragment.NetworkRequestListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	public static final String EXTRA_URL = "url";
	private static final String TAG_LOADER_FRAGMENT = "loader";
	private static final String TAG_FEED_LIST_FRAGMENT = "feedList";

	private RssLoaderFragment mLoaderFragment;
	private ArticlesFragment mArticlesFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		if(intent != null){
			Data.getInstance().mUrl = intent.getStringExtra(EXTRA_URL);
		}

		FragmentManager fm = getSupportFragmentManager();
		mLoaderFragment = (RssLoaderFragment) fm.findFragmentByTag(TAG_LOADER_FRAGMENT);
		mArticlesFragment = (ArticlesFragment) fm.findFragmentByTag(TAG_FEED_LIST_FRAGMENT);

		if (mLoaderFragment == null) {
			// RssLoaderFragment has not been retained so we must create it anew
			mLoaderFragment = new RssLoaderFragment();

			fm.beginTransaction()
					.add(mLoaderFragment, TAG_LOADER_FRAGMENT)
					.commit();
		}
		if (mArticlesFragment == null) {
			// FeedListFragment has not been retained so we must create it anew
			mArticlesFragment = ArticlesFragment.newInstance(new ArrayList<Article>(), 1);
			fm.beginTransaction()
					// It's almost always a good idea to use .replace instead of .add so that
					// you never accidentally layer multiple Fragments on top of each other
					// unless of course that's your intention
					.replace(R.id.container, mArticlesFragment, TAG_FEED_LIST_FRAGMENT)
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

		mLoaderFragment.startTask(Data.getInstance().mUrl + Constants.URL_PAGE + Data.getInstance().mCurrentPage);
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
			mArticlesFragment.error(getString(R.string.error_connection));
			return;
		}
		
		if(result == RssLoaderFragment.NetworkRequestListener.RESULT_ERROR_NETWORK_NOT_AVAILABLE){
			mArticlesFragment.error(getString(R.string.error_network_unavailable));
			return;
		}
		
		if(articles == null || articles.isEmpty()){
			mArticlesFragment.noMoreFeeds();
			return;
		}

		// Increase page so we grab the next one on the next load
		Data.getInstance().mCurrentPage++;
		Data.getInstance().mListArticles.addAll(articles);

		mArticlesFragment.addFeeds(articles);
	}
	/* END Callbacks from NetworkRequestListener*/
}
