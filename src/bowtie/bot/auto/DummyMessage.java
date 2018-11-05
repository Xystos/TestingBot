package bowtie.bot.auto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmbed;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;

import com.vdurmont.emoji.Emoji;

public class DummyMessage implements IMessage
{

    /**
     * The unique snowflake ID of the object.
     */
    protected final long id;

    /**
     * The raw content of the message.
     */
    protected volatile String content;

    /**
     * The author of the message.
     */
    protected final User author;

    /**
     * The channel the message was sent in.
     */
    protected final Channel channel;

    /**
     * The users mentioned in the message.
     */
    protected volatile List<Long> mentions;

    /**
     * The roles mentioned in the message.
     */
    protected volatile List<Long> roleMentions;

    /**
     * Whether the message mentions everyone.
     */
    protected volatile boolean mentionsEveryone;

    /**
     * Gets whether the message mentions all online users.
     */
    protected volatile boolean mentionsHere;

    /**
     * Whether an @everyone mention in the message would be valid. (If the author of the message has permission to
     * mention everyone)
     */
    protected volatile boolean everyoneMentionIsValid;

    /**
     * The channels mentioned in the message.
     */
    protected final List<IChannel> channelMentions;

    /**
     * The client the message belongs to.
     */
    protected final IDiscordClient client;

    /**
     * The message's content with human-readable mentions. This is lazily evaluated.
     */
    protected volatile String formattedContent = null;

    /**
     * Pattern for Discord's channel mentions.
     */
    private static final Pattern CHANNEL_PATTERN = Pattern.compile("<#([0-9]{1,19})>");

    /**
     * Whether the message was deleted.
     */
    private volatile boolean deleted = false;

    public DummyMessage(IDiscordClient client, long id, String content, IUser user, IChannel channel,
            boolean mentionsEveryone, List<Long> mentions, List<Long> roleMentions)
    {
        this.client = client;
        this.id = id;
        setContent(content);
        this.author = (User)user;
        this.channel = (Channel)channel;
        this.mentions = mentions;
        this.roleMentions = roleMentions;
        this.channelMentions = new ArrayList<>();
        this.everyoneMentionIsValid = mentionsEveryone;

        setChannelMentions();
    }

    public DummyMessage(IDiscordClient client, SerializableDummyMessage message)
    {
        this.client = client;
        this.id = message.getID();
        setContent(message.getContent());

        this.author = RequestBuffer.request(new IRequest<User>()
        {
            @Override
            public User request()
            {
                return (User)client.fetchUser(message.getAuthorID());
            }
        }).get();

        this.channel = RequestBuffer.request(new IRequest<Channel>()
        {
            @Override
            public Channel request()
            {
                return (Channel)client.getChannelByID(message.getChannelID());
            }
        }).get();

        this.mentions = message.getMentions();
        this.roleMentions = message.getRoleMentions();
        this.channelMentions = new ArrayList<>();
        this.everyoneMentionIsValid = message.mentionsEveryone();

        setChannelMentions();
    }

    @Override
    public String getContent()
    {
        return content;
    }

    /**
     * Sets the CACHED content of the message.
     *
     * @param content
     *            The content of the message.
     */
    public void setContent(String content)
    {
        this.content = content;
        this.formattedContent = null; // Force re-update later

        if (content != null)
        {
            this.mentionsEveryone = content.contains("@everyone");
            this.mentionsHere = content.contains("@here");
        }
    }

    /**
     * Populates the channel mentions list.
     */
    public void setChannelMentions()
    {
        if (content != null)
        {
            channelMentions.clear();
            Matcher matcher = CHANNEL_PATTERN.matcher(content);

            while (matcher.find())
            {
                long mentionedID = Long.parseUnsignedLong(matcher.group(1));
                IChannel mentioned = client.getChannelByID(mentionedID);

                if (mentioned != null)
                {
                    channelMentions.add(mentioned);
                }
            }
        }
    }

    @Override
    public IChannel getChannel()
    {
        return channel;
    }

    @Override
    public IUser getAuthor()
    {
        return author;
    }

    @Override
    public long getLongID()
    {
        return id;
    }

    @Override
    public Instant getTimestamp()
    {
        return null;
    }

    public List<Long> getLongMentions()
    {
        return this.mentions;
    }

    @Override
    public List<IUser> getMentions()
    {
        if (mentionsEveryone)
        {
            return channel.isPrivate() ? channel.getUsersHere() : channel.getGuild().getUsers();
        }

        return mentions.stream().map(client::getUserByID).collect(Collectors.toList());
    }

    public List<Long> getLongRoleMentions()
    {
        return this.roleMentions;
    }

    @Override
    public List<IRole> getRoleMentions()
    {
        return roleMentions.stream().map(m -> getGuild().getRoleByID(m)).collect(Collectors.toList());
    }

    @Override
    public List<IChannel> getChannelMentions()
    {
        return channelMentions;
    }

    @Override
    public List<Attachment> getAttachments()
    {
        return null;
    }

    @Override
    public List<IEmbed> getEmbeds()
    {
        return null;
    }

    @Override
    public IMessage reply(String content)
    {
        return reply(content, null);
    }

    @Override
    public IMessage reply(String content, EmbedObject embed)
    {
        return this;
    }

    @Override
    public IMessage edit(String content)
    {
        return edit(content, null);
    }

    @Override
    public IMessage edit(EmbedObject embed)
    {
        return edit(null, embed);
    }

    @Override
    public IMessage edit(String content, EmbedObject embed)
    {
        return this;
    }

    @Override
    public boolean mentionsEveryone()
    {
        return everyoneMentionIsValid && mentionsEveryone;
    }

    @Override
    public boolean mentionsHere()
    {
        return everyoneMentionIsValid && mentionsHere;
    }

    @Override
    public void delete()
    {

    }

    @Override
    public Optional<Instant> getEditedTimestamp()
    {
        return null;
    }

    @Override
    public boolean isPinned()
    {
        return false;
    }

    @Override
    public IMessage copy()
    {
        return new DummyMessage(client, id, content, author, channel, everyoneMentionIsValid, mentions, roleMentions);
    }

    @Override
    public IGuild getGuild()
    {
        return getChannel().isPrivate() ? null : getChannel().getGuild();
    }

    @Override
    public String getFormattedContent()
    {
        if (content == null)
            return null;

        if (formattedContent == null)
        {
            String currentContent = content;

            for (IUser u : getMentions())
                currentContent = currentContent.replace(u.mention(false), "@" + u.getName()).replace(u.mention(true),
                        "@" + u.getDisplayName(getGuild()));

            for (IChannel ch : getChannelMentions())
                currentContent = currentContent.replace(ch.mention(), "#" + ch.getName());

            for (IRole r : getRoleMentions())
                currentContent = currentContent.replace(r.mention(), "@" + r.getName());

            formattedContent = currentContent;
        }

        return formattedContent;
    }

    @Override
    public List<IReaction> getReactions()
    {
        return null;
    }

    @Override
    public IReaction getReactionByEmoji(IEmoji emoji)
    {
        return getReactionByID(emoji.getLongID());
    }

    @Override
    public IReaction getReactionByEmoji(ReactionEmoji emoji)
    {
        return getReactions().stream().filter(r -> r.getEmoji().equals(emoji)).findFirst().orElse(null);
    }

    @Override
    public IReaction getReactionByID(long id)
    {
        return getReactions().stream().filter(r -> r.getEmoji().getLongID() == id).findFirst().orElse(null);
    }

    @Override
    public IReaction getReactionByUnicode(Emoji unicode)
    {
        return getReactionByUnicode(unicode.getUnicode());
    }

    @Override
    public IReaction getReactionByUnicode(String unicode)
    {
        return getReactions().stream().filter(r -> r.getEmoji().isUnicode() && r.getEmoji().getName().equals(unicode))
                .findFirst().orElse(null);
    }

    @Override
    public void addReaction(IReaction reaction)
    {
    }

    @Override
    public void addReaction(IEmoji emoji)
    {
    }

    @Override
    public void addReaction(Emoji emoji)
    {
    }

    @Override
    public void addReaction(ReactionEmoji reactionEmoji)
    {
    }

    @Override
    public void removeReaction(IUser user, IReaction reaction)
    {
    }

    @Override
    public void removeReaction(IUser user, IEmoji emoji)
    {
    }

    @Override
    public void removeReaction(IUser user, Emoji emoji)
    {
    }

    @Override
    public void removeReaction(IUser user, ReactionEmoji reactionEmoji)
    {
    }

    @Override
    public void removeReaction(IUser user, String emoji)
    {
    }

    @Override
    public void removeAllReactions()
    {
    }

    @Override
    public long getWebhookLongID()
    {
        return -1;
    }

    @Override
    public MessageTokenizer tokenize()
    {
        return new MessageTokenizer(this);
    }

    @Override
    public boolean isDeleted()
    {
        return deleted;
    }

    @Override
    public Type getType()
    {
        return null;
    }

    @Override
    public boolean isSystemMessage()
    {
        return !getType().equals(Type.DEFAULT);
    }

    @Override
    public IDiscordClient getClient()
    {
        return client;
    }

    @Override
    public IShard getShard()
    {
        return getChannel().getShard();
    }

    @Override
    public String toString()
    {
        return content;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object other)
    {
        return DiscordUtils.equals(this, other);
    }
}
