package bowtie.dict.wrap.datamuse;

import java.util.List;

/**
 * @author &#8904
 *
 */
public class DatamuseCollection
{
    private String name;
    private List<String> results;

    public DatamuseCollection(String name, List<String> results)
    {
        this.name = name;
        this.results = results;
    }

    public String getName()
    {
        return this.name;
    }

    public List<String> getResults()
    {
        return this.results;
    }
}