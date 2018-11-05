package bowtie.bot.bots.cmd.auto;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.bot.auto.AutomatedCommand;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class RemoveAutomatedCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public RemoveAutomatedCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public RemoveAutomatedCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        String[] parts = event.getFixedContent().split(" ");

        long id = -1;
        try
        {
            id = Long.parseLong(parts[1]);
        }
        catch (Exception e)
        {
            this.bot.sendMessage("You have to add a positive number.", event.getChannel(), Colors.RED);
            return;
        }

        AutomatedCommand command = AutomatedCommand.get(id, event.getGuildObject().getStringID());

        if (command == null)
        {
            this.bot.sendMessage("There is no active automated command with that number on your server.",
                    event.getChannel(), Colors.RED);
            return;
        }

        command.stop();
        AutomatedCommand.remove(command);
        main.getDatabase().removeAutomatedCommand(id);
        this.bot.sendMessage("The automated command has been stopped.", event.getChannel(), Colors.GREEN);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Stop Automation Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#automate");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will stop the automation with the given ID.\n"
                + "|  Use the " + guild.getPrefix() + "showautomated' command to see all \n"
                + "|  automations with their IDs.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "stopauto 43\n"
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