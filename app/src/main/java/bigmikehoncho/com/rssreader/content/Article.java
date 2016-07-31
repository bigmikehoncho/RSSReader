package bigmikehoncho.com.rssreader.content;

import java.io.Serializable;

/**
 * Created by Mike on 7/30/2016.
 */
public class Article implements Serializable{

    String title;
    String link;
    String description;
    String pubDate;
    String thumbnailUrl;

    @Override
    public String toString() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
