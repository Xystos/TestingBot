package bowtie.feed;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import bowtie.feed.exc.FeedNotFoundException;
import bowtie.feed.exc.NoValidEntriesException;
import bowtie.feed.util.FeedUtils;
import bowtie.feed.util.Html2Text;
import bowtie.feed.xml.Item;
import bowtie.feed.xml.ItemSet;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;

/**
 * @author &#8904
 *
 */
public class RssFeed
{
    private List<SyndEntry> entries;
    private SyndFeed feed;
    private ItemSet itemSet;
    private String xml;
    private String link;
    private String title;

    public RssFeed(String xml, SyndFeed feed)
    {
        this.feed = feed;
        this.link = feed.getLink();
        this.title = feed.getTitle();
        this.entries = feed.getEntries();
        this.xml = xml;
    }

    public RssFeed(String xml, SyndFeed feed, String link)
    {
        this.feed = feed;
        this.link = link;
        this.title = feed.getTitle();
        this.entries = feed.getEntries();
        this.xml = xml;
    }

    public void setup() throws NoValidEntriesException
    {
        this.itemSet = new ItemSet(this.entries);
        // making sure the item tags are in separate lines
        this.xml = this.xml.replace("</item>", System.lineSeparator() + "</item>" + System.lineSeparator())
                .replace("<item>", System.lineSeparator() + "<item>" + System.lineSeparator())
                .replace("</entry>", System.lineSeparator() + "</entry>" + System.lineSeparator())
                .replace("<entry>", System.lineSeparator() + "<entry>" + System.lineSeparator())
                .replace("</title>", System.lineSeparator() + "</title>" + System.lineSeparator())
                .replace("</link>", System.lineSeparator() + "</link>" + System.lineSeparator())
                .replace("<link", System.lineSeparator() + "<link")
                .replace("<link>", System.lineSeparator() + "<link>")
                .replace("</guid>", System.lineSeparator() + "</guid>" + System.lineSeparator())
                .replace("<guid", System.lineSeparator() + "<guid")
                .replace("<img", System.lineSeparator() + "<img")
                .replace("<media:thumbnail", System.lineSeparator() + "<media:thumbnail")
                .replace("<thumbnail", System.lineSeparator() + "<thumbnail")
                .replace("&lt;", "<").replace("&gt;", ">");

        Map<String, String> itemXmlMap = new HashMap<>();
        String[] lines = this.xml.split(System.lineSeparator());
        String itemXml = "";
        String link = "";

        String linkTag = "<link";
        boolean foundAny = false;

        for (String line : lines)
        {
            if (!line.trim().isEmpty())
            {
                itemXml += line.trim() + System.lineSeparator();
            }
            if (line.trim().startsWith(linkTag + ">") || line.trim().startsWith(linkTag + " />"))
            {
                Html2Text html = new Html2Text();
                line = line.replace("<![CDATA[", "").replace("]]>", "");
                try
                {
                    html.parse(line);
                }
                catch (IOException e)
                {
                }
                link = StringEscapeUtils.unescapeHtml4(html.getText()).trim();
                continue;
            }
            if (line.trim().startsWith(linkTag) && line.contains("href=\""))
            {
                line = line.substring(line.toLowerCase().indexOf(linkTag), line.length());
                line = line.substring(line.toLowerCase().indexOf("href=\""), line.length());
                line = line.replace("href=\"", "");
                link = line.substring(0, line.indexOf("\""));
            }
            if (line.trim().equals("<item>") || line.trim().equals("<entry>"))
            {
                itemXml = "";
                link = "";
            }
            if (line.trim().equals("</item>") || line.trim().equals("</entry>"))
            {
                if (!link.trim().equals(""))
                {
                    itemXmlMap.put(link, itemXml);
                    foundAny = true;
                }
                itemXml = "";
                link = "";
            }
        }
        if (!foundAny && this.feed.getLink() == null)
        {
            throw new NoValidEntriesException("Could not find any valid entries.");
        }
        this.itemSet.distributeXml(itemXmlMap);
        this.itemSet.calcUpdateInterval();
    }

    public String getXml()
    {
        return this.xml;
    }

    public String getLink()
    {
        return this.link;
    }

    public String getTitle()
    {
        return StringEscapeUtils.unescapeHtml4(this.title).trim();
    }

    public String getShortenedTitle(int maxLength)
    {
        String result = this.getTitle();

        if (this.getTitle().length() > maxLength)
        {
            result = this.getTitle().substring(0, maxLength);
        }
        return result;
    }

    public int getUpdateInterval()
    {
        return this.itemSet.getUpdateInterval();
    }

    public void setUpdateInterval(int interval)
    {
        this.itemSet.setUpdateInterval(interval);
    }

    public int getAvrgInterval()
    {
        return this.itemSet.getAvrgInterval();
    }

    public ItemSet getItemSet()
    {
        return this.itemSet;
    }

    public Item getItem(String link)
    {
        return this.itemSet.getItem(link);
    }

    public String getImageUrl()
    {
        SyndImage image = this.feed.getImage() != null ? this.feed.getImage() : this.feed.getIcon();
        String url = null;

        if (image != null)
        {
            url = image.getUrl();
        }
        else
        {
            String tempXml = this.xml.replace("<", System.lineSeparator() + "<");
            String[] lines = tempXml.split(System.lineSeparator());

            for (String line : lines)
            {
                if (line.startsWith("<icon>"))
                {
                    String found = line.replace("<icon>", "").replace("<icon>", "");
                    url = found.isEmpty() ? null : found;
                    break;
                }
                else if (line.startsWith("<image>"))
                {
                    String found = line.replace("<image>", "").replace("<image>", "");
                    url = found.isEmpty() ? null : found;
                    break;
                }
            }
        }

        if (url != null && url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        if (url == null || url.trim().isEmpty() || !FeedUtils.imageExists(url))
        {
            url = "https://www.moz.de/fileadmin/_processed_/f/2/csm_2000px-Rss-feed.svg_69c07b2591.png";
        }
        return url;
    }

    public List<Item> update(String feedLink) throws FeedNotFoundException, NoValidEntriesException
    {
        this.link = feedLink;
        RssFeed feed = FeedUtils.getFeedFromUrl(feedLink);
        feed.setup();
        this.xml = feed.getXml();
        return this.itemSet.update(feed.getItemSet());
    }
}