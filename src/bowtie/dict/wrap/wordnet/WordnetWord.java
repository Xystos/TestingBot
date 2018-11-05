package bowtie.dict.wrap.wordnet;

import java.util.List;

/**
 * @author &#8904
 *
 */
public class WordnetWord
{
    private String word;
    private List<String> definitions;
    private String pos;
    private String link;
    private int listSize = -1;
    private int index = -1;

    public WordnetWord(String word, List<String> definitions, String pos, String link)
    {
        this.word = word;
        this.definitions = definitions;
        this.pos = pos;
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

    public String getWord()
    {
        return this.word;
    }

    public List<String> getDefinitions()
    {
        return this.definitions;
    }

    public String getLink()
    {
        return this.link;
    }

    public String getPOS()
    {
        return this.pos;
    }
}