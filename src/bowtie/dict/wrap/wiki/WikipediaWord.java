package bowtie.dict.wrap.wiki;

/**
 * @author &#8904
 *
 */
public class WikipediaWord
{
    private String title;
    private String content;
    private String image;
    private String link;
    private int listSize = -1;
    private int index = -1;

    public WikipediaWord(String title, String content, String link, String image)
    {
        this.title = title;
        this.content = content;
        this.image = image;
        this.link = link;
    }

    public void setListsize(int size)
    {
        this.listSize = size;
    }

    public int getListsize()
    {
        return this.listSize;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return this.index;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getContent()
    {
        return this.content;
    }

    public String getImage()
    {
        return this.image;
    }

    public String getLink()
    {
        return this.link;
    }
}