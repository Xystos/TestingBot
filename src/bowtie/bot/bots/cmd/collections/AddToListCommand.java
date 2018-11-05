package bowtie.bot.bots.cmd.collections;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.bot.collections.list.ItemList;
import bowtie.bot.collections.list.ListManager;
import bowtie.core.Main;

import com.vdurmont.emoji.EmojiManager;

/**
 * @author &#8904
 *
 */
public class AddToListCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public AddToListCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public AddToListCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        ListManager manager = ListManager.getManagerForGuild(event.getGuildObject());
        String[] parts = event.getMessage().getContent().trim().split(" ");
        String name = "";

        if (parts.length > 1)
        {
            name = parts[1];
        }

        String item = "";

        if (parts.length > 2)
        {
            for (int i = 2; i < parts.length; i ++ )
            {
                item += parts[i] + " ";
            }
            item = item.trim();
        }
        else
        {
            item = null;
        }

        if (item == null)
        {
            this.bot.sendMessage("You have to add the text that should be added to the list. Example: "
                    + event.getGuildObject().getPrefix() + "add list2 This will be added",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        ItemList list = manager.getListByName(name.toLowerCase());

        if (list == null)
        {
            this.bot.sendMessage("Make sure to open a  list with that name by using the 'list' command first.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (list.addMember(item))
        {
            RequestBuffer.request(() -> event.getMessage().addReaction(EmojiManager.getForAlias("white_check_mark")));
        }
        else
        {
            this.bot.sendMessage("That item is already in the list.", event.getMessage().getChannel(), Colors.RED);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Add Member List Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#collections");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will add the given item to the list with \n"
                + "|  the given name.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "addmember listname member\n"
                        + "|  " + guild.getPrefix() + "addmember animals monkey\n"
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