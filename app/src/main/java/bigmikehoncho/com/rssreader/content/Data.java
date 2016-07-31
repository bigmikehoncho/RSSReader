package bigmikehoncho.com.rssreader.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class for storing application-wide data
 */
public class Data {
    private static Data ourInstance = new Data();

    public int mCurrentPage = 1;
    public List<Article> mListArticles = new ArrayList<>();
	public String mUrl;

    public static Data getInstance() {
        return ourInstance;
    }

    private Data() {
    }
	
	public void clear(){
		mCurrentPage = 1;
		mListArticles = new ArrayList<>();
		mUrl = "";
	}
}
