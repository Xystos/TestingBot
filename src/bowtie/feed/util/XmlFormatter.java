package bowtie.feed.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author &#8904
 *
 */
public final class XmlFormatter
{
    public static String format(String unformattedXml) throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setErrorListener(new DummyErrorListener()); // doesnt work, bug in SDK
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult result = new StreamResult(new StringWriter());
        Source source = new StreamSource(new StringReader(unformattedXml));
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
        if (!xmlString.startsWith("<?xml"))
        {
            xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + xmlString;
        }
        return xmlString;
    }
}