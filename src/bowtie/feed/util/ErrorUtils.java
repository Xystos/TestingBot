package bowtie.feed.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.TimeZone;

import sx.blah.discord.util.RequestBuffer;
import bowt.log.Logger;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public final class ErrorUtils
{
    private static Main main;

    public synchronized static void sendErrorDump(Throwable e, String... text)
    {
        try
        {
            new PrintWriter("logs/errorDump.log").close();
        }
        catch (FileNotFoundException e2)
        {
        }
        Logger logger = new Logger("logs/errorDump.log", TimeZone.getTimeZone("CET"));
        logger.setLogToSystemOut(false);
        logger.print(e);
        for (String s : text)
        {
            logger.print(s + "\n\n\n\n");
        }
        RequestBuffer.request(
                () ->
                {
                    try
                    {
                        main.getBot().getClient().getApplicationOwner().getOrCreatePMChannel()
                                .sendFile(logger.getLoggerFile());
                    }
                    catch (Exception e1)
                    {
                    }
                }).get();
    }

    public synchronized static void sendErrorDump(String... text)
    {
        try
        {
            new PrintWriter("logs/errorDump.log").close();
        }
        catch (FileNotFoundException e2)
        {
        }
        Logger logger = new Logger("logs/errorDump.log", TimeZone.getTimeZone("CET"));
        logger.setLogToSystemOut(false);
        for (String s : text)
        {
            logger.print(s + "\n\n\n\n");
        }
        RequestBuffer.request(
                () ->
                {
                    try
                    {
                        main.getBot().getClient().getApplicationOwner().getOrCreatePMChannel()
                                .sendFile(logger.getLoggerFile());
                    }
                    catch (Exception e1)
                    {
                    }
                }).get();
    }

    public static void setMain(Main main)
    {
        ErrorUtils.main = main;
    }
}