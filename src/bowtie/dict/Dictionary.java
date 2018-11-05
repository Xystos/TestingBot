package bowtie.dict;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rita.RiWordNet;
import bowt.bot.Bot;
import bowtie.dict.util.DictionaryUtils;
import bowtie.dict.wrap.datamuse.DatamuseCollection;
import bowtie.dict.wrap.urban.UrbanDictionaryDefinition;
import bowtie.dict.wrap.urban.UrbanDictionaryWord;
import bowtie.dict.wrap.wiki.WikipediaHyperlink;
import bowtie.dict.wrap.wiki.WikipediaWord;
import bowtie.dict.wrap.wordnet.WordnetWord;

/**
 * @author &#8904
 *
 */
public class Dictionary
{
    private static final String URBAN_API_URL = "http://api.urbandictionary.com/v0/define?term=";
    private static final String WIKI_API_URL = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts|pageimages&piprop=original&exintro=&explaintext=&redirects=1&pageids=";
    public static final String WIKI_URL = "http://en.wikipedia.org/wiki/";
    private static final String WIKI_SEARCH_API_URL = "https://en.wikipedia.org/w/api.php?format=json&action=query&list=search&srwhat=text&srlimit=50&prop=extracts|pageimages&piprop=original&exintro=&explaintext=&redirects=1&srsearch=";
    private static final String WIKI_LINK_URL = "https://en.wikipedia.org/w/api.php?action=parse&prop=wikitext&section=0&page=";
    private static final String OWL_API_URL = "https://owlbot.info/api/v2/dictionary/";
    private static final String DATAMUSE_API_URL = "https://api.datamuse.com/";
    private static final String DATAMUSE_WORDS = "words?v=enwiki";
    private static final String DATAMUSE_SUG = "sug?v=enwiki";
    private static final String DATAMUSE_RHYME = "&rel_rhy=";
    private static final String DATAMUSE_SYN = "&rel_syn=";
    private static final String DATAMUSE_ANT = "&rel_ant=";
    private static final String DATAMUSE_MEANS_LIKE = "&ml=";
    private static final String DATAMUSE_TOPICS = "&ml=";
    private static final String DATAMUSE_DESCRIBES = "&rel_jjb=";
    private static final String DATAMUSE_DESCRIBE_WITH = "&rel_jja=";
    private static final String DATAMUSE_MAX = "&max=";
    private static final String DATAMUSE_TRIGGER = "&rel_trg=";
    private static final String DATAMUSE_SPELLED_LIKE = "&sp=";
    private static final String WORDNET_PATH = "/usr/local/WordNet-3.0";// "X:\\Workspace\\Jars\\wordnet\\WordNet-3.0";//
                                                                        // "/usr/local/WordNet-3.0";
    private static final int MAX_SEARCH_RESULTS = 10;

    private static Dictionary instance;

    private Dictionary()
    {

    }

    public DatamuseCollection describeWith(String word, String topic) throws IOException
    {
        URL url = null;
        try
        {
            if (topic != null)
            {
                url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_DESCRIBE_WITH
                        + URLEncoder.encode(word, "UTF-8")
                        + DATAMUSE_MAX + 20 + DATAMUSE_TOPICS + topic);
            }
            else
            {
                url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_DESCRIBE_WITH
                        + URLEncoder.encode(word, "UTF-8")
                        + DATAMUSE_MAX + 20);
            }
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream())
        {
            String json = DictionaryUtils.slurp(in);
            JSONArray array = new JSONArray(json);

            List<String> results = new ArrayList<String>();

            for (int i = 0; i < array.length(); ++ i)
            {
                JSONObject object = array.getJSONObject(i);
                results.add(DictionaryUtils.capitalizeFirst(object.getString("word")));
            }

            String name = "Words that can be described by '" + DictionaryUtils.capitalizeFirst(word) + "'";

            if (topic != null)
            {
                name = "Words that can be described by '" + DictionaryUtils.capitalizeFirst(word)
                        + "' and fall into the category '" + DictionaryUtils.capitalizeFirst(topic) + "'";
            }

            return new DatamuseCollection(name, results);
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public DatamuseCollection describes(String word, String syn) throws IOException
    {
        URL url = null;
        try
        {
            if (syn != null)
            {
                url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_DESCRIBES + URLEncoder.encode(word, "UTF-8")
                        + DATAMUSE_MAX + 20 + DATAMUSE_MEANS_LIKE + syn);
            }
            else
            {
                url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_DESCRIBES + URLEncoder.encode(word, "UTF-8")
                        + DATAMUSE_MAX + 20);
            }
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream())
        {
            String json = DictionaryUtils.slurp(in);
            JSONArray array = new JSONArray(json);

            List<String> results = new ArrayList<String>();

            for (int i = 0; i < array.length(); ++ i)
            {
                JSONObject object = array.getJSONObject(i);
                results.add(DictionaryUtils.capitalizeFirst(object.getString("word")));
            }

            String name = "Words that are often used with '" + DictionaryUtils.capitalizeFirst(word) + "'";

            if (syn != null)
            {
                name = "Words that are often used with '" + DictionaryUtils.capitalizeFirst(word)
                        + "' and are related to '" + DictionaryUtils.capitalizeFirst(syn) + "'";
            }

            return new DatamuseCollection(name, results);
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public DatamuseCollection findAntonyms(String word) throws IOException
    {
        URL url = null;
        try
        {
            url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_ANT + URLEncoder.encode(word, "UTF-8")
                    + DATAMUSE_MAX + 20);
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream())
        {
            String json = DictionaryUtils.slurp(in);
            JSONArray array = new JSONArray(json);

            List<String> results = new ArrayList<String>();

            for (int i = 0; i < array.length(); ++ i)
            {
                JSONObject object = array.getJSONObject(i);
                results.add(DictionaryUtils.capitalizeFirst(object.getString("word")));
            }

            return new DatamuseCollection("Antonyms for '" + DictionaryUtils.capitalizeFirst(word) + "'", results);
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public DatamuseCollection findSynonyms(String word) throws IOException
    {
        URL url = null;
        try
        {
            url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_MEANS_LIKE + URLEncoder.encode(word, "UTF-8")
                    + DATAMUSE_MAX + 20);
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream())
        {
            String json = DictionaryUtils.slurp(in);
            JSONArray array = new JSONArray(json);

            List<String> results = new ArrayList<String>();

            for (int i = 0; i < array.length(); ++ i)
            {
                JSONObject object = array.getJSONObject(i);
                results.add(DictionaryUtils.capitalizeFirst(object.getString("word")));
            }

            return new DatamuseCollection("Synonyms for '" + DictionaryUtils.capitalizeFirst(word) + "'", results);
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public DatamuseCollection findRhymes(String word, String start) throws IOException
    {
        URL url = null;
        try
        {
            if (start == null)
            {
                url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_RHYME + URLEncoder.encode(word, "UTF-8")
                        + DATAMUSE_MAX + 20);
            }
            else
            {
                url = new URL(DATAMUSE_API_URL + DATAMUSE_WORDS + DATAMUSE_RHYME + URLEncoder.encode(word, "UTF-8")
                        + DATAMUSE_MAX + 20 + DATAMUSE_SPELLED_LIKE + start + "*");
            }
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream())
        {
            String json = DictionaryUtils.slurp(in);
            JSONArray array = new JSONArray(json);
            List<String> results = new ArrayList<String>();

            for (int i = 0; i < array.length(); ++ i)
            {
                JSONObject object = array.getJSONObject(i);
                results.add(DictionaryUtils.capitalizeFirst(object.getString("word")));
            }

            String name = "Rhymes for '" + DictionaryUtils.capitalizeFirst(word) + "'";

            if (start != null)
            {
                name = "Rhymes for '" + DictionaryUtils.capitalizeFirst(word) + "' that start with '" + start + "'";
            }

            return new DatamuseCollection(name, results);
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public synchronized List<WordnetWord> lookUpWordnet(String word) throws UnsupportedEncodingException
    {
        RiWordNet wordnet = new RiWordNet(WORDNET_PATH);
        wordnet.randomizeResults(false);

        List<WordnetWord> words = new ArrayList<>();

        if (wordnet.exists(word))
        {
            if (wordnet.isNoun(word))
            {
                words.add(createWordnetWord(word, RiWordNet.NOUN, wordnet));
            }
            if (wordnet.isVerb(word))
            {
                words.add(createWordnetWord(word, RiWordNet.VERB, wordnet));
            }
            if (wordnet.isAdverb(word))
            {
                words.add(createWordnetWord(word, RiWordNet.ADV, wordnet));
            }
            if (wordnet.isAdjective(word))
            {
                words.add(createWordnetWord(word, RiWordNet.ADJ, wordnet));
            }
        }

        for (int i = 0; i < words.size(); i ++ )
        {
            words.get(i).setIndex(i + 1);
            words.get(i).setListsize(words.size());
        }

        return words;
    }

    private WordnetWord createWordnetWord(String word, String pos, RiWordNet wordnet)
    {
        String link = "";
        try
        {
            link = "http://wordnetweb.princeton.edu/perl/webwn?&sub=Search+WordNet&o2=&o0=1&o8=1&o1=1&o7=&o5=&o9=&o6=&o3=&o4=&h=&s="
                    + URLEncoder.encode(word, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            Bot.errorLog.print(this, e);
        }
        String[] results = wordnet.getAllGlosses(word, pos);
        List<String> definitions = new ArrayList<>();

        for (String s : results)
        {
            definitions.add(DictionaryUtils.fixText(s));

            if (definitions.size() == MAX_SEARCH_RESULTS)
            {
                break;
            }
        }

        return new WordnetWord(DictionaryUtils.capitalizeFirst(word), definitions, pos, link);
    }

    public List<WikipediaWord> lookUpWikipedia(String word) throws IOException
    {
        URL url = null;
        try
        {
            url = new URL(WIKI_SEARCH_API_URL + URLEncoder.encode(word, "UTF-8"));
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream())
        {
            String json = DictionaryUtils.slurp(in);
            JSONObject query = new JSONObject(json).getJSONObject("query");

            JSONArray results = query.getJSONArray("search");

            if (results.length() == 0)
            {
                return null;
            }

            List<Integer> ids = new ArrayList<Integer>();

            String[] searchParts = word.toLowerCase().split(" ");
            // title search is disabled so this is my cheap version of it
            for (int i = 0; i < results.length(); i ++ )
            {
                if (ids.size() < MAX_SEARCH_RESULTS - 4)
                {
                    JSONObject ob = results.getJSONObject(i);

                    String title = ob.getString("title");

                    // adding pages where the words that were searched for match at least 33% of the title
                    int count = 0;

                    for (int j = 0; j < searchParts.length; j ++ )
                    {
                        if (title.toLowerCase().matches("(.*)" + searchParts[j] + "(.*)"))
                        {
                            count += searchParts[j].length();
                        }
                    }

                    if (count >= title.replace(" ", "").length() / 3.0)
                    {
                        ids.add(ob.getInt("pageid"));
                    }
                }
            }

            if (ids.size() < (MAX_SEARCH_RESULTS - 4) / 2.0)
            {
                for (int i = 0; i < results.length(); i ++ )
                {
                    if (ids.size() < (MAX_SEARCH_RESULTS - 4) / 2.0)
                    {
                        JSONObject ob = results.getJSONObject(i);
                        int id = ob.getInt("pageid");
                        if (!ids.contains(id))
                        {
                            ids.add(id);
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }

            String idString = ids.get(0) + "";

            for (int i = 1; i < ids.size(); i ++ )
            {
                idString += "|" + ids.get(i);
            }

            try
            {
                url = new URL(WIKI_API_URL + idString);
            }
            catch (MalformedURLException e)
            {
                Bot.errorLog.print(this, e);
            }
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in2 = connection.getInputStream())
            {
                json = DictionaryUtils.slurp(in2);

                List<WikipediaWord> words = new ArrayList<WikipediaWord>();
                JSONObject pages = new JSONObject(json).getJSONObject("query").getJSONObject("pages");

                JSONObject page = null;
                String text = null;
                String title = null;
                String link = null;
                String image = null;

                for (int i : ids)
                {
                    page = pages.getJSONObject(Integer.toString(i));
                    text = page.getString("extract");
                    if (text.split("\n")[0].toLowerCase().contains("may refer to:"))
                    {
                        continue;
                    }

                    title = page.getString("title");

                    text = addHyperlinks(title, text);
                    link = WIKI_URL + URLEncoder.encode(title, "UTF-8").replace("+", "_");
                    try
                    {
                        image = page.getJSONObject("original").getString("source");
                    }
                    catch (Exception e)
                    {

                    }
                    words.add(new WikipediaWord(title, text, link, image));
                    page = null;
                    text = null;
                    title = null;
                    link = null;
                    image = null;
                }

                for (int i = 0; i < words.size(); i ++ )
                {
                    words.get(i).setIndex(i + 1);
                    words.get(i).setListsize(words.size());
                }
                return words;
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    private String addHyperlinks(String title, String text) throws IOException
    {
        URL url = null;
        try
        {
            url = new URL(WIKI_LINK_URL + URLEncoder.encode(title, "UTF-8").replace("+", "_"));
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream())
        {
            String output = DictionaryUtils.slurp(in);

            List<WikipediaHyperlink> links = DictionaryUtils.getWikipediaHyperlinks(output);
            text = DictionaryUtils.insertWikipediaHyperlinks(text, links);
            return text;
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public UrbanDictionaryWord lookUpUrban(String word) throws IOException
    {
        URL url = null;
        try
        {
            url = new URL(URBAN_API_URL + URLEncoder.encode(word, "UTF-8"));
        }
        catch (MalformedURLException e)
        {
            Bot.errorLog.print(this, e);
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try (InputStream in = connection.getInputStream())
        {
            String json = DictionaryUtils.slurp(in);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray("list");

            List<UrbanDictionaryDefinition> definitions = new ArrayList<UrbanDictionaryDefinition>();
            String example = null;
            String definition = null;
            String author = null;
            String link = null;
            String dictWord = null;
            int thumbsUp = 0;
            int thumbsDown = 0;

            for (int i = 0; i < array.length(); ++ i)
            {
                JSONObject object = array.getJSONObject(i);
                dictWord = object.getString("word");
                author = object.getString("author");
                try
                {
                    definition = object.getString("definition");
                }
                catch (JSONException e)
                {
                    definition = "";
                }
                try
                {
                    example = object.getString("example");
                }
                catch (JSONException e)
                {
                    example = "";
                }
                try
                {
                    link = object.getString("permalink");
                }
                catch (JSONException e)
                {
                    link = "";
                }
                thumbsUp = object.getInt("thumbs_up");
                thumbsDown = object.getInt("thumbs_down");

                definitions.add(new UrbanDictionaryDefinition(dictWord, definition, example, author, link, thumbsUp,
                        thumbsDown));
            }
            return new UrbanDictionaryWord(definitions);
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public static Dictionary get()
    {
        if (Dictionary.instance == null)
        {
            Dictionary.instance = new Dictionary();
        }

        return Dictionary.instance;
    }
}