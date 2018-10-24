package mzy.stockmarketviewer;

public class NewsModel {
    private String title = "";
    private String link = "";
    private String author = "";
    private String date = "";

    public String getTitle() {
        return title;
    }
    public String getLink() {
        return link;
    }
    public String getAuthor() {
        return author;
    }
    public String getDate() {
        return date;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
