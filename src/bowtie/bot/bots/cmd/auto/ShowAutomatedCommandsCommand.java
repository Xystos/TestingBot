package bowtie.bot.bots.cmd.auto;

import java.util.ArrayList;
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
public class ShowAutomatedCommandsCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public ShowAutomatedCommandsCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public ShowAutomatedCommandsCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        List<AutomatedCommand> commands = AutomatedCommand.get(event.getGuildObject().getStringID());

        List<String> titles = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        for (AutomatedCommand command : commands)
        {
            titles.add(Long.toString(command.getID()));
            String message = "";
            if (command.getAddText() != null)
            {
                message += command.getAddText() + "\n";
            }
            message += command.getCommandString();
            messages.add(message);
        }

        bot.sendListMessage("Active automated commands on this server:", titles, messages, event.getChannel(),
                Colors.DEFAULT, 10, false);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Show Automations Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#automate");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will show all currently active command automations\n"
                + "|  for your server.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "showautomated\n"
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