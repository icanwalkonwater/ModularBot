package com.jesus_crie.modularbot.v8support.discordjs;

import com.eclipsesource.v8.V8Object;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import javax.annotation.Nonnull;

public class DJSPermissionOverwrites {

    public static void bindPermissionOvewrites(@Nonnull final PermissionOverride override, @Nonnull final V8Object proxy) {
        proxy.add("id", override.isMemberOverride() ? override.getMember().getUser().getId() : override.getRole().getId());
        proxy.add("type", override.isMemberOverride() ? "member" : "role");
        proxy.add("allow", override.getAllowedRaw());
        proxy.add("deny", override.getDeniedRaw());

        proxy.registerJavaMethod((receiver, parameters) -> {
            final AuditableRestAction<Void> action = override.delete();

            if (parameters.length() == 1)
                action.reason(parameters.getString(0));

            action.complete();
        }, "_delete");
    }
}
