package bowtie.bot.bots.cmd.users;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cmnd.CommandCooldown;
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
public class AddOwnerCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public AddOwnerCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public AddOwnerCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        new CommandCooldown(this, 2000, event.getGuildObject()).startTimer();
        List<IUser> mentions = event.getMessage().getMentions();
        if (mentions.isEmpty())
        {
            this.bot.sendMessage("You have to tag the user that should become an owner.",
                    event.getMessage().getChannel(), Colors.RED);
        }
        else
        {
            if (event.getGuildObject().addOwner(mentions.get(0)))
            {
                if (event.getGuildObject().isMaster(mentions.get(0)))
                {
                    // if a master is being promoted
                    event.getGuildObject().removeMaster(mentions.get(0));
                    this.main.getDatabase().removeMaster(mentions.get(0).getStringID(),
                            event.getGuildObject().getStringID());
                    this.main.getDatabase().addMaster(mentions.get(0).getStringID(),
                            event.getGuildObject().getStringID(), UserPermissions.OWNER);
                    Bot.log.print(this, "Promoted " + mentions.get(0).getName() + "#"
                            + mentions.get(0).getDiscriminator() + " to owner on "
                            + "'" + event.getGuild().getName() + "'.");
                }
                else
                {
                    this.main.getDatabase().addMaster(mentions.get(0).getStringID(),
                            event.getGuildObject().getStringID(), UserPermissions.OWNER);
                    Bot.log.print(this, "Registered " + mentions.get(0).getName() + "#"
                            + mentions.get(0).getDiscriminator() + " as owner on "
                            + "'" + event.getGuild().getName() + "'.");
                }
                event.getMessage().addReaction(EmojiManager.getForAlias("white_check_mark"));
            }
            else
            {
                this.bot.sendMessage("That user is already an owner on this server.",
                        event.getMessage().getChannel(), Colors.RED);
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

        builder.withAuthorName("Grant Owner Permissions Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#masters");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Gives the mentioned user/s owner permissions \n"
                + "|  for the bot.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "owner @user\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "owner @user @user\n"
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