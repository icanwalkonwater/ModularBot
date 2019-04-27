package com.jesus_crie.modularbot.graalvm;

import com.jesus_crie.modularbot.graalvm.js.JSPromiseExecutorProxy;
import com.jesus_crie.modularbot.graalvm.js.NodeEventEmitter;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to allow quick conversions between java objects and the polyglot API.
 */
public class GUtils {

    /**
     * Create a proxy object with members on the fly with key-values pairs.
     * This is a convenience method, not a particularly safe nor good looking method.
     * The arguments need to be provided by pairs of {String, Object}, for example
     * <pre>
     * ProxyObject po = createObject(
     *      "prop1", 42,
     *      "pi", Math.PI,
     *      "numbers", createList(1, 2, 3, 4)
     * );
     * </pre>
     *
     * @param kvs - The key-value pairs.
     * @return A proxy object from the polyglot API.
     */
    @Nonnull
    public static ProxyObject createObject(@Nonnull final Object... kvs) {
        if (kvs.length % 2 != 0)
            throw new IllegalArgumentException("You need to provide an even amount of arguments !");

        final Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            if (!String.class.isAssignableFrom(kvs[i].getClass()))
                throw new IllegalArgumentException("Each key need to be a string !");

            map.put((String) kvs[i], kvs[i + 1]);
        }

        return ProxyObject.fromMap(map);
    }

    /**
     * Alias for {@link ProxyArray#fromList(List)}.
     *
     * @see ProxyArray#fromList(List)
     */
    @Nonnull
    public static ProxyArray createList(@Nonnull final List<Object> list) {
        return ProxyArray.fromList(list);
    }

    /**
     * Alias for {@link ProxyArray#fromArray(Object...)}.
     *
     * @see ProxyArray#fromArray(Object...)
     */
    @Nonnull
    public static ProxyArray createList(@Nonnull final Object... arr) {
        return ProxyArray.fromArray(arr);
    }

    /**
     * Create a JS promise in the given context and with the given executor.
     * This promise isn't wrapped, the executor is, not the promise.
     *
     * @param context  - Context used to instantiate the promise.
     * @param executor - The executor to provide to the promise.
     * @return A promise configured with the given executor.
     */
    @Nonnull
    public static Value createJSPromise(@Nonnull final Context context, @Nonnull final JSPromiseExecutorProxy executor) {
        final Value promiseClass = context.eval("js", "Promise");
        return promiseClass.newInstance(executor);
    }

    /*private static final Source NODE_IMPORT_SOURCE = Source.newBuilder("js",
            "const EventEmitter = Java.type('" + NodeEventEmitter.class.getName() + "');",
            "node_imports")
            .cached(true).encoding(Charset.forName("UTF-8")).buildLiteral();*/

    /**
     * Inject java classes that reflect the behaviour of some Node.JS modules.
     * You can then import them mostly like with the real node.
     * E.g.:
     * {@code
     * const EventEmitter = Polyglot.import('events');
     * <p>
     * var myEE = new EventEmitter();
     * }
     *
     * @param context - The context where to put those bindings.
     */
    public static void injectNodeClasses(@Nonnull final Context context) {
        // Inject the equivalent of the 'events' module of Node.JS
        context.getPolyglotBindings().putMember("events", NodeEventEmitter.class);
    }
}
