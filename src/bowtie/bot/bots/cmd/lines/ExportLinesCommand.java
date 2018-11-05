package bowtie.bot.bots.cmd.lines;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class ExportLinesCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public ExportLinesCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public ExportLinesCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        List<String> lines = main.getDatabase().getLines(event.getGuildObject());

        lines = lines.stream()
                .filter(l -> !l.equals("Yes.") && !l.equals("No.") && !l.equals("Maybe."))
                .collect(Collectors.toList());

        if (lines.isEmpty())
        {
            this.bot.sendMessage("Could not find any cusstom lines.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        JSONArray lineArray = new JSONArray();

        for (String line : lines)
        {
            lineArray.put(line);
        }

        JSONObject linesJson = new JSONObject();
        linesJson.put("Lines", lineArray);

        InputStream stream = new ByteArrayInputStream(linesJson.toString(4).getBytes(StandardCharsets.UTF_8));

        RequestBuffer.request(
                () ->
                {
                    event.getChannel().sendFile(
                            "This is your file containing all custom lines of this server.",
                            stream,
                            event.getGuildObject().getStringID() + ".txt");
                }).get();
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Export 8Ball Lines Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#8Ball");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will create a file containing all \n"
                + "|  custom lines of the 8Ball command.\n"
                + "|  \n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "exportlines\n"
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
