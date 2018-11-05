package bowtie.bot.bots.cmd.alias;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.hand.impl.GuildCommandHandler;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class SetAliasCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public SetAliasCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public SetAliasCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        String[] parts = event.getMessage().getContent().split(" ");
        if (parts.length < 3)
        {
            this.bot.sendMessage(
                    "Usage: " + event.getGuildObject().getPrefix() + "alias commandName newAlias",
                    event.getChannel(), Colors.RED);
            return;
        }
        String commandText = parts[1].trim().toLowerCase();
        Command command = ((GuildCommandHandler)event.getGuildObject().getCommandHandler()).getCommand(commandText);
        if (command != null)
        {
            command.addAlias(event.getGuild().getStringID(), parts[2].trim().toLowerCase());
            main.getDatabase().addCommandAlias(
                    event.getGuild().getStringID(),
                    command.getValidExpressions().get(0),
                    parts[2].trim().toLowerCase());

            this.bot.sendMessage("Changed the alias of the '" + command.getValidExpressions().get(0) + "' "
                    + "command to '" + parts[2].trim().toLowerCase() + "'.", event.getChannel(), Colors.GREEN);
            return;
        }

        this.bot.sendMessage("That is not a valid command.", event.getChannel(), Colors.RED);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Add Command Alias Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#alias");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Allows you to add an alias to a command.\n"
                + "|  Adding an alias means that you can now also use that\n"
                + "|  command with the alias text.\n"
                + "|  \n"
                + "|  For example adding the alias 'tc' to the 'teamchannel' \n"
                + "|  command means that you can use 'tc' instead of \n"
                + "|  'teamchannel' to call that command.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "alias commandName aliasName\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "alias teamchannel tc\n"
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