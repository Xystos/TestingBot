package bowtie.bot.bots.cmd.users;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

import com.vdurmont.emoji.EmojiManager;

/**
 * @author &#8904
 *
 */
public class RemoveOwnerCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public RemoveOwnerCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public RemoveOwnerCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        List<IUser> mentions = event.getMessage().getMentions();
        if (mentions.isEmpty())
        {
            this.bot.sendMessage("You have to tag the user that should lose the owner permissions.", event.getMessage()
                    .getChannel(), Colors.RED);
        }
        else
        {
            if (!event.getGuildObject().isOwner(mentions.get(0)))
            {
                this.bot.sendMessage("That user is not a owner on this server.",
                        event.getMessage().getChannel(), Colors.RED);
            }
            else if (event.getGuildObject().getOwners().size() == 1)
            {
                this.bot.sendMessage("That user is my last owner on this server. I can't function without an owner.",
                        event.getMessage().getChannel(), Colors.RED);
            }
            else if (event.getGuildObject().removeOwner(mentions.get(0)))
            {
                this.main.getDatabase().removeMaster(mentions.get(0).getStringID(),
                        event.getGuildObject().getStringID());
                event.getMessage().addReaction(EmojiManager.getForAlias("white_check_mark"));
                Bot.log.print(this, "Removed owner permissions of " + mentions.get(0).getName() + "#"
                        + mentions.get(0).getDiscriminator()
                        + " on '" + event.getGuild().getName() + "'.");
            }
        }
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Revoke Owner Permissions Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#masters");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Revokes the mentioned users owner permissions \n"
                + "|  for the bot.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "noowner @user\n"
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