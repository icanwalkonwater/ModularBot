package com.jesus_crie.modularbot.utils;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class SplittedEmbedBuilderTest {

    private static final String lorem2004 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam luctus dapibus ante. Maecenas semper vitae urna sed consectetur. Curabitur erat erat, maximus vel scelerisque et, bibendum ut arcu. Maecenas velit orci, rhoncus dapibus sollicitudin eu, viverra faucibus elit. Nulla in ultrices augue, sed mollis elit. Aliquam nec elementum erat. Sed feugiat tellus a elit finibus venenatis. Maecenas dignissim commodo odio id pharetra. Suspendisse porttitor metus ac ex aliquet, at fringilla dolor auctor. Proin ultricies orci sed ante cursus congue. Duis quis imperdiet odio, vitae cursus lorem. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae;\n" +
            "\n" +
            "Cras vulputate lacus sed pharetra ullamcorper. Aenean nec aliquam magna, nec porttitor ex. Donec ac orci ac orci placerat vulputate. Pellentesque vitae orci molestie, mattis nisl a, interdum nibh. Sed commodo neque sollicitudin augue placerat molestie. Nunc ut libero at turpis auctor consectetur eget sagittis dolor. Maecenas cursus, ante id mollis hendrerit, ipsum elit gravida nisl, vitae finibus nulla tellus eget neque. Ut eleifend mattis tortor at laoreet. Quisque pellentesque, nisl ut fringilla aliquet, dui odio viverra mi, non volutpat magna diam bibendum turpis. Sed dui diam, consectetur vitae dapibus ut, vehicula id ligula. Mauris ac tortor sit amet sapien pellentesque semper. Ut felis velit, iaculis a eros ut, ultrices tempus leo. Mauris vel imperdiet eros.\n" +
            "\n" +
            "Suspendisse molestie est in metus pellentesque viverra sed non eros. Vestibulum sit amet lacus non odio molestie dapibus in nec felis. Pellentesque quis quam ante. Mauris eros tellus, fermentum non vulputate eu, faucibus a mi. Nam viverra ultricies arcu fermentum lobortis. Maecenas tempor est nec nisl lobortis, ornare fringilla mauris aliquam. Quisque feugiat finibus nisl at gravida. Etiam euismod eget tortor eu rutrum. Nulla congue mollis orci eu mattis. Morbi rutrum orci sit amet justo varius vulputate. Integer pretium tincidunt libero sed.";

    private static final String lorem1002 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In sit amet finibus velit. Ut maximus sit amet neque ac sagittis. In consequat non orci dignissim sagittis. Integer ac lacus ultricies, accumsan nunc id, blandit tortor. Pellentesque eget odio ut tortor dapibus gravida. Suspendisse feugiat iaculis porttitor. Aliquam ut enim ut magna varius viverra. Mauris blandit ex sit amet varius laoreet. Vivamus ut placerat tellus. Praesent pretium diam ipsum, a euismod metus congue nec. Mauris dolor urna, porta quis porttitor sed, facilisis nec magna. Praesent congue mauris et velit accumsan lacinia. Curabitur pretium sapien sed vehicula vehicula. Suspendisse interdum sollicitudin ultrices. In at accumsan nulla.\n" +
            "\n" +
            "Ut tellus ligula, aliquam in eleifend viverra, tristique vel leo. Aenean condimentum tempor mi, quis egestas mauris egestas sit amet. Sed pellentesque sagittis est, quis sagittis enim molestie vel. Pellentesque volutpat eleifend lectus in placerat. Integer in condimentum orci. In amet.";

    private SplittedEmbedBuilder builder;

    @BeforeEach
    void setup() {
        builder = new SplittedEmbedBuilder();
    }

    @Test
    void testNonSplittedEmbed() {
        builder.setTitle("Hey", null);
        MessageEmbed m = builder.buildAsSingle();

        assertThat(m.getTitle(), equalTo("Hey"));
        assertThat(m.getUrl(), is(nullValue()));
        assertThat(m.getColorRaw(), is(Role.DEFAULT_COLOR_RAW));
    }

    @Test
    void testTooLongEmbedDescription() {
        builder.setDescription(lorem2004);
        builder.addField("Hey", lorem1002, false);
        builder.addField("Ho", lorem1002, false);
        builder.addField("Ho", lorem1002, false);
        builder.addField("Ho", lorem1002, false);
        builder.addField("Ho", lorem1002, false);
        builder.addField("Ho", lorem1002, false);

        assertThrows(IllegalStateException.class, () -> builder.buildAsSingle());
    }
}