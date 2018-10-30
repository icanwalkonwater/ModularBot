package com.jesus_crie.modularbot.utils;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.EmbedType;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Extension of {@link EmbedBuilder EmbedBuilder}, the behaviour of this class is the same as the regular builder but
 * this one will split itself if its fields doesn't fit in one embed.
 * The description and the fields values can be splitted, the author, footer and title can't be splited.
 * <p>
 * The embed will be splited according to the placement of the different fields and the fields can't be repeated.
 * For example if the author, description, footer and image is set and the description is way too long for one embed, the first
 * embed will contain the author and the beginning of the description, the second will contain the end of the description,
 * the image and the footer.
 * <p>
 * If everything can fit in one embed, this builder will work exactly the same as {@link EmbedBuilder EmbedBuilder}.
 * <p>
 * <b>The javadoc from this class is inherited from {@link EmbedBuilder EmbedBuilder} !</b>
 */
class SplittedEmbedBuilder {

    private static final Queue<BiFunction<SplittedEmbedBuilder, EmbedBuilder, EmbedBuilder>> BUILDER = new LinkedList<>();

    static {
        // First set the author and title, they can't be splitted anyway.
        BUILDER.add((builder, prev) -> {
            if (builder.author != null)
                prev.setAuthor(builder.author.getName(), builder.author.getUrl(), builder.author.getIconUrl());

            if (builder.title != null)
                prev.setTitle(builder.title, builder.url);

            return prev;
        });

        // Now process the description
        BUILDER.add((builder, prev) -> {
            final String baseDesc = builder.description.toString().trim();

            if (baseDesc.length() > MessageEmbed.TEXT_MAX_LENGTH) {

                final List<String> descParts = new ArrayList<>();
                final StringBuilder current = new StringBuilder();
                int spaceLeft = MessageEmbed.EMBED_MAX_LENGTH_BOT - prev.length();
                if (spaceLeft > MessageEmbed.TEXT_MAX_LENGTH) spaceLeft = MessageEmbed.TEXT_MAX_LENGTH;

                for (final String s : baseDesc.split("\n")) {
                    if (s.length() > MessageEmbed.TEXT_MAX_LENGTH)
                        throw new IllegalArgumentException("Description contains a line too long to fit un an embed !");

                    if (s.length() >= spaceLeft) {
                        descParts.add(current.toString().trim());
                        current.setLength(0);
                        spaceLeft = MessageEmbed.TEXT_MAX_LENGTH;
                    }

                    current.append(s).append("\n");
                    spaceLeft -= s.length() + 1;
                }
            } else if (MessageEmbed.EMBED_MAX_LENGTH_BOT - prev.length() >= baseDesc.length()) {
                // The description can fit in the embed entirely !
                prev.setDescription(baseDesc);
            } else {
                // Description don't exceed its limit but the embed is full, so put the description in the next embed.

            }

            return prev;
        });
    }

    protected final List<MessageEmbed.Field> fields = new LinkedList<>();
    protected final StringBuilder description = new StringBuilder();
    protected int color = Role.DEFAULT_COLOR_RAW;
    protected String url, title;
    protected OffsetDateTime timestamp;
    protected MessageEmbed.Thumbnail thumbnail;
    protected MessageEmbed.AuthorInfo author;
    protected MessageEmbed.Footer footer;
    protected MessageEmbed.ImageInfo image;

    public SplittedEmbedBuilder() {
    }

    public SplittedEmbedBuilder(@Nonnull final MessageEmbed embed) {
        setDescription(embed.getDescription());
        this.url = embed.getUrl();
        this.title = embed.getTitle();
        this.timestamp = embed.getTimestamp();
        this.color = embed.getColorRaw();
        this.thumbnail = embed.getThumbnail();
        this.author = embed.getAuthor();
        this.footer = embed.getFooter();
        this.image = embed.getImage();
        if (embed.getFields() != null)
            fields.addAll(embed.getFields());
    }

    /**
     * Same method than {@link EmbedBuilder#build()}.
     * <p>
     * Returns a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * that has been checked as being valid for sending.
     *
     * @return the built, sendable {@link net.dv8tion.jda.core.entities.MessageEmbed}
     * @throws java.lang.IllegalStateException If the embed is empty. Can be checked with {@link #isEmpty()}.
     */
    public MessageEmbed buildAsSingle() {
        if (isEmpty())
            throw new IllegalStateException("Cannot build an empty embed!");
        if (!isValidLength(AccountType.BOT))
            throw new IllegalStateException("The embed is too long to be sent !");
        if (description.length() > MessageEmbed.TEXT_MAX_LENGTH)
            throw new IllegalStateException(String.format("Description is longer than %d! Please limit your input!", MessageEmbed.TEXT_MAX_LENGTH));
        final String description = this.description.length() < 1 ? null : this.description.toString();

        return EntityBuilder.createMessageEmbed(url, title, description, EmbedType.RICH, timestamp,
                color, thumbnail, null, author, null, footer, image, new LinkedList<>(fields));
    }

    /**
     * Split and build this builder.
     * If the fields can fit into only one embed, this will return a singleton list.
     *
     * @return
     */
    @Nonnull
    public Queue<MessageEmbed> build() {
        if (isValidLength(AccountType.BOT))
            return new LinkedList<>(Collections.singleton(buildAsSingle()));

        final LinkedList<MessageEmbed> embeds = new LinkedList<>();
        final Queue<BiFunction<SplittedEmbedBuilder, EmbedBuilder, EmbedBuilder>> buildQueue = new LinkedList<>(BUILDER);


        return embeds;
    }

    /**
     * Resets this builder to default state.
     * <br>All parts will be either empty or null after this method has returned.
     *
     * @return The current SplittedEmbedBuilder with default values
     */
    public SplittedEmbedBuilder clear() {
        description.setLength(0);
        fields.clear();
        url = null;
        title = null;
        timestamp = null;
        color = Role.DEFAULT_COLOR_RAW;
        thumbnail = null;
        author = null;
        footer = null;
        image = null;
        return this;
    }

    /**
     * Checks if the given embed is empty. Empty embeds will throw an exception if built
     *
     * @return true if the embed is empty and cannot be built
     */
    public boolean isEmpty() {
        return title == null
                && description.length() == 0
                && timestamp == null
                //&& color == null color alone is not enough to send
                && thumbnail == null
                && author == null
                && footer == null
                && image == null
                && fields.isEmpty();
    }

    /**
     * The overall length of the current SplittedEmbedBuilder in displayed characters.
     * <br>Represents the {@link net.dv8tion.jda.core.entities.MessageEmbed#getLength() MessageEmbed.getLength()} value.
     *
     * @return length of the current builder state
     */
    public int length() {
        int length = description.length();
        synchronized (fields) {
            length = fields.stream().map(f -> f.getName().length() + f.getValue().length()).reduce(length, Integer::sum);
        }
        if (title != null)
            length += title.length();
        if (author != null)
            length += author.getName().length();
        if (footer != null)
            length += footer.getText().length();
        return length;
    }

    /**
     * Checks whether the constructed {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * is within the limits for the specified {@link net.dv8tion.jda.core.AccountType AccountType}
     * <ul>
     * <li>Bot: {@value MessageEmbed#EMBED_MAX_LENGTH_BOT}</li>
     * <li>Client: {@value MessageEmbed#EMBED_MAX_LENGTH_CLIENT}</li>
     * </ul>
     *
     * @param type The {@link net.dv8tion.jda.core.AccountType AccountType} to validate
     * @return True, if the {@link #length() length} is less or equal to the specific limit
     * @throws java.lang.IllegalArgumentException If provided with {@code null}
     */
    public boolean isValidLength(@Nonnull final AccountType type) {
        final int length = length();
        switch (type) {
            case BOT:
                return length <= MessageEmbed.EMBED_MAX_LENGTH_BOT;
            case CLIENT:
            default:
                return length <= MessageEmbed.EMBED_MAX_LENGTH_CLIENT;
        }
    }

    /**
     * Sets the Title of the embed.
     * <br>Overload for {@link #setTitle(String, String)} without URL parameter.
     *
     * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
     *
     * @param title the title of the embed
     * @return the builder after the title has been set
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If the provided {@code title} is an empty String.</li>
     *                                            <li>If the length of {@code title} is greater than {@link net.dv8tion.jda.core.entities.MessageEmbed#TITLE_MAX_LENGTH}.</li>
     *                                            </ul>
     */
    public SplittedEmbedBuilder setTitle(@Nullable final String title) {
        return setTitle(title, null);
    }

    /**
     * Sets the Title of the embed.
     * <br>You can provide {@code null} as url if no url should be used.
     *
     * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
     *
     * @param title the title of the embed
     * @param url   Makes the title into a hyperlink pointed at this url.
     * @return the builder after the title has been set
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If the provided {@code title} is an empty String.</li>
     *                                            <li>If the length of {@code title} is greater than {@link net.dv8tion.jda.core.entities.MessageEmbed#TITLE_MAX_LENGTH}.</li>
     *                                            <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#URL_MAX_LENGTH}.</li>
     *                                            <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *                                            </ul>
     */
    public SplittedEmbedBuilder setTitle(@Nullable final String title, @Nullable String url) {
        if (title == null) {
            this.title = null;
            this.url = null;
        } else {
            if (title.isEmpty())
                throw new IllegalArgumentException("Title cannot be empty!");
            if (title.length() > MessageEmbed.TITLE_MAX_LENGTH)
                throw new IllegalArgumentException("Title cannot be longer than " + MessageEmbed.TITLE_MAX_LENGTH + " characters.");
            if (Helpers.isBlank(url))
                url = null;
            urlCheck(url);

            this.title = title;
            this.url = url;
        }
        return this;
    }

    /**
     * The {@link java.lang.StringBuilder StringBuilder} used to
     * build the description for the embed.
     * <br>Note: To reset the description use {@link #setDescription(CharSequence) setDescription(null)}
     *
     * @return StringBuilder with current description context
     */
    public StringBuilder getDescriptionBuilder() {
        return description;
    }

    /**
     * Sets the Description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     * <p><b><a href="http://i.imgur.com/lbchtwk.png">Example</a></b>
     *
     * @param description the description of the embed, {@code null} to reset
     * @return the builder after the description has been set
     * @throws java.lang.IllegalArgumentException If the length of {@code description} is greater than {@link net.dv8tion.jda.core.entities.MessageEmbed#TEXT_MAX_LENGTH}
     */
    public final SplittedEmbedBuilder setDescription(@Nullable final CharSequence description) {
        this.description.setLength(0);
        if (description != null && description.length() >= 1)
            appendDescription(description);
        return this;
    }

    /**
     * Appends to the description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     * <p><b><a href="http://i.imgur.com/lbchtwk.png">Example</a></b>
     *
     * @param description the string to append to the description of the embed
     * @return the builder after the description has been set
     */
    public SplittedEmbedBuilder appendDescription(@Nonnull final CharSequence description) {
        this.description.append(description);
        return this;
    }

    /**
     * Sets the Timestamp of the embed.
     *
     * <p><b><a href="http://i.imgur.com/YP4NiER.png">Example</a></b>
     *
     * <p><b>Hint:</b> You can get the current time using {@link java.time.Instant#now() Instant.now()} or convert time from a
     * millisecond representation by using {@link java.time.Instant#ofEpochMilli(long) Instant.ofEpochMilli(long)};
     *
     * @param temporal the temporal accessor of the timestamp
     * @return the builder after the timestamp has been set
     */
    public SplittedEmbedBuilder setTimestamp(@Nullable final TemporalAccessor temporal) {
        if (temporal == null) {
            this.timestamp = null;
        } else if (temporal instanceof OffsetDateTime) {
            this.timestamp = (OffsetDateTime) temporal;
        } else {
            ZoneOffset offset;
            try {
                offset = ZoneOffset.from(temporal);
            } catch (DateTimeException ignore) {
                offset = ZoneOffset.UTC;
            }
            try {
                LocalDateTime ldt = LocalDateTime.from(temporal);
                this.timestamp = OffsetDateTime.of(ldt, offset);
            } catch (DateTimeException ignore) {
                try {
                    Instant instant = Instant.from(temporal);
                    this.timestamp = OffsetDateTime.ofInstant(instant, offset);
                } catch (DateTimeException ex) {
                    throw new DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: " +
                            temporal + " of type " + temporal.getClass().getName(), ex);
                }
            }
        }
        return this;
    }

    /**
     * Sets the Color of the embed.
     *
     * <a href="http://i.imgur.com/2YnxnRM.png" target="_blank">Example</a>
     *
     * @param color The {@link java.awt.Color Color} of the embed
     *              or {@code null} to use no color
     * @return the builder after the color has been set
     * @see #setColor(int)
     */
    public SplittedEmbedBuilder setColor(@Nullable final Color color) {
        this.color = color == null ? Role.DEFAULT_COLOR_RAW : color.getRGB();
        return this;
    }

    /**
     * Sets the raw RGB color value for the embed.
     *
     * <a href="http://i.imgur.com/2YnxnRM.png" target="_blank">Example</a>
     *
     * @param color The raw rgb value, or {@link Role#DEFAULT_COLOR_RAW} to use no color
     * @return the builder after the color has been set
     * @see #setColor(java.awt.Color)
     */
    public SplittedEmbedBuilder setColor(final int color) {
        this.color = color;
        return this;
    }

    /**
     * Sets the Thumbnail of the embed.
     *
     * <p><b><a href="http://i.imgur.com/Zc3qwqB.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(java.io.File, net.dv8tion.jda.core.entities.Message) MessageChannel.sendFile(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * @param url the url of the thumbnail of the embed
     * @return the builder after the thumbnail has been set
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#URL_MAX_LENGTH}.</li>
     *                                            <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *                                            </ul>
     */
    public SplittedEmbedBuilder setThumbnail(@Nullable final String url) {
        if (url == null) {
            this.thumbnail = null;
        } else {
            urlCheck(url);
            this.thumbnail = new MessageEmbed.Thumbnail(url, null, 0, 0);
        }
        return this;
    }

    /**
     * Sets the Image of the embed.
     *
     * <p><b><a href="http://i.imgur.com/2hzuHFJ.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(java.io.File, net.dv8tion.jda.core.entities.Message) MessageChannel.sendFile(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * @param url the url of the image of the embed
     * @return the builder after the image has been set
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#URL_MAX_LENGTH}.</li>
     *                                            <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *                                            </ul>
     * @see net.dv8tion.jda.core.entities.MessageChannel#sendFile(java.io.File, String, net.dv8tion.jda.core.entities.Message) MessageChannel.sendFile(...)
     */
    public SplittedEmbedBuilder setImage(@Nullable final String url) {
        if (url == null) {
            this.image = null;
        } else {
            urlCheck(url);
            this.image = new MessageEmbed.ImageInfo(url, null, 0, 0);
        }
        return this;
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     * This convenience method just sets the name.
     *
     * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
     *
     * @param name the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @return the builder after the author has been set
     */
    public SplittedEmbedBuilder setAuthor(@Nullable final String name) {
        return setAuthor(name, null, null);
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     * This convenience method just sets the name and the url.
     *
     * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
     *
     * @param name the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @param url  the url of the author of the embed
     * @return the builder after the author has been set
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#URL_MAX_LENGTH}.</li>
     *                                            <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *                                            </ul>
     */
    public SplittedEmbedBuilder setAuthor(@Nullable final String name, @Nullable final String url) {
        return setAuthor(name, url, null);
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     *
     * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(java.io.File, net.dv8tion.jda.core.entities.Message) MessageChannel.sendFile(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * @param name    the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @param url     the url of the author of the embed
     * @param iconUrl the url of the icon for the author
     * @return the builder after the author has been set
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#URL_MAX_LENGTH}.</li>
     *                                            <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *                                            <li>If the length of {@code iconUrl} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#URL_MAX_LENGTH}.</li>
     *                                            <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
     *                                            </ul>
     */
    public SplittedEmbedBuilder setAuthor(@Nullable final String name, @Nullable final String url, @Nullable final String iconUrl) {
        //We only check if the name is null because its presence is what determines if the
        // the author will appear in the embed.
        if (name == null) {
            this.author = null;
        } else {
            urlCheck(url);
            urlCheck(iconUrl);
            this.author = new MessageEmbed.AuthorInfo(name, url, iconUrl, null);
        }
        return this;
    }

    /**
     * Sets the Footer of the embed.
     *
     * <p><b><a href="http://i.imgur.com/jdf4sbi.png">Example</a></b>
     *
     * <p><b>Uploading images with Embeds</b>
     * <br>When uploading an <u>image</u>
     * (using {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(java.io.File, net.dv8tion.jda.core.entities.Message) MessageChannel.sendFile(...)})
     * you can reference said image using the specified filename as URI {@code attachment://filename.ext}.
     *
     * @param text    the text of the footer of the embed. If this is not set, the footer will not appear in the embed.
     * @param iconUrl the url of the icon for the footer
     * @return the builder after the footer has been set
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If the length of {@code text} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#TEXT_MAX_LENGTH}.</li>
     *                                            <li>If the length of {@code iconUrl} is longer than {@link net.dv8tion.jda.core.entities.MessageEmbed#URL_MAX_LENGTH}.</li>
     *                                            <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
     *                                            </ul>
     */
    public SplittedEmbedBuilder setFooter(@Nullable final String text, @Nullable final String iconUrl) {
        //We only check if the text is null because its presence is what determines if the
        // footer will appear in the embed.
        if (text == null) {
            this.footer = null;
        } else {
            if (text.length() > MessageEmbed.TEXT_MAX_LENGTH)
                throw new IllegalArgumentException("Text cannot be longer than " + MessageEmbed.TEXT_MAX_LENGTH + " characters.");
            urlCheck(iconUrl);
            this.footer = new MessageEmbed.Footer(text, iconUrl, null);
        }
        return this;
    }

    /**
     * Copies the provided Field into a new Field for this builder.
     * <br>For additional documentation, see {@link #addField(String, String, boolean)}
     *
     * @param field the field object to add
     * @return the builder after the field has been added
     */
    public SplittedEmbedBuilder addField(@Nullable final MessageEmbed.Field field) {
        return field == null ? this : addField(field.getName(), field.getValue(), field.isInline());
    }

    /**
     * Adds a Field to the embed.
     *
     * <p>Note: If a blank string is provided to either {@code name} or {@code value}, the blank string is replaced
     * with {@link net.dv8tion.jda.core.EmbedBuilder#ZERO_WIDTH_SPACE}.
     *
     * <p><b><a href="http://i.imgur.com/gnjzCoo.png">Example of Inline</a></b>
     * <p><b><a href="http://i.imgur.com/Ky0KlsT.png">Example if Non-inline</a></b>
     *
     * @param name   the name of the Field, displayed in bold above the {@code value}.
     * @param value  the contents of the field.
     * @param inline whether or not this field should display inline.
     * @return the builder after the field has been added
     * @throws java.lang.IllegalArgumentException <ul>
     *                                            <li>If only {@code name} or {@code value} is set. Both must be set.</li>
     *                                            <li>If the length of {@code name} is greater than {@link net.dv8tion.jda.core.entities.MessageEmbed#TITLE_MAX_LENGTH}.</li>
     *                                            <li>If the length of {@code value} is greater than {@link net.dv8tion.jda.core.entities.MessageEmbed#VALUE_MAX_LENGTH}.</li>
     *                                            </ul>
     */
    public SplittedEmbedBuilder addField(@Nullable final String name, @Nullable final String value, boolean inline) {
        if (name == null && value == null)
            return this;
        this.fields.add(new MessageEmbed.Field(name, value, inline));
        return this;
    }

    /**
     * Adds a blank (empty) Field to the embed.
     *
     * <p><b><a href="http://i.imgur.com/tB6tYWy.png">Example of Inline</a></b>
     * <p><b><a href="http://i.imgur.com/lQqgH3H.png">Example of Non-inline</a></b>
     *
     * @param inline whether or not this field should display inline
     * @return the builder after the field has been added
     */
    public SplittedEmbedBuilder addBlankField(final boolean inline) {
        this.fields.add(new MessageEmbed.Field(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE, inline));
        return this;
    }

    /**
     * Clears all fields from the embed, such as those created with the
     * {@link SplittedEmbedBuilder#SplittedEmbedBuilder(MessageEmbed)}
     * constructor or via the
     * {@link SplittedEmbedBuilder#addField(net.dv8tion.jda.core.entities.MessageEmbed.Field) addField} methods.
     *
     * @return the builder after the field has been added
     */
    public SplittedEmbedBuilder clearFields() {
        this.fields.clear();
        return this;
    }

    /**
     * <b>Modifiable</b> list of {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} Fields that the builder will
     * use for {@link #build()}.
     * <br>You can add/remove Fields and restructure this {@link java.util.List List} and it will then be applied in the
     * built MessageEmbed. These fields will be available again through {@link net.dv8tion.jda.core.entities.MessageEmbed#getFields() MessageEmbed.getFields()}.
     *
     * @return Mutable List of {@link net.dv8tion.jda.core.entities.MessageEmbed.Field Fields}
     */
    public List<MessageEmbed.Field> getFields() {
        return fields;
    }

    private void urlCheck(@Nullable final String url) {
        if (url == null)
            return;
        else if (url.length() > MessageEmbed.URL_MAX_LENGTH)
            throw new IllegalArgumentException("URL cannot be longer than " + MessageEmbed.URL_MAX_LENGTH + " characters.");
        else if (!EmbedBuilder.URL_PATTERN.matcher(url).matches())
            throw new IllegalArgumentException("URL must be a valid http or https url.");
    }
}
