package com.github.webmorph.event;

import com.github.webmorph.eventbus.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lenni0451.classtransform.exceptions.TransformerLoadException;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This event is fired when WebMorph is about to initialize its Mixin system.
 * <p>
 * It provides a hook to register your own transformer packages or classes that contain {@link Mixin}-annotated mixins.
 * <p>
 * Use this event to declare where your custom mixins are located before WebMorph begins class transformation.
 * <p>
 * Typical usage:
 * <pre>{@code
 * MixinTransformerRegistrationEvent.on(event ->
 *     event.addTransformer("com.example.mixins.**")
 * );
 * }</pre>
 *
 * @see Mixin
 */
@Getter
@RequiredArgsConstructor
public class MixinTransformerRegistrationEvent extends Event {
    private final Set<String> transformers = new HashSet<>();

    /**
     * Add a transformer class to the transformer list.<br>
     * Use the direct class name for a single transformer <i>(e.g. <b>package.Transformer</b>)</i><br>
     * Use the package ending with '*' for all transformer in the packet (not sub packages) <i>(e.g. <b>package.*</b>)</i><br>
     * Use the package ending with '**' for all transformer in the package and sub packages <i>(e.g. <b>package.**</b>)</i><br>
     * If the class is specified directly an exception will be thrown if the class is missing the {@link Mixin} annotation.
     *
     * @param transformer The name of transformer class to add
     * @throws IllegalStateException    If the class is specified directly and is missing the {@link Mixin} annotation
     * @throws TransformerLoadException If the transformer could not be loaded
     * @throws RuntimeException         If the class bytecode could not be parsed using ASM
     */
    public void addTransformer(final String transformer) {
        this.transformers.add(transformer);
    }

    /**
     * @return The names of all registered transformers
     */
    public Collection<String> getTransformers() {
        return Collections.unmodifiableSet(this.transformers);
    }
}
