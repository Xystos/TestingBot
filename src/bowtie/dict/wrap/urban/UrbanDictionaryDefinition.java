package bowtie.dict.wrap.urban;

/**
 * @author &#8904
 *
 */
public class UrbanDictionaryDefinition
{
    private final String word;
    private final String definition;
    private final String example;
    private final String author;
    private final String permalink;
    private final int thumbsUp;
    private final int thumbsDown;
    private int index = -1;
    private int listSize = -1;

    public UrbanDictionaryDefinition(String word, String definition, String example, String author, String permalink,
            int thumbsUp, int thumbsDown)
    {
        this.word = word;
        this.definition = definition.replace("[", "***").replace("]", "***");
        this.example = example.replace("[", "***").replace("]", "***");
        this.author = author;
        this.permalink = permalink;
        this.thumbsDown = thumbsDown;
        this.thumbsUp = thumbsUp;
    }

    public void setListsize(int listSize)
    {
        this.listSize = listSize;
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

    public String getWord()
    {
        return this.word;
    }

    public String getDefinition()
    {
        return this.definition;
    }

    public String getExample()
    {
        return this.example;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public String getPermalink()
    {
        return this.permalink;
    }

    public int getThumbsUp()
    {
        return this.thumbsUp;
    }

    public int getThumbsDown()
    {
        return this.thumbsDown;
    }
}