package bowtie.bot.bots.cmd.auto;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.bot.auto.AutomatedCommand;
import bowtie.bot.auto.DummyMessage;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class AutomateCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public AutomateCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public AutomateCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        String[] parts = event.getMessage().getContent().split("<>");
        String content = "";

        if (parts.length < 2)
        {
            this.bot.sendMessage("You have to add the command including all parameters. Example: '"
                    + event.getGuildObject().getPrefix() + "automate 1h 30m <> bt-group group1'.", event.getMessage()
                    .getChannel(),
                    Colors.RED);
            return;
        }

        String[] times = parts[0].trim().split(" ");

        int days = 0;
        int hours = 0;
        int minutes = 0;

        for (int i = 1; i < times.length; i ++ )
        {
            if (times[i].toLowerCase().contains("d"))
            {
                days = getNumber(times[i], "d");
                if (days < 1)
                {
                    this.bot.sendMessage(
                            "'"
                                    + times[i]
                                    + "' seems to be in a wrong format. Only positive numbers formatted like '1d' or '15d' are allowed.",
                            event.getMessage().getChannel(), Colors.RED);
                    return;
                }
            }
            else if (times[i].toLowerCase().contains("h"))
            {
                hours = getNumber(times[i], "h");
                if (hours < 1)
                {
                    this.bot.sendMessage(
                            "'"
                                    + times[i]
                                    + "' seems to be in a wrong format. Only positive numbers formatted like '3h' or '10h' are allowed.",
                            event.getMessage().getChannel(), Colors.RED);
                    return;
                }
            }
            else if (times[i].toLowerCase().contains("m"))
            {
                minutes = getNumber(times[i], "m");
                if (minutes < 1)
                {
                    this.bot.sendMessage(
                            "'"
                                    + times[i]
                                    + "' seems to be in a wrong format. Only positive numbers formatted like '5m' or '33m' are allowed.",
                            event.getMessage().getChannel(), Colors.RED);
                    return;
                }
            }
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
        long interval = 0;
        interval += minutes * 60000L;
        interval += hours * 3600000L;
        interval += days * 86400000L;

        if (interval == 0)
        {
            this.bot.sendMessage("You have to add at least 1 minute as an interval between commands.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        String commandName = parts[1].replace(event.getGuildObject().getPrefix(), "").trim().split(" ")[0];
        Command cmd = Main.commandHandler.getCommand(commandName);

        if (cmd == null)
        {
            this.bot.sendMessage("'" + commandName + "' is not a valid command.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (cmd instanceof AutomateCommand)
        {
            this.bot.sendMessage("This command can not be automated.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (!cmd.isValidPermission(UserPermissions.getPermissionLevel(event.getAuthor(), event.getGuildObject()),
                event.getGuildObject()))
        {
            this.bot.sendMessage("You don't have a high enough permission level to execute the '" + commandName
                    + "' command.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        List<Long> mentions = new ArrayList<>();

        for (IUser user : event.getMentions())
        {
            mentions.add(user.getLongID());
        }

        List<Long> roleMentions = new ArrayList<>();
        for (IRole role : event.getMessage().getRoleMentions())
        {
            roleMentions.add(role.getLongID());
        }

        content += parts[1].trim();

        if (parts.length > 2)
        {
            if (parts[2].length() > 100)
            {
                this.bot.sendMessage("The additional text may not be longer than 100 characters.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }

            content += " <> " + parts[2];
        }

        DummyMessage message = new DummyMessage(bot.getClient(), event.getMessage().getLongID(), content,
                event.getAuthor(), event.getChannel(), event.getMessage().mentionsEveryone(), mentions, roleMentions);

        AutomatedCommand command = new AutomatedCommand(AutomatedCommand.currentID ++ , message,
                event.getGuildObject(), main, interval);
        AutomatedCommand.add(command);
        main.getDatabase().addAutomatedCommand(command);
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
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Automate Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#automate");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription(
                "|  This command allows you to make the bot automatically use a certain \n"
                        + "|  command a set interval. You can for example pick a new giveaway \n"
                        + "|  winner every day, form new teams every week or let the 8Ball \n"
                        + "|  spit out a random line every hour. \n"
                        + "|  \n"
                        + "|  \n"
                        + "|  The command is divided into 2-3 parts all separated by <> :\n"
                        + "|  \n"
                        + "|  The first part is the automation command with the desired delay. \n"
                        + "|  Valid time units are minutes(m), hours(h) and days(d). A correctly \n"
                        + "|  formatted interval would look \n"
                        + "|  something like this:\n"
                        + "|  \n"
                        + "|  45m\n"
                        + "|  or\n"
                        + "|  15d 2h 5m\n"
                        + "|  \n"
                        + "|  \n"
                        + "|  The second part is the command that should be automatically used. \n"
                        + "|  Just add it as you would usually use it. Make sure to separate each \n"
                        + "|  part with a <>.\n"
                        + "|  \n"
                        + "|  "
                        + guild.getPrefix()
                        + "auto 45m <> "
                        + guild.getPrefix()
                        + "all \n"
                        + "|  \n"
                        + "|  The above command will pick a random user every 45 minutes, starting \n"
                        + "|  from the time the automation was added.\n"
                        + "|  \n"
                        + "|  \n"
                        + "|  The third (optional) part allows you to make the bot send a message \n"
                        + "|  right before it executes the automated command. Again make sure to \n"
                        + "|  separate the parts with <>.\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "auto 1d <> " + guild.getPrefix()
                        + "8Ball <> Random quote of the day: \n"
                        + "|  \n"
                        + "|  Try it out a few times to get the hang of it.\n"
                        + "|  \n");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "auto 1d <> " + guild.getPrefix() + "help <> Daily help reminder: \n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "auto 7d <> " + guild.getPrefix() + "all \n"
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