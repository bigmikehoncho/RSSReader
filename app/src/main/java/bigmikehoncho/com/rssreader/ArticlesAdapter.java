package bigmikehoncho.com.rssreader;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import bigmikehoncho.com.rssreader.ArticlesFragment.OnListFragmentInteractionListener;
import bigmikehoncho.com.rssreader.content.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Article} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * 
 * Adapter for displaying a list of Articles
 */
public class ArticlesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int ARTICLE = 1;
	public static final int LOADING = 2;

	private List<Article> mValues = new ArrayList<>();
	private final OnListFragmentInteractionListener mListener;

	private int mLoaderCount = 1;

	public ArticlesAdapter(List<Article> items, OnListFragmentInteractionListener listener) {
		mValues.addAll(items);
		mListener = listener;
	}

	public void addFeedItems(List<Article> articleList) {
		int size = mValues.size();
		mValues.addAll(articleList);
		notifyItemRangeChanged(size, articleList.size());
	}

	public void removeLoading() {
		mLoaderCount = 0;
		notifyItemRemoved(mValues.size());
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder = null;
		switch (viewType) {
			case ARTICLE:
				holder = createFeedViewHolder(parent);
				break;
			case LOADING:
				holder = createLoadingViewHolder(parent);
		}
		return holder;
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof FeedViewHolder) {
			bindFeedViewHolder(holder, position);
		} else if (holder instanceof LoadingViewHolder) {
			bindLoadingViewHolder(holder);
		}
	}

	private RecyclerView.ViewHolder createFeedViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);

		return new FeedViewHolder(v);
	}

	private RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);

		return new LoadingViewHolder(v);
	}

	private void bindFeedViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		final FeedViewHolder holder = (FeedViewHolder) viewHolder;

		Article currentArticle = mValues.get(position);
		holder.mArticle = currentArticle;
		holder.mTVTitle.setText(currentArticle.getTitle());
		holder.mTVDate.setText(currentArticle.getPubDate());

		holder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mListener) {
					// Notify the active callbacks interface (the activity, if the
					// fragment is attached to one) that an item has been selected.
					mListener.onListFragmentInteraction(holder.mArticle);
				}
			}
		});
	}

	private void bindLoadingViewHolder(RecyclerView.ViewHolder viewHolder) {
		
	}

	@Override
	public int getItemCount() {
		return mValues.size() + mLoaderCount;
	}

	@Override
	public int getItemViewType(int position) {
		return (position >= mValues.size() && mLoaderCount > 0) ? LOADING : ARTICLE;
	}

	public class FeedViewHolder extends RecyclerView.ViewHolder {
		public final View mView;
		public final TextView mTVTitle;
		public final TextView mTVDate;
		public Article mArticle;

		public FeedViewHolder(View view) {
			super(view);
			mView = view;
			mTVTitle = (TextView) view.findViewById(R.id.article_title);
			mTVDate = (TextView) view.findViewById(R.id.article_date);
		}

		@Override
		public String toString() {
			return super.toString() + " '" + mTVDate.getText() + "'";
		}
	}

	public class LoadingViewHolder extends RecyclerView.ViewHolder {
		ImageView mIVLoading;

		public LoadingViewHolder(View itemView) {
			super(itemView);
		}
	}
}
