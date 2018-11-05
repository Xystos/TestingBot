package bowtie.dict.util;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import rita.RiWordNet;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cons.Colors;
import bowtie.dict.Dictionary;
import bowtie.dict.wrap.datamuse.DatamuseCollection;
import bowtie.dict.wrap.urban.UrbanDictionaryDefinition;
import bowtie.dict.wrap.wiki.WikipediaHyperlink;
import bowtie.dict.wrap.wiki.WikipediaWord;
import bowtie.dict.wrap.wordnet.WordnetWord;

/**
 * @author &#8904
 *
 */
public final class DictionaryUtils
{

    public static EmbedObject embedDatamuseCollection(DatamuseCollection col)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(col.getName());

        for (String s : col.getResults())
        {
            builder.appendField(".", s, true);
        }

        builder.withColor(Color.YELLOW.brighter());

        return builder.build();
    }

    public static EmbedObject embedUrbanWord(UrbanDictionaryDefinition def)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(def.getWord());
        builder.withAuthorUrl(def.getPermalink());
        builder.withFooterText("Definition " + def.getIndex() + "/" + def.getListsize() + " by '" + def.getAuthor()
                + "'");
        String definition = def.getDefinition();

        if (definition.length() > EmbedBuilder.DESCRIPTION_CONTENT_LIMIT)
        {
            String readMore = "[Read more](" + def.getPermalink() + ")";
            definition = definition.substring(0, EmbedBuilder.DESCRIPTION_CONTENT_LIMIT - (readMore.length() + 5))
                    + "... " + readMore;
        }
        builder.withDescription(definition);

        String example = def.getExample();

        if (example.length() > EmbedBuilder.FIELD_CONTENT_LIMIT)
        {
            String readMore = "[Read more](" + def.getPermalink() + ")";
            example = example.substring(0, EmbedBuilder.FIELD_CONTENT_LIMIT - (readMore.length() + 5)) + "... "
                    + readMore;
        }

        if (!example.isEmpty())
        {
            builder.appendField("**Example**", example + "\n", false);
        }
        builder.appendField("Votes", ":thumbsup: " + def.getThumbsUp() + "    :thumbsdown: " + def.getThumbsDown(),
                false);
        builder.withColor(Colors.ORANGE);

        return builder.build();
    }

    public static EmbedObject embedWordnetWord(WordnetWord word)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(word.getWord());
        builder.withAuthorUrl(word.getLink());

        String pos = "";

        switch (word.getPOS())
        {
        case RiWordNet.NOUN:
            pos = "Noun";
            break;
        case RiWordNet.VERB:
            pos = "Verb";
            break;
        case RiWordNet.ADJ:
            pos = "Adjective";
            break;
        case RiWordNet.ADV:
            pos = "Adverb";
            break;
        }
        builder.withTitle(pos);
        builder.withColor(Color.BLUE);
        builder.withFooterText("Result " + word.getIndex() + "/" + word.getListsize());

        for (int i = 0; i < word.getDefinitions().size(); i ++ )
        {
            builder.appendField(".", word.getDefinitions().get(i), false);
        }

        return builder.build();
    }

    public static EmbedObject embedWikipediaWord(WikipediaWord word)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(word.getTitle());
        builder.withAuthorUrl(word.getLink());
        builder.withThumbnail(word.getImage());
        builder.withFooterText("Result " + word.getIndex() + "/" + word.getListsize());
        builder.withColor(Color.WHITE);

        String[] paragraphs = word.getContent().split("\n");
        int startIndex = 0;

        if (paragraphs[0].length() <= EmbedBuilder.DESCRIPTION_CONTENT_LIMIT)
        {
            builder.withDescription(cleanUpWikipediaHyperlinks(paragraphs[0]));
            startIndex = 1;
        }

        for (int i = startIndex; i < paragraphs.length; i ++ )
        {
            if (paragraphs[i].length() + builder.getTotalVisibleCharacters() <= EmbedBuilder.MAX_CHAR_LIMIT)
            {
                if (paragraphs[i].length() > EmbedBuilder.FIELD_CONTENT_LIMIT)
                {
                    String[] words = paragraphs[i].split(" ");

                    String field = "";

                    for (int j = 0; j < words.length; j ++ )
                    {
                        if (field.length() + words[j].length() < EmbedBuilder.FIELD_CONTENT_LIMIT)
                        {
                            if (words[j].contains("[") && words[j].contains("]") && words[j].contains("(")
                                    && words[j].contains(")"))
                            {
                                field += cleanUpWikipediaHyperlinks(words[j]) + " ";
                            }
                            else
                            {
                                field += words[j] + " ";
                            }
                        }
                        else
                        {
                            builder.appendField(".", field.trim(), false);
                            field = "";
                            j -- ;
                        }
                    }
                    builder.appendField(".", field.trim(), false);
                }
                else
                {
                    if (!paragraphs[i].isEmpty())
                    {
                        builder.appendField(".", cleanUpWikipediaHyperlinks(paragraphs[i]), false);
                    }
                }
            }
        }

        return builder.build();
    }

    public static String insertWikipediaHyperlinks(String text, List<WikipediaHyperlink> links)
    {
        for (WikipediaHyperlink link : links)
        {
            try
            {
                text = text.replace(link.getAnchor(),
                        "[" + URLEncoder.encode(link.getAnchor(), "UTF-8") + "](" + link.getLink() + ")");
            }
            catch (UnsupportedEncodingException e)
            {
                Bot.errorLog.print(e);
            }
        }

        return text;
    }

    public static String cleanUpWikipediaHyperlinks(String text)
    {
        String cleaned = "";
        String[] words = text.split(" ");

        for (int i = 0; i < words.length; i ++ )
        {
            if (words[i].contains("[") && words[i].contains("]") && words[i].contains("(") && words[i].contains(")"))
            {
                words[i] = words[i].replace("+", " ");
            }
        }

        for (int i = 0; i < words.length; i ++ )
        {
            cleaned += words[i] + " ";
        }
        cleaned = cleaned.trim();

        return cleaned;
    }

    public static List<WikipediaHyperlink> getWikipediaHyperlinks(String parse) throws UnsupportedEncodingException
    {
        String[] parts = parse.split("\\[\\[");
        List<WikipediaHyperlink> links = new ArrayList<WikipediaHyperlink>();

        for (int i = 0; i < parts.length; i ++ )
        {
            if (parts[i].contains("]]"))
            {
                String link = parts[i].split("]]")[0];
                String[] linkedParts = link.split("\\|");

                if (linkedParts.length == 1)
                {
                    linkedParts = new String[]
                    {
                            linkedParts[0], linkedParts[0]
                    };
                }

                WikipediaHyperlink hyperlink = new WikipediaHyperlink(linkedParts[1].trim(), Dictionary.WIKI_URL
                        + URLEncoder.encode(linkedParts[0], "UTF-8").replace(" ", "_").replace("+", "_"));

                if (!links.contains(hyperlink))
                {
                    links.add(hyperlink);
                }
            }
        }

        return links;
    }

    public static String slurp(final InputStream is)
    {
        return slurp(is, Charset.defaultCharset());
    }

    /**
     * Reads all of the stream into a String, decoding with the provided {@link java.nio.charset.Charset} then closes
     * the stream quietly.
     */
    public static String slurp(final InputStream is, final Charset charSet)
    {
        List<String> tokenOrEmpty = new ArrayList<String>();
        try
        {
            tokenOrEmpty = tokenSlurp(is, charSet, "\\A");
        }
        catch (IOException e)
        {
            Bot.errorLog.print(e);
        }
        return tokenOrEmpty.isEmpty() ? "" : getSoleElement(tokenOrEmpty);
    }

    /**
     * Tokenizes the provided input stream into memory using the given delimiter.
     * 
     * @throws IOException
     */
    private static List<String> tokenSlurp(final InputStream is, final Charset charSet, final String delimiterPattern)
            throws IOException
    {
        try (Scanner s = new Scanner(is, charSet.toString()))
        {
            s.useDelimiter(delimiterPattern);
            final LinkedList<String> tokens = new LinkedList<>();
            while (s.hasNext())
            {
                tokens.add(s.next());
            }
            return tokens;
        }
        finally
        {
            is.close();
        }
    }

    public static <T> T getSoleElement(final Collection<T> items)
    {
        if (items.size() != 1)
            throw new IllegalArgumentException(String.format("Expected a single element in %s, but found %s.", items,
                    items.size()));
        return items.iterator().next();
    }

    public static String capitalizeFirst(String text)
    {
        if (text.length() > 0)
        {
            String firstLetter = text.substring(0, 1).toUpperCase();
            text = firstLetter + (text.substring(1, text.length()));
        }

        return text;
    }

    public static String fixText(String text)
    {
        return capitalizeFirst(text) + ".";
    }
}
