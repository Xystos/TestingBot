package bowtie.bot.bots.cmd.collections;

import java.time.LocalDateTime;
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
import bowtie.bot.collections.group.Group;
import bowtie.bot.collections.group.GroupManager;
import bowtie.core.Main;

import com.vdurmont.emoji.EmojiManager;

/**
 * @author &#8904
 *
 */
public class CreateGroupCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public CreateGroupCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public CreateGroupCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        GroupManager manager = GroupManager.getManagerForGuild(event.getGuildObject());
        String[] parts = event.getMessage().getContent().trim().toLowerCase().split(" ");
        String name = "";
        long destroyTime = -1;
        if (parts.length == 2)
        {
            name = parts[1];
        }
        else if (parts.length < 2)
        {
            this.bot.sendMessage("You have to add the name of the group. Example: '"
                    + event.getGuildObject().getPrefix()
                    + "create groupname'.", event.getMessage().getChannel(), Colors.RED);
            return;
        }
        else if (parts.length > 2)
        {
            name = parts[1];

            int weeks = 0;
            int days = 0;
            int hours = 0;
            int minutes = 0;
            int seconds = 0;

            for (int i = 2; i < parts.length; i ++ )
            {
                if (parts[i].toLowerCase().contains("w"))
                {
                    weeks = getNumber(parts[i], "w");
                    if (weeks < 0)
                    {
                        this.bot.sendMessage(
                                "'"
                                        + parts[i]
                                        + "' seems to be in a wrong format. Only positive numbers formatted like '2w' or '34w' are allowed.",
                                event.getMessage().getChannel(), Colors.RED);
                        return;
                    }
                }
                else if (parts[i].toLowerCase().contains("d"))
                {
                    days = getNumber(parts[i], "d");
                    if (days < 0)
                    {
                        this.bot.sendMessage(
                                "'"
                                        + parts[i]
                                        + "' seems to be in a wrong format. Only positive numbers formatted like '1d' or '15d' are allowed.",
                                event.getMessage().getChannel(), Colors.RED);
                        return;
                    }
                }
                else if (parts[i].toLowerCase().contains("h"))
                {
                    hours = getNumber(parts[i], "h");
                    if (hours < 0)
                    {
                        this.bot.sendMessage(
                                "'"
                                        + parts[i]
                                        + "' seems to be in a wrong format. Only positive numbers formatted like '3h' or '10h' are allowed.",
                                event.getMessage().getChannel(), Colors.RED);
                        return;
                    }
                }
                else if (parts[i].toLowerCase().contains("m"))
                {
                    minutes = getNumber(parts[i], "m");
                    if (minutes < 0)
                    {
                        this.bot.sendMessage(
                                "'"
                                        + parts[i]
                                        + "' seems to be in a wrong format. Only positive numbers formatted like '5m' or '33m' are allowed.",
                                event.getMessage().getChannel(), Colors.RED);
                        return;
                    }
                }
                else if (parts[i].toLowerCase().contains("s"))
                {
                    seconds = getNumber(parts[i], "s");
                    if (seconds < 0)
                    {
                        this.bot.sendMessage(
                                "'"
                                        + parts[i]
                                        + "' seems to be in a wrong format. Only positive numbers formatted like '10s' or '40s' are allowed.",
                                event.getMessage().getChannel(), Colors.RED);
                        return;
                    }
                }
            }
            if (weeks > 52)
            {
                this.bot.sendMessage("The amount of weeks may not be higher than 52.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            if (days > 365)
            {
                this.bot.sendMessage("The amount of days may not be higher than 365.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            if (hours > 168)
            {
                this.bot.sendMessage("The amount of hours may not be higher than 168.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            if (minutes > 1440)
            {
                this.bot.sendMessage("The amount of minutes may not be higher than 1440.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            if (seconds > 300)
            {
                this.bot.sendMessage("The amount of seconds may not be higher than 300.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }

            destroyTime += seconds * 1000L;
            destroyTime += minutes * 60000L;
            destroyTime += hours * 3600000L;
            destroyTime += days * 86400000L;
            destroyTime += weeks * 4233600000L;
        }

        LocalDateTime date = LocalDateTime.now();
        String dateString = date.getYear() + "." + date.getMonth().getValue() + "." + date.getDayOfMonth();
        Group group = new Group(name, manager, dateString);

        if (main.getDatabase().existingList(event.getGuildObject().getStringID(), name))
        {
            this.bot.sendMessage(
                    "There is already a list with that name. A group can not have the same name as a list.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (manager.add(group))
        {
            if (destroyTime != -1)
            {
                group.destroyAt(System.currentTimeMillis() + destroyTime);
                main.getDatabase().addGroupTimer(event.getGuildObject().getStringID(), name,
                        System.currentTimeMillis() + destroyTime);
                RequestBuffer.request(() -> event.getMessage().addReaction(EmojiManager.getForAlias("alarm_clock")))
                        .get();
            }
            RequestBuffer.request(() -> event.getMessage().addReaction(EmojiManager.getForAlias("white_check_mark")))
                    .get();
        }
        else
        {
            bot.sendMessage("That group does already exist.", event.getChannel(), Colors.RED);
        }
    }

    private int getNumber(String text, String replace)
    {
        String numberText = text.toLowerCase().replace(replace.toLowerCase(), "");
        int number = -1;
        try
        {
            number = Integer.parseInt(numberText);
        }
        catch (NumberFormatException e)
        {
        }
        return number;
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Create Group Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#collections");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will create a new group with \n"
                + "|  the given name. Keep in mind that there can't be another \n"
                + "|  list or group with the same name. \n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "group groupname\n"
                        + "|  " + guild.getPrefix() + "group giveaway\n"
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