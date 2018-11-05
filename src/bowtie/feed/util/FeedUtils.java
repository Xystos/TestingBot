package bowtie.feed.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.htmlcleaner.TagNode;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.guild.GuildObject;
import bowtie.feed.RssFeed;
import bowtie.feed.exc.FeedNotFoundException;
import bowtie.feed.xml.Item;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * It Reads and prints any RSS/Atom feed type.
 * <p>
 * 
 * @author Alejandro Abdelnur
 *
 */
public class FeedUtils
{
    private static final Pattern YOUTUBE_RSS_PATTERN = Pattern
            .compile("https://www.youtube.com/feeds/videos.xml\\?channel_id=([A-Za-z0-9_-]*)");
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern
            .compile("https://www.youtube.com/[(channel | user)]+/([A-Za-z0-9_-]*)");

    public static void main(String[] args)
    {
        System.out.println(getYouTubeFeedLink("https://www.youtube.com/user/sdtherzjdygaertwerwer/playlists"));
    }

    public static String getYouTubeFeedLink(String youtubeChannel)
    {
        if (!youtubeChannel.startsWith("https://"))
        {
            youtubeChannel = "https://" + youtubeChannel;
        }

        String feedURL = null;

        try
        {
            URL url = new URL(youtubeChannel);
            URLConnection con = url.openConnection();
            try (InputStream in = con.getInputStream())
            {
                String encoding = con.getContentEncoding();
                encoding = encoding == null ? "UTF-8" : encoding;
                String body = IOUtils.toString(in, encoding);

                Matcher mat = YOUTUBE_RSS_PATTERN.matcher(body);

                if (mat.find())
                {
                    String id = mat.group(1);
                    feedURL = "https://www.youtube.com/feeds/videos.xml?channel_id=" + id;
                }
            }
        }
        catch (Exception e)
        {
        }

        if (feedURL == null)
        {
            Matcher mat = YOUTUBE_ID_PATTERN.matcher(youtubeChannel);

            if (mat.find())
            {
                String id = mat.group(1);
                feedURL = "https://www.youtube.com/feeds/videos.xml?channel_id=" + id;
            }
        }

        return feedURL;
    }

    /**
     * For XML
     * 
     * @param inputString
     * @return
     * @throws FeedNotFoundException
     */
    public static RssFeed getFeedFromXml(String inputString) throws FeedNotFoundException
    {
        SyndFeed feed = null;
        String xml = null;
        SyndFeedInput input = new SyndFeedInput();
        StringReader reader;
        BufferedReader br;
        try
        {
            reader = new StringReader(inputString);
            br = new BufferedReader(new StringReader(inputString));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                builder.append(line + System.lineSeparator());
            }
            xml = builder.toString();
            xml = xml.replaceFirst("^([\\W]+)<", "<").replaceAll("[^\\x20-\\x7e]", "").trim();
            if (!xml.startsWith("<?xml"))
            {
                xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + xml;
            }

            HtmlCleaner cleaner = new HtmlCleaner();
            CleanerProperties props = cleaner.getProperties();
            xml = xml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
            xml = xml.replace("<br>", "<br></br>");
            TagNode node = cleaner.clean(xml);
            SimpleHtmlSerializer htmlSerializer = new SimpleHtmlSerializer(props);
            xml = htmlSerializer.getAsString(node).trim();
            xml = xml.replace("<head>", "")
                    .replace("</head>", "")
                    .replace("<body>", "")
                    .replace("</body>", "");

            if (!xml.startsWith("<html"))
            {
                xml = xml.replace("</html>", "");
            }
            xml = StringEscapeUtils.escapeHtml4(xml).trim();
            xml = xml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
            try
            {
                xml = XmlFormatter.format(xml);
            }
            catch (TransformerException e1)
            {
            }
            xml = StringEscapeUtils.unescapeHtml4(xml).trim();
            feed = input.build(reader);
            reader.close();
            br.close();
            return new RssFeed(xml, feed);
        }
        catch (Exception e)
        {
            throw new FeedNotFoundException("Could not create feed from the given XML.");
        }
    }

    /**
     * For URLs
     * 
     * @param inputString
     * @return
     * @throws FeedNotFoundException
     * @throws
     */
    public static RssFeed getFeedFromUrl(String inputString) throws FeedNotFoundException
    {
        if (!inputString.startsWith("https://"))
        {
            if (inputString.startsWith("http://") && inputString.length() >= 8)
            {
                try
                {
                    new URL(inputString);
                }
                catch (MalformedURLException e)
                {
                    inputString = inputString.substring(7);
                    inputString = "https://" + inputString;
                }
            }
            else if (inputString.startsWith("https://") && inputString.length() >= 9)
            {
                try
                {
                    new URL(inputString);
                }
                catch (MalformedURLException e)
                {
                    inputString = inputString.substring(8);
                    inputString = "http://" + inputString;
                }
            }
            else
            {
                try
                {
                    new URL("http://" + inputString);
                    inputString = "http://" + inputString;
                }
                catch (MalformedURLException e)
                {
                    inputString = "https://" + inputString;
                }
            }
        }
        SyndFeed feed = null;
        String xml = null;
        SyndFeedInput input = new SyndFeedInput();
        XmlReader reader = null;
        BufferedReader br = null;

        try (CloseableHttpClient client = HttpClients.createMinimal())
        {
            HttpUriRequest request = new HttpGet(inputString);
            request.setHeader("User-Agent", "Mozilla/5.0");

            try (CloseableHttpResponse response = client.execute(request);
                    InputStream stream = response.getEntity().getContent())
            {
                reader = new XmlReader(stream);
                br = new BufferedReader(new XmlReader(new URL(inputString)));
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = br.readLine()) != null)
                {
                    builder.append(line.trim() + System.lineSeparator());
                }
                xml = builder.toString();
                feed = input.build(reader);
                br.close();

                xml = xml.replaceFirst("^([\\W]+)<", "<").replaceAll("[^\\x20-\\x7e]", "").trim();
                HtmlCleaner cleaner = new HtmlCleaner();
                CleanerProperties props = cleaner.getProperties();
                xml = xml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
                xml = xml.replace("<br>", "<br></br>");
                TagNode node = cleaner.clean(xml);
                SimpleHtmlSerializer htmlSerializer = new SimpleHtmlSerializer(props);
                xml = htmlSerializer.getAsString(node).trim();
                xml = xml.replace("<head>", "")
                        .replace("</head>", "")
                        .replace("<body>", "")
                        .replace("</body>", "");
                if (!xml.startsWith("<?xml"))
                {
                    xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + xml;
                }
                xml = StringEscapeUtils.escapeHtml4(xml).trim();
                xml = xml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
                xml = XmlFormatter.format(xml);
                xml = StringEscapeUtils.unescapeHtml4(xml).trim();
                return new RssFeed(xml, feed, inputString);
            }
            catch (Exception e)
            {
                if (reader != null)
                {
                    reader.close();
                }
                if (br != null)
                {
                    br.close();
                }

                reader = new XmlReader(new URL(inputString));
                br = new BufferedReader(new XmlReader(new URL(inputString)));

                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = br.readLine()) != null)
                {
                    builder.append(line.trim() + System.lineSeparator());
                }
                xml = builder.toString();
                xml = xml.replaceFirst("^([\\W]+)<", "<").replaceAll("[^\\x20-\\x7e]", "").trim();
                HtmlCleaner cleaner = new HtmlCleaner();
                CleanerProperties props = cleaner.getProperties();
                xml = xml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
                xml = xml.replace("<br>", "<br></br>");
                TagNode node = cleaner.clean(xml);
                SimpleHtmlSerializer htmlSerializer = new SimpleHtmlSerializer(props);
                xml = htmlSerializer.getAsString(node).trim();

                xml = xml.replace("<head>", "")
                        .replace("</head>", "")
                        .replace("<body>", "")
                        .replace("</body>", "");
                if (!xml.startsWith("<?xml"))
                {
                    xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + xml;
                }
                xml = StringEscapeUtils.escapeHtml4(xml).trim();
                xml = xml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
                try
                {
                    xml = XmlFormatter.format(xml);
                }
                catch (TransformerException e1)
                {
                }
                xml = StringEscapeUtils.unescapeHtml4(xml).trim();
                feed = input.build(reader);
                return new RssFeed(xml, feed, inputString);
            }
        }
        catch (Exception e)
        {
            throw new FeedNotFoundException("Could not create feed from the given URL '" + inputString + "'.");
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    Bot.errorLog.print(e);
                }
            }
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    Bot.errorLog.print(e);
                }
            }
        }
    }

    public static EmbedObject getFeedEmbed(RssFeed feed, Item item, GuildObject guild, Color color,
            boolean withImage, boolean withTitle, boolean withIcon,
            boolean withFooter, boolean withText)
    {
        EmbedBuilder builder = new EmbedBuilder();

        if (feed.getTitle() != null && withTitle)
        {
            builder.withAuthorName(feed.getShortenedTitle(EmbedBuilder.AUTHOR_NAME_LIMIT));
            if (!feed.getLink().trim().isEmpty())
            {
                try
                {
                    String link = feed.getLink();
                    new URL(link);
                    builder.withAuthorUrl(link);
                }
                catch (Exception e)
                {

                }
            }
        }
        builder.withTitle(item.getShortenedTitle(EmbedBuilder.TITLE_LENGTH_LIMIT));
        try
        {
            String itemUrl = item.getImprovedLink();
            new URL(itemUrl);
            builder.withUrl(itemUrl);
        }
        catch (Exception e)
        {

        }

        if (withFooter)
        {
            builder.withFooterText(item.getDate(guild));
        }

        if (withImage && item.getImageURL() != null && imageExists(item.getImageURL()))
        {
            builder.withImage(item.getImageURL());
        }
        if (withIcon)
        {
            String imageUrl = feed.getImageUrl();
            if (imageUrl != null && imageExists(imageUrl))
            {
                builder.withThumbnail(imageUrl);
            }
        }

        if (withText)
        {
            builder.withDescription(item.getShortenedDescription(EmbedBuilder.DESCRIPTION_CONTENT_LIMIT));
        }
        builder.withColor(color);

        return builder.build();
    }

    public static EmbedObject getFeedEmbed(RssFeed feed, Item item, GuildObject guild, Color color)
    {
        return getFeedEmbed(feed, item, guild, color, true, true, true, true, true);
    }

    public static boolean imageExists(String URLName)
    {
        try
        {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection)new URL(URLName).openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e)
        {
            return false;
        }
    }
}