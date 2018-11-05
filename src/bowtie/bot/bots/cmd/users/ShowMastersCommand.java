package bowtie.bot.bots.cmd.users;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cmnd.CommandCooldown;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class ShowMastersCommand extends Command
{
    private Bot bot;

    /**
     * @param validExpressions
     * @param permission
     */
    public ShowMastersCommand(String[] validExpressions, int permission, Bot bot)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public ShowMastersCommand(List<String> validExpressions, int permission, Bot bot)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        new CommandCooldown(this, 5000, event.getGuildObject()).startTimer();
        List<String> ownerNames = new ArrayList<String>();
        List<String> ids = event.getGuildObject().getOwners();
        List<IUser> owners = new ArrayList<>();

        for (String id : ids)
        {
            IUser user = RequestBuffer.request(new IRequest<IUser>()
            {
                @Override
                public IUser request()
                {
                    return bot.getClient().fetchUser(Long.parseLong(id));
                }
            }).get();

            if (user != null)
            {
                owners.add(user);
            }
        }

        for (IUser user : owners)
        {
            ownerNames.add(user.getName() + "#" + user.getDiscriminator());
        }
        this.bot.sendListMessage("These people have OWNER permissions on this server:",
                ownerNames, event.getMessage().getChannel(), Colors.PURPLE, 15, true);

        List<String> masterNames = new ArrayList<String>();
        ids = event.getGuildObject().getMasters();
        List<IUser> masters = new ArrayList<>();

        for (String id : ids)
        {
            IUser user = RequestBuffer.request(new IRequest<IUser>()
            {
                @Override
                public IUser request()
                {
                    return bot.getClient().fetchUser(Long.parseLong(id));
                }
            }).get();

            if (user != null)
            {
                masters.add(user);
            }
        }

        for (IUser user : masters)
        {
            masterNames.add(user.getName() + "#" + user.getDiscriminator());
        }
        if (!masterNames.isEmpty())
        {
            this.bot.sendListMessage("These people have MASTER permissions on this server:",
                    masterNames, event.getMessage().getChannel(), Colors.PURPLE, 15, true);
        }
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Show Masters and Owners Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#masters");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Shows all masters and owners for this server.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "showmasters\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "showowners\n"
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