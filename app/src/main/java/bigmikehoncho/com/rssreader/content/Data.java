package bigmikehoncho.com.rssreader.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mike on 7/30/2016.
 */
public class Data {
    private static Data ourInstance = new Data();

    public int mCurrentPage = 0;
    public List<Article> mListArticles = new ArrayList<>();

    public static Data getInstance() {
        return ourInstance;
    }

    private Data() {
    }
}
