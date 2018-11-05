package bowtie.dict.wrap.wiki;

/**
 * @author &#8904
 *
 */
public class WikipediaHyperlink
{
    private String anchor;
    private String link;

    public WikipediaHyperlink(String anchor, String link)
    {
        this.anchor = anchor;
        this.link = link;
    }

    public String getAnchor()
    {
        return this.anchor;
    }

    public String getLink()
    {
        return this.link;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof WikipediaHyperlink)
        {
            if (this.anchor.equals(((WikipediaHyperlink)o).getAnchor())
                    && this.link.equals(((WikipediaHyperlink)o).getLink()))
            {
                return true;
            }
        }
        return false;
    }
}