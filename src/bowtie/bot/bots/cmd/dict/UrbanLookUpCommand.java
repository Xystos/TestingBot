package bowtie.bot.bots.cmd.dict;

import java.io.IOException;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.bot.hand.MessageManager;
import bowtie.core.Main;
import bowtie.dict.Dictionary;
import bowtie.dict.util.DictionaryUtils;
import bowtie.dict.wrap.urban.UrbanDictionaryMessage;
import bowtie.dict.wrap.urban.UrbanDictionaryWord;

import com.vdurmont.emoji.EmojiManager;

/**
 * @author &#8904
 *
 */
public class UrbanLookUpCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public UrbanLookUpCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public UrbanLookUpCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
            word += parts[i] + " ";
        }

        word = word.trim();

        if (word.isEmpty())
        {
            bot.sendMessage("You have to specify a word. Take a look at the command description by typing '"
                    + event.getGuildObject().getPrefix() + "help urban'.", event.getChannel(), Colors.RED);
            return;
        }

        UrbanDictionaryWord dictWord = null;
        try
        {
            dictWord = Dictionary.get().lookUpUrban(word);
        }
        catch (IOException e)
        {
            Bot.errorLog.print(this, e);
        }
        if (dictWord.getDefinitionCount() > 0)
        {
            MessageManager manager = MessageManager.getManagerForGuild(event.getGuild());
            IMessage message = bot.sendMessage(DictionaryUtils.embedUrbanWord(dictWord.get(0)), event.getChannel());
            RequestBuffer.request(() ->
            {
                message.addReaction(EmojiManager.getForAlias(":arrow_backward:"));
            }).get();
            RequestBuffer.request(() ->
            {
                message.addReaction(EmojiManager.getForAlias(":arrow_forward:"));
            }).get();
            manager.addUrban(new UrbanDictionaryMessage(dictWord, message));
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

        builder.withAuthorName("Urban Dictionary Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#dict");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will show up to 10 definitions for\n"
                + "|  the given word from the Urban Dictionary.\n"
                + "|  \n"
                + "|  You can press the arrow reactions to switch pages.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "urban hello\n"
                        + "|  " + guild.getPrefix() + "urban how are you\n"
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