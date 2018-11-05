package bowtie.feed.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Text extends HTMLEditorKit.ParserCallback
{
    StringBuffer s;

    public void parse(String text) throws IOException
    {
        StringReader in = new StringReader(text);
        parse(in);
        in.close();
    }

    public void parse(Reader in) throws IOException
    {
        s = new StringBuffer();
        ParserDelegator delegator = new ParserDelegator();
        // the third parameter is TRUE to ignore charset directive
        delegator.parse(in, this, Boolean.TRUE);
    }

    @Override
    public void handleText(char[] text, int pos)
    {
        s.append(text);
    }

    public String getText()
    {
        return s.toString();
    }
}