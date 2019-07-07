package com.jesus_crie.modularbot.v8support.discordjs;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.impl.PermissionOverrideImpl;

import javax.annotation.Nonnull;

public class DJSChannel {

    public static void bindChannel(@Nonnull final Channel channel, @Nonnull final V8Object proxy) {
        proxy.add("id", channel.getId());
        proxy.add("type", channel.getType().name());
        proxy.registerJavaMethod((JavaVoidCallback) (r, p) -> channel.delete().complete(), "_delete");
    }

    public static void bindGuildChannel(@Nonnull final TextChannel channel, @Nonnull final V8Object proxy) {
        bindChannel(channel, proxy);

        proxy.add("name", channel.getName());
        proxy.add("position", channel.getPositionRaw());
        proxy.add("parentID", channel.getParent().getId());

        final V8Array permissionOverwrites = new V8Array(proxy.getRuntime());
        for (PermissionOverride override : channel.getPermissionOverrides()) {
            final V8Object v8Override = new V8Object(proxy.getRuntime());
            DJSPermissionOverwrites.bindPermissionOvewrites(override, v8Override);
            v8Override.add("channel", proxy);

            permissionOverwrites.push(v8Override);

            v8Override.close();
        }
    }
}
