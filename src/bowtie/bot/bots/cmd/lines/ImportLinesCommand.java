package bowtie.bot.bots.cmd.lines;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;
import bowtie.core.db.DatabaseAccess;
import bowtie.json.JSON;

/**
 * @author &#8904
 *
 */
public class ImportLinesCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public ImportLinesCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public ImportLinesCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        List<Attachment> attachments = event.getAttachments();

        if (attachments.isEmpty())
        {
            this.bot.sendMessage("You have to add the file to import as an attachment.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        String jsonString = downloadFile(attachments.get(0).getUrl());

        JSONObject json = JSON.parse(jsonString);

        if (json == null)
        {
            this.bot.sendMessage("The content of the file is not formatted correctly.\n\n"
                    + "Please do not change anything inside the files provided by the bot. "
                    + "If you did not change anything contact Lukas&#8904 and report this problem.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        JSONArray lines = json.getJSONArray("Lines");

        if (lines == null)
        {
            this.bot.sendMessage("The content of the file is not formatted correctly.\n\n"
                    + "Please do not change anything inside the files provided by the bot. "
                    + "If you did not change anything contact Lukas&#8904 and report this problem.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        for (int i = 0; i < lines.length(); i ++ )
        {
            String line = lines.getString(i);

            if (line.length() >= 200)
            {
                this.bot.sendMessage("The line is too long. Custom answers may not be longer than 200 characters.",
                        event.getChannel(), new Color(255, 0, 0));
                return;
            }
            else if (line.length() == 0)
            {
                this.bot.sendMessage("The line is empty.", event.getChannel(), Colors.RED);
                return;
            }
            int result = this.main.getDatabase().addLine(line, event.getGuildObject());
            if (result == DatabaseAccess.SUCCESS)
            {
                this.bot.sendMessage("The line '" + line + "' was added to the list of available answers.",
                        event.getChannel(), new Color(0, 255, 0));
                Bot.log.print(this, "The line '" + line + "' was added to the list of available answers of "
                        + "'" + event.getGuild().getName() + "' (" + event.getGuildObject().getStringID()
                        + ")");
            }
            else
            {
                this.bot.sendMessage("The line '" + line + "' is already in the list of available answers.",
                        event.getChannel(), new Color(255, 0, 0));
            }
        }
    }

    public String downloadFile(String url)
    {
        URL website;
        String text = null;
        try
        {
            website = new URL(url);
            URLConnection conn = website.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name()))
            {
                text = scanner.useDelimiter("\\A").next();
            }
        }
        catch (MalformedURLException e)
        {
        }
        catch (IOException e)
        {
        }
        return text;
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Import Custom 8Ball Lines Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#8Ball");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  For this command you should first use 'exportlines' \n"
                + "|  to get a file containing all custom lines.\n"
                + "|  \n"
                + "|  Upload that file and put this command into the comment pop-up.\n"
                + "|  \n"
                + "|  The bot will now import all lines from that file and tries to \n"
                + "|  restore them. \n"
                + "|");

        builder.appendField(
                "|  Usage: ",
                "|  (always upload the file and put the command below into the \n"
                        + "|  comment pop-up)\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "importlines\n"
                        + "|", false);

        builder.appendField(
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
                "[This command needs " + UserPermissions.getPermissionString(this.getPermissionOverride(guild))
                        + " permissions](http://www.bowtiebots.xyz/master-system.html)", false);
        builder.withFooterText("Click the title to read about this command on the website.");
        return builder.build();
    }
}
