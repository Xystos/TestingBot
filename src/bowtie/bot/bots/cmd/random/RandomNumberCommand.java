package bowtie.bot.bots.cmd.random;

import java.util.List;
import java.util.SplittableRandom;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class RandomNumberCommand extends Command
{
    private Bot bot;
    private Main main;
    SplittableRandom r;

    /**
     * @param validExpressions
     * @param permission
     */
    public RandomNumberCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        r = new SplittableRandom();
    }

    public RandomNumberCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        r = new SplittableRandom();
    }

    /**
     * @see bowt.cmnd.Command#execute(bowt.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        String[] parts = event.getMessage().getContent().trim().toLowerCase().split(" ");
        long number = 6;
        long rolls = 1;
        boolean highest = false;
        boolean lowest = false;

        try
        {
            if (parts.length > 1)
            {
                number = Long.parseLong(parts[1].trim());
            }
            if (parts.length > 2)
            {
                if (parts[2].trim().toLowerCase().equals("max"))
                {
                    highest = true;
                    rolls = number;
                    number = 6;
                }
                else if (parts[2].trim().toLowerCase().equals("min"))
                {
                    lowest = true;
                    rolls = number;
                    number = 6;
                }
                else
                {
                    rolls = Long.parseLong(parts[2].trim());
                }
            }
            if (parts.length > 3)
            {
                number = Long.parseLong(parts[1].trim());
                rolls = Long.parseLong(parts[2].trim());
                highest = parts[3].trim().toLowerCase().equals("max");
                lowest = parts[3].trim().toLowerCase().equals("min");
            }
        }
        catch (NumberFormatException e)
        {
            return;
        }

        if (number > Integer.MAX_VALUE)
        {
            this.bot.sendMessage("That number is too high. The bot can only pick numbers from 1 to "
                    + Integer.MAX_VALUE + ".", event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (rolls > 100)
        {
            this.bot.sendMessage("The bot can only pick up to 100 numbers at once.", event.getMessage().getChannel(),
                    Colors.RED);
            return;
        }

        if (number < 1 || rolls < 1)
        {
            this.bot.sendMessage("The bot can only use numbers above 0.", event.getMessage().getChannel(), Colors.RED);
            return;
        }

        int num;

        if (rolls == 1)
        {
            num = r.nextInt((int)number) + 1;
            this.bot.sendMessage(Integer.toString(num), event.getChannel(), Colors.PURPLE);
        }
        else if (rolls > 0)
        {
            int[] numbers = new int[(int)rolls];
            String numberString = "";
            int highestNum = Integer.MIN_VALUE;
            int lowestNum = Integer.MAX_VALUE;

            for (int i = 0; i < rolls; i ++ )
            {
                num = r.nextInt((int)number) + 1;

                if (num > highestNum)
                {
                    highestNum = num;
                }
                if (num < lowestNum)
                {
                    lowestNum = num;
                }

                numbers[i] = num;

                numberString += num + "\n\n";
            }

            if (highest)
            {
                numberString = "The highest number out of " + rolls + " rolls:\n\n" + highestNum;
            }
            if (lowest)
            {
                numberString = "The lowest number out of " + rolls + " rolls:\n\n" + lowestNum;
            }

            this.bot.sendMessage(numberString, event.getChannel(), Colors.PURPLE);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Roll Random Number Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html/#random_number");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Rolls a random number between 1 and the\n"
                + "|  upper bound you specified.\n"
                + "|  If you don't specify a number the bot will pick between \n"
                + "|  1 and 6\n"
                + "|  \n"
                + "|  You can add a second number after the first one to make \n"
                + "|  the bot roll multiple numbers at once.\n"
                + "|  \n"
                + "|  If you add 'max' at the end the bot will only show the \n"
                + "|  highest number it rolled. If you add 'min' it only shows \n"
                + "|  the lowest one."
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "roll 5\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "roll 5 10\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "roll 100 50 max\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "roll 100 50 min\n"
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