package bigmikehoncho.com.rssreader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import bigmikehoncho.com.rssreader.content.Article;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RssLoaderFragment.NetworkRequestListener} interface
 * to handle interaction events.
 * 
 * Loads RSS Feed using AsyncTask.  Because of this it must retainInstance to avoid memory leaks
 */
public class RssLoaderFragment extends Fragment {
	private static final String TAG = RssLoaderFragment.class.getSimpleName();

	/*XML Strings for parsing*/
    private static final String XML_ITEM = "item";
	private static final String XML_TITLE = "title";
	private static final String XML_DESCRIPTION = "description";
	private static final String XML_PUB_DATE = "pubDate";
	private static final String XML_LINK = "link";

    private Context mContext;
    private NetworkTask mTask;
    private NetworkRequestListener mListener;
	
	private int mResultCode;
    private ArrayList<Article> mArticles;

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 */
	public interface NetworkRequestListener {
		/*Result code for callback*/
		int RESULT_OK = 0;
		int RESULT_ERROR_CONNECTION = 1;
		int RESULT_ERROR_NETWORK_NOT_AVAILABLE = 2;
		int RESULT_FILE_NOT_FOUND = 3;

		void onRequestStarted();

		void onRequestProgressUpdate(int progress);

		void onRequestFinished(ArrayList<Article> articles, int result);
	}

    public RssLoaderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        if (context instanceof NetworkRequestListener) {
            mListener = (NetworkRequestListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    /**
     * The Activity can call this when it wants to start the task
     */
    public void startTask(String url) {
		if(isNetworkAvailable()) {
			Log.i(TAG, "startTask: " + url);
			mTask = new NetworkTask();
			mTask.execute(url);
		} else {
			mListener.onRequestFinished(null, NetworkRequestListener.RESULT_ERROR_NETWORK_NOT_AVAILABLE);
		}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If the AsyncTask finished when we didn't have a listener we can
        // deliver the result here
        if ((mArticles != null) && (mListener != null)) {
            mListener.onRequestFinished(mArticles, NetworkRequestListener.RESULT_OK);
            mArticles = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // We still have to cancel the task in onDestroy because if the user exits the app or
        // finishes the Activity, we don't want the task to keep running
        // Since we are retaining the Fragment, onDestroy won't be called for an orientation change
        // so this won't affect our ability to keep the task running when the user rotates the device
        if ((mTask != null) && (mTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mTask.cancel(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // This is VERY important to avoid a memory leak (because mListener is really a reference to an Activity)
        // When the orientation change occurs, onDetach will be called and since the Activity is being destroyed
        // we don't want to keep any references to it
        // When the Activity is being re-created, onAttach will be called and we will get our listener back
        mListener = null;
    }
	
	/**
	 * Quick check to see if we are connected to the internet
	 * */
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**Background task to load feeds*/
    private class NetworkTask extends AsyncTask<String, Void, ArrayList<Article>> {

        @Override
        protected void onPreExecute() {
            if (mListener != null) {
                mListener.onRequestStarted();
            }
        }

        @Override
        protected ArrayList<Article> doInBackground(String... urls) {

            return processXml(downloadData(urls[0]));
        }

        @Override
        protected void onPostExecute(ArrayList<Article> result) {
            if (mListener != null) {
                mListener.onRequestFinished(result, mResultCode);
            } else {
                // If the task finishes while the orientation change is happening and while
                // the Fragment is not attached to an Activity, our mListener might be null
                // If you need to make sure that the result eventually gets to the Activity
                // you could save the result here, then in onActivityCreated you can pass it back
                // to the Activity
                mArticles = result;
            }
        }

		/**
		 * Take a document and return a list of Articles
		 * */
        private ArrayList<Article> processXml(Document data) {
			ArrayList<Article> articles = new ArrayList<>();
            if (data != null) {
                Element root = data.getDocumentElement();
                Node channel = root.getChildNodes().item(1);
                NodeList items = channel.getChildNodes();
                for (int i = 0; i < items.getLength(); i++) {
                    Node currentChild = items.item(i);
                    if (currentChild.getNodeName().equalsIgnoreCase(XML_ITEM)) {
                        Article item = new Article();
                        NodeList itemChilds = currentChild.getChildNodes();
                        for (int j = 0; j < itemChilds.getLength(); j++) {
                            Node current = itemChilds.item(j);
                            if (current.getNodeName().equalsIgnoreCase(XML_TITLE)) {
                                item.setTitle(current.getTextContent());
                            } else if (current.getNodeName().equalsIgnoreCase(XML_DESCRIPTION)) {
                                item.setDescription(current.getTextContent());
                            } else if (current.getNodeName().equalsIgnoreCase(XML_PUB_DATE)) {
                                item.setPubDate(current.getTextContent());
                            } else if (current.getNodeName().equalsIgnoreCase(XML_LINK)) {
                                item.setLink(current.getTextContent());
                            }
                        }
                        articles.add(item);
                    }
                }
            }

            return articles;
        }

		/**
		 * Here's the actual network call.
		 * Must be called off the UI thread.
		 * */
        public Document downloadData(String address) {
            try {
                URL url = new URL(address);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
				Document document = builder.parse(inputStream);
				mResultCode = NetworkRequestListener.RESULT_OK;
                return document;
            } catch (MalformedURLException | ParserConfigurationException | SAXException | ProtocolException e) {
				mResultCode = NetworkRequestListener.RESULT_ERROR_CONNECTION;
				e.printStackTrace();
            } catch (FileNotFoundException fnfe){
				mResultCode = NetworkRequestListener.RESULT_FILE_NOT_FOUND;
				fnfe.printStackTrace();
			} catch (IOException e) {
				mResultCode = NetworkRequestListener.RESULT_ERROR_CONNECTION;
				e.printStackTrace();
			}

			return null;
		}

    }
}
