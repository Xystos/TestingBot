package bowtie.feed.util;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * @author &#8904
 *
 */
public class DummyErrorListener implements ErrorListener
{
    /**
     * @see javax.xml.transform.ErrorListener#warning(javax.xml.transform.TransformerException)
     */
    @Override
    public void warning(TransformerException exception) throws TransformerException
    {
    }

    /**
     * @see javax.xml.transform.ErrorListener#error(javax.xml.transform.TransformerException)
     */
    @Override
    public void error(TransformerException exception) throws TransformerException
    {
    }

    /**
     * @see javax.xml.transform.ErrorListener#fatalError(javax.xml.transform.TransformerException)
     */
    @Override
    public void fatalError(TransformerException exception) throws TransformerException
    {
    }

}
