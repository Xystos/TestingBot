package bowtie.bot.bots.cmd.dict;

import java.io.IOException;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;
import bowtie.dict.Dictionary;
import bowtie.dict.util.DictionaryUtils;
import bowtie.dict.wrap.datamuse.DatamuseCollection;

/**
 * @author &#8904
 *
 */
public class DescribeWithLookUpCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public DescribeWithLookUpCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public DescribeWithLookUpCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @see bowt.cmnd.Command#execute(bowt.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        String[] parts = event.getMessage().getContent().split(" ");

        String word = "";

        for (int i = 1; i < parts.length; i ++ )
        {
            if (!parts[i].startsWith(CommandEvent.parameterIndicator))
            {
                word += parts[i] + " ";
            }
        }

        word = word.trim();

        if (word.isEmpty())
        {
            bot.sendMessage("You have to specify a word. Take a look at the command description by typing '"
                    + event.getGuildObject().getPrefix() + "help descwith'.", event.getChannel(), Colors.RED);
            return;
        }

        String topic = event.getParameter("rel");

        DatamuseCollection col = null;
        try
        {
            col = Dictionary.get().describeWith(word, topic);
        }
        catch (IOException e)
        {
            Bot.errorLog.print(this, e);
        }
        if (col != null && col.getResults().size() > 0)
        {
            bot.sendMessage(DictionaryUtils.embedDatamuseCollection(col), event.getChannel());
        }
        else
        {
            bot.sendMessage("I could not find anything.", event.getChannel(), Colors.RED);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Describe With Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#dict");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will show up to 20 words that can be\n"
                + "|  described by the given word.\n"
                + "|  \n"
                + "|  You can add a category to limit the results to a certain related \n"
                + "|  topic. To do so add '-rel=yourCategory' after the input word. \n"
                + "|  You can not put any spaces between the words for the topic parameter.\n"
                + "|  \n"
                + "|  This command uses the Datamuse API which can be found \n"
                + "|  at https://www.datamuse.com.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "describewith healthy\n"
                        + "|  " + guild.getPrefix() + "describewith healthy -rel=food\n"
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