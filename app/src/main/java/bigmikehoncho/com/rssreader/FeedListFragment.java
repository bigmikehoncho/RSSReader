package bigmikehoncho.com.rssreader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import bigmikehoncho.com.rssreader.content.Article;

/**
 * A fragment representing a list of Articles.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FeedListFragment extends Fragment {
	private static final String TAG = FeedListFragment.class.getSimpleName();

	private static final String ARG_FEEDS_LIST = "feeds";
	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final String STATE_FEEDS_LIST = "feeds";
	private static final String STATE_WARNING_TEXT = "warning";
	private static final String STATE_LIST = "state";

	public static final int LIST_STATE_DEFAULT = 0;
	public static final int LIST_STATE_LOADING = 1;
	public static final int LIST_STATE_LAST_PAGE = 2;
	public static final int LIST_STATE_ERROR = 3;

	private Context mContext;
	private ArrayList<Article> mListArticles = new ArrayList<>();
	private int mColumnCount = 1;
	private int mListState = LIST_STATE_DEFAULT;
	private OnListFragmentInteractionListener mListener;

	private ProgressBar mProgress;
	private TextView mTVWarning;
	private RecyclerView mRecyclerView;
	private MyPageRecyclerViewAdapter mAdapter;
	private LinearLayoutManager mLayoutManager;

	// Callback to check when RecyclerView has reached the bottom
	private RecyclerView.OnScrollListener mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			
			if (mListState != LIST_STATE_LAST_PAGE && mListState != LIST_STATE_LOADING) {
				int totalItemCount = mLayoutManager.getItemCount();
				if (mLayoutManager.findLastCompletelyVisibleItemPosition() == totalItemCount - 1) {
					loadMoreFeeds();
				}
			}
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FeedListFragment() {
	}

	@SuppressWarnings("unused")
	public static FeedListFragment newInstance(ArrayList<Article> listArticle, int columnCount) {
		FeedListFragment fragment = new FeedListFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_FEEDS_LIST, listArticle);
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_FEEDS_LIST, mListArticles);
		outState.putInt(STATE_LIST, mListState);
		outState.putString(STATE_WARNING_TEXT, mTVWarning.getText().toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mListArticles = (ArrayList<Article>) args.getSerializable(ARG_FEEDS_LIST);
			args.remove(ARG_FEEDS_LIST);
			mColumnCount = args.getInt(ARG_COLUMN_COUNT);
		}
		if (savedInstanceState != null) {
			mListArticles = (ArrayList<Article>) savedInstanceState.getSerializable(STATE_FEEDS_LIST);
			mListState = savedInstanceState.getInt(STATE_LIST);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_page_list, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mProgress = (ProgressBar) view.findViewById(R.id.progress);
		mTVWarning = (TextView) view.findViewById(R.id.warning_bar);
		mAdapter = new MyPageRecyclerViewAdapter(mListArticles, mListener);
		mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
		if (mColumnCount <= 1) {
			mLayoutManager = new LinearLayoutManager(mContext);
		} else {
			mLayoutManager = new GridLayoutManager(mContext, mColumnCount);
		}
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

		if (savedInstanceState != null) {
			mTVWarning.setText(savedInstanceState.getString(STATE_WARNING_TEXT));
		}
		setByState();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mContext = context;
		if (context instanceof OnListFragmentInteractionListener) {
			mListener = (OnListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/*Set the UI based on the current state of the list*/
	private void setByState() {
		switch (mListState) {
			case LIST_STATE_DEFAULT:
				mProgress.setVisibility(View.INVISIBLE);
				mTVWarning.setVisibility(View.GONE);
				break;
			case LIST_STATE_LOADING:
				mProgress.setVisibility(View.VISIBLE);
				mTVWarning.setVisibility(View.GONE);
				break;
			case LIST_STATE_LAST_PAGE:
				mAdapter.removeLoading();
				mProgress.setVisibility(View.INVISIBLE);
				mTVWarning.setVisibility(View.GONE);
				break;
			case LIST_STATE_ERROR:
				mProgress.setVisibility(View.INVISIBLE);
				mTVWarning.setVisibility(View.VISIBLE);
				break;
		}

	}

	/*Public method to add more feed items*/
	public void addFeeds(ArrayList<Article> articles) {
		mListArticles.addAll(articles);
		mAdapter.addFeedItems(articles);

		mListState = LIST_STATE_DEFAULT;
		setByState();
	}

	/*No more feed items available. Stop showing loader UI*/
	public void noMoreFeeds() {
		mAdapter.removeLoading();

		mListState = LIST_STATE_LAST_PAGE;
		setByState();
	}

	/*Error in grabbing the feed. Show appropriate message*/
	public void error(String errorMessage) {
		Log.i(TAG, "error: " + errorMessage);
		mTVWarning.setText(errorMessage);

		mListState = LIST_STATE_ERROR;
		setByState();
	}

	private void loadMoreFeeds() {
		if (mListener != null) {
			mListState = LIST_STATE_LOADING;
			setByState();
			mListener.onLoadNext();
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 */
	public interface OnListFragmentInteractionListener {
		void onListFragmentInteraction(Article article);

		void onLoadNext();
	}
}
