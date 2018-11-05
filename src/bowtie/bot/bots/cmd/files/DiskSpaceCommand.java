package bowtie.bot.bots.cmd.files;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
public class DiskSpaceCommand extends Command
{
    private Bot bot;

    /**
     * @param validExpressions
     * @param permission
     */
    public DiskSpaceCommand(String[] validExpressions, int permission, Bot bot)
    {
        super(validExpressions, permission);
        this.bot = bot;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public DiskSpaceCommand(List<String> validExpressions, int permission, Bot bot)
    {
        super(validExpressions, permission);
        this.bot = bot;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        String jarFolderSize = null;
        String logFolderSize = null;
        String dbFolderSize = null;
        String libFolderSize = null;
        String jarSize = null;
        String propertiesFolderSize = null;

        try
        {
            String jarParentPath = getJarParentFile().getAbsolutePath();
            jarFolderSize = formatSize(size(new File(jarParentPath).toPath()));
            dbFolderSize = formatSize(size(new File(jarParentPath + "/db").toPath()));
            logFolderSize = formatSize(size(new File(jarParentPath + "/logs").toPath()));
            libFolderSize = formatSize(size(new File(jarParentPath + "/bowtiebot_lib").toPath()));
            jarSize = formatSize(new File(jarParentPath + "/bowtiebot.jar").length());
            propertiesFolderSize = formatSize(size(new File(jarParentPath + "/properties").toPath()));
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        bot.sendMessage("```Total: " + jarFolderSize + "\n\n"
                + "Jar: " + jarSize + "\n"
                + "Lib: " + libFolderSize + "\n"
                + "Db: " + dbFolderSize + "\n"
                + "Prop: " + propertiesFolderSize + "\n"
                + "Logs: " + logFolderSize + "```", event.getMessage().getChannel(), Colors.PURPLE);
    }

    public File getJarParentFile()
    {
        try
        {
            CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
            File jarFile;
            if (codeSource.getLocation() != null)
            {
                jarFile = new File(codeSource.getLocation().toURI());
            }
            else
            {
                String path = getClass().getResource(getClass().getSimpleName() + ".class").getPath();
                String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
                jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
                jarFile = new File(jarFilePath);
            }
            return jarFile.getParentFile();
        }
        catch (Exception e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public long size(Path path)
    {
        final AtomicLong size = new AtomicLong(0);
        try
        {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            Bot.errorLog.print(this, e);
        }
        return size.get();
    }

    private String formatSize(long size)
    {
        String[] units =
        {
                "b", "kb", "mb", "gb"
        };
        float actSize = (float)size;
        String unit = units[0];
        for (int i = 0; i < 4; i ++ )
        {
            if (actSize >= 1000)
            {
                actSize /= 1000;
                unit = units[i + 1];
            }
        }
        return (String.format("%.2f", actSize) + " " + unit).replace(",", ".");
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Disk Space Command");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Soon\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "disk\n"
                        + "|", false);

        builder.appendField(
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
                "[This command needs " + UserPermissions.getPermissionString(this.getPermissionOverride(guild))
                        + " permissions](http://www.bowtiebots.xyz/master-system.html)", false);

        return builder.build();
    }
}