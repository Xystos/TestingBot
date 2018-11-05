package bowtie.feed.xml;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;

import bowt.guild.GuildObject;
import bowt.util.regio.Region;
import bowtie.feed.util.Html2Text;

/**
 * @author &#8904
 *
 */
public class Item
{
    private SyndEntry entry;
    private String xml;
    private String imageURL;
    private String description;
    private Date date;
    private boolean hasDateTags = true;

    public Item(SyndEntry entry)
    {
        this.date = entry.getUpdatedDate();
        if (this.date == null)
        {
            this.date = entry.getPublishedDate();
        }
        if (this.date == null)
        {
            this.hasDateTags = false;
            this.date = new Date();
        }

        this.entry = entry;
        SyndContent syndDesc = this.entry.getDescription();
        if (syndDesc != null)
        {
            this.description = syndDesc.getValue();
            if (this.description != null)
            {
                // because fucking escaped tags for what ever shitty reason
                this.description = StringEscapeUtils.unescapeHtml4(this.description).trim();
                this.description = this.description.replace(" />", "/>");
                this.description = this.description.replace("&apos;", "’");

                Html2Text html = new Html2Text();
                try
                {
                    html.parse(this.description);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                this.description = StringEscapeUtils.unescapeHtml4(html.getText()).trim();
            }
        }
    }

    public boolean hasDateTags()
    {
        return this.hasDateTags;
    }

    public void setXml(String xml)
    {
        this.xml = xml;
        if (this.xml != null)
        {
            this.xml = StringEscapeUtils.unescapeHtml4(this.xml).trim();
            findImage();
        }
        if (this.description == null)
        {
            findDescription();
        }
        if (this.description != null)
        {
            String[] lines = this.description.replace(System.lineSeparator(), " ").split(" ");

            for (String line : lines)
            {
                if (line.trim().startsWith("http"))
                {
                    line = "[" + line + "](" + line + ")";
                }
            }
        }
    }

    private void findImage()
    {
        if (this.xml != null && this.imageURL == null)
            ;
        {
            String[] lines = this.xml.replace("<![CDATA[", "").replace("]]>", "")
                    .replace("<", System.lineSeparator() + "<").split(System.lineSeparator());

            for (String line : lines)
            {
                if (line.toLowerCase().contains("<img") && line.toLowerCase().contains("src"))
                {
                    line = line.replace("'", "\"");
                    line = line.substring(line.toLowerCase().indexOf("<img"), line.length());
                    line = line.substring(line.toLowerCase().indexOf("src=\""), line.length());
                    line = line.replace("src=\"", "");
                    line = line.substring(0, line.indexOf("\""));
                    this.imageURL = line;
                    break;
                }
            }

            if (this.imageURL == null)
            {
                for (String line : lines)
                {
                    if (line.toLowerCase().contains("<media:thumbnail") && line.toLowerCase().contains("url"))
                    {
                        line = line.replace("'", "\"");
                        line = line.substring(line.toLowerCase().indexOf("<media:thumbnail"), line.length());
                        line = line.substring(line.toLowerCase().indexOf("url=\""), line.length());
                        line = line.replace("url=\"", "");
                        line = line.substring(0, line.indexOf("\""));
                        this.imageURL = line;

                        if (this.imageURL.toLowerCase().endsWith(".jpg")
                                || this.imageURL.toLowerCase().endsWith(".png")
                                || this.imageURL.toLowerCase().endsWith(".jpeg")
                                || this.imageURL.toLowerCase().endsWith(".gif"))
                        {
                            break;
                        }
                    }
                }
            }

            if (this.imageURL == null)
            {
                for (String line : lines)
                {
                    if (line.toLowerCase().contains("<thumbnail>"))
                    {
                        line = line.replace("<thumbnail>", "").replace("</thumbnail>", "");
                        this.imageURL = line;
                        if (this.imageURL.toLowerCase().endsWith(".jpg")
                                || this.imageURL.toLowerCase().endsWith(".png")
                                || this.imageURL.toLowerCase().endsWith(".jpeg")
                                || this.imageURL.toLowerCase().endsWith(".gif"))
                        {
                            break;
                        }
                    }
                }
            }

            if (this.imageURL == null)
            {
                if (getLink().toLowerCase().endsWith(".jpg")
                        || getLink().toLowerCase().endsWith(".png")
                        || getLink().toLowerCase().endsWith(".jpeg")
                        || getLink().toLowerCase().endsWith(".gif"))
                {
                    this.imageURL = getLink();
                }
            }

            if (this.imageURL == null)
            {
                for (String line : lines)
                {
                    if (line.toLowerCase().contains(".jpg")
                            || line.toLowerCase().contains(".png")
                            || line.toLowerCase().contains(".jpeg")
                            || line.toLowerCase().contains(".gif"))
                    {
                        line = line.replace("'", "\"");
                        if (line.contains("src=\""))
                        {
                            line = line.substring(line.toLowerCase().indexOf("src=\""), line.length());
                            line = line.replace("src=\"", "");
                            line = line.substring(0, line.indexOf("\""));
                            this.imageURL = line;
                            if (this.imageURL.toLowerCase().endsWith(".jpg")
                                    || this.imageURL.toLowerCase().endsWith(".png")
                                    || this.imageURL.toLowerCase().endsWith(".jpeg")
                                    || this.imageURL.toLowerCase().endsWith(".gif"))
                            {
                                break;
                            }
                        }
                        else if (line.contains("url=\""))
                        {
                            line = line.substring(line.toLowerCase().indexOf("url=\""), line.length());
                            line = line.replace("url=\"", "");
                            line = line.substring(0, line.indexOf("\""));
                            this.imageURL = line;
                            if (this.imageURL.toLowerCase().endsWith(".jpg")
                                    || this.imageURL.toLowerCase().endsWith(".png")
                                    || this.imageURL.toLowerCase().endsWith(".jpeg")
                                    || this.imageURL.toLowerCase().endsWith(".gif"))
                            {
                                break;
                            }
                        }
                        else if (line.contains("href=\""))
                        {
                            line = line.substring(line.toLowerCase().indexOf("href=\""), line.length());
                            line = line.replace("href=\"", "");
                            line = line.substring(0, line.indexOf("\""));
                            this.imageURL = line;
                            if (this.imageURL.toLowerCase().endsWith(".jpg")
                                    || this.imageURL.toLowerCase().endsWith(".png")
                                    || this.imageURL.toLowerCase().endsWith(".jpeg")
                                    || this.imageURL.toLowerCase().endsWith(".gif"))
                            {
                                break;
                            }
                        }
                        else
                        {
                            Html2Text html = new Html2Text();
                            try
                            {
                                html.parse(line);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            this.imageURL = StringEscapeUtils.unescapeHtml4(html.getText()).trim();
                            if (this.imageURL.toLowerCase().endsWith(".jpg")
                                    || this.imageURL.toLowerCase().endsWith(".png")
                                    || this.imageURL.toLowerCase().endsWith(".jpeg")
                                    || this.imageURL.toLowerCase().endsWith(".gif"))
                            {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void findDescription()
    {
        List<SyndContent> contents = this.entry.getContents();

        if (contents != null && !contents.isEmpty())
        {
            this.description = contents.get(0).getValue();
            if (this.description != null)
            {
                this.description = StringEscapeUtils.unescapeHtml4(this.description).trim();
                this.description = this.description.replace(" />", "/>");
                this.description = this.description.replace("&apos;", "’");

                Html2Text html = new Html2Text();
                try
                {
                    html.parse(this.description);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                this.description = StringEscapeUtils.unescapeHtml4(html.getText()).trim();
            }
        }

        if (description == null && this.xml != null)
        {
            if (this.xml.contains("<media:description>") && this.xml.contains("</media:description>"))
            {
                this.description = this.xml.substring(this.xml.indexOf("<media:description>"),
                        this.xml.indexOf("</media:description>"));
                this.description = this.description.replace("<media:description>", "").replace("</media:description>",
                        "");
            }
        }

        if (description == null)
        {
            this.description = "[...](" + getImprovedLink() + ")";
        }
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getShortenedDescription(int maxLength)
    {
        String result = this.description;

        if (result == null)
        {
            result = "";
        }

        if (this.description.length() > maxLength)
        {
            String readMore = "... [Read more](" + getLink() + ")";
            String shortText = this.description.substring(0, maxLength - readMore.length());
            result = shortText + readMore;
        }
        return result;
    }

    public String getTitle()
    {
        return StringEscapeUtils.unescapeHtml4(this.entry.getTitle()).trim();
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

    public String getUrlTitle(int maxLength)
    {
        String result = "[" + this.getTitle();
        String hyperLink = "](" + this.getLink() + ")";

        if (result.length() + hyperLink.length() > maxLength)
        {
            String shortText = result.substring(0, maxLength - hyperLink.length());
            result = shortText;
        }
        return result + hyperLink;
    }

    public String getImageURL()
    {
        return this.imageURL;
    }

    public String getRawLink()
    {
        return this.entry.getLink().trim();
    }

    public String getLink()
    {
        String link = this.entry.getLink().trim();
        if (!link.startsWith("https://"))
        {
            if (link.startsWith("http://") && link.length() >= 8)
            {
                try
                {
                    new URL(link);
                }
                catch (MalformedURLException e)
                {
                    link = link.substring(7);
                    link = "https://" + link;
                }
            }
            else if (link.startsWith("https://") && link.length() >= 9)
            {
                try
                {
                    new URL(link);
                }
                catch (MalformedURLException e)
                {
                    link = link.substring(8);
                    link = "http://" + link;
                }
            }
            else
            {
                try
                {
                    new URL("http://" + link);
                    link = "http://" + link;
                }
                catch (MalformedURLException e)
                {
                    link = "https://" + link;
                }
            }
        }
        return link;
    }

    public String getImprovedLink()
    {
        if (this.xml != null &&
                this.xml.contains("<guid") &&
                (getLink().toLowerCase().endsWith(".jpg")
                        || getLink().toLowerCase().endsWith(".png")
                        || getLink().toLowerCase().endsWith(".jpeg")
                        || getLink().toLowerCase().endsWith(".gif")))
        {
            String[] lines = this.xml.split(System.lineSeparator());

            for (String line : lines)
            {
                if (line.startsWith("<guid"))
                {
                    line = line.substring(line.indexOf(">") + 1);
                    line = line.replace("</guid>", "").trim();
                    return line;
                }
            }
        }

        return getRawLink();
    }

    public SyndEntry getEntry()
    {
        return this.entry;
    }

    public Date getDate()
    {
        return this.date;
    }

    public String getXml()
    {
        return this.xml;
    }

    public String getDate(GuildObject guild)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date);
        SimpleDateFormat sdf = new SimpleDateFormat("E dd MMM hh:mm:ss zzz yyyy");
        sdf.setTimeZone(Region.getTimeZone(guild.getGuild()));
        return sdf.format(calendar.getTime());
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Item)
        {
            Item item = (Item)o;

            if (this.getLink().equals(item.getLink()))
            {
                return true;
            }
        }
        return false;
    }
}