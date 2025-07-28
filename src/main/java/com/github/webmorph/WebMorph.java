package com.github.webmorph;

import com.github.webmorph.configuration.WebMorphConfiguration;
import com.github.webmorph.event.ApplicationContextInitializeEvent;
import com.github.webmorph.event.MixinTransformerRegistrationEvent;
import com.github.webmorph.eventbus.EventBus;
import dev.ckateptb.reflection.Reflect;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.additionalclassprovider.GuavaClassPathProvider;
import net.lenni0451.classtransform.mixinstranslator.MixinsTranslator;
import net.lenni0451.reflect.Agents;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Core entrypoint and bootstrap utility for initializing the WebMorph runtime.
 * <p>
 * This class sets up:
 * <ul>
 *   <li>Mixin-based bytecode transformers using {@link TransformerManager}</li>
 *   <li>Spring Boot context with {@link WebMorphConfiguration}</li>
 *   <li>Optional loading of {@code application.properties} from classpath</li>
 * </ul>
 */
@Slf4j
public class WebMorph {
    private static final AtomicReference<GenericApplicationContext> context = new AtomicReference<>();

    /**
     * Bootstraps the WebMorph environment, applies mixin transformers,
     * and launches the Spring context with the given entrypoint class.
     * <p>
     * Also reads {@code application.properties} if found in the classpath root.
     *
     * @param clazz the main application class (e.g., {@code Application.class})
     * @param args  command-line arguments passed to Spring Boot
     * @return the fully initialized {@link GenericApplicationContext}
     */
    @SneakyThrows
    public static GenericApplicationContext bootstrap(Class<?> clazz, String[] args) {
        GenericApplicationContext context = WebMorph.context.get();
        if (context != null) {
            log.warn("WebMorph already initialized.");
            return context;
        }
        // Initialize bytecode transformer system
        TransformerManager transformer = new TransformerManager(new GuavaClassPathProvider());
        transformer.addTransformerPreprocessor(new MixinsTranslator());

        // Register and apply custom mixin transformers
        MixinTransformerRegistrationEvent event = new MixinTransformerRegistrationEvent();
        event.addTransformer(WebMorph.class.getPackageName() + ".mixin.**");
        event.dispatch();
        event.getTransformers().forEach(transformer::addTransformer);

        // Hook instrumentation agent
        transformer.hookInstrumentation(Agents.getInstrumentation());

        // Use isolated classloader (Guava-based) for scanning
        ClassLoader classLoader = GuavaClassPathProvider.class.getClassLoader();

        // Build Spring application context
        WebMorph.context.set((GenericApplicationContext) new SpringApplicationBuilder(WebMorphConfiguration.class, clazz)
                .contextFactory(webApplicationType -> {
                    try {
                        return Reflect.on(switch (webApplicationType) {
                                    case SERVLET ->
                                            "org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext";
                                    case REACTIVE ->
                                            "org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext";
                                    case NONE -> "org.springframework.context.support.GenericApplicationContext";
                                }).getDefaultConstructor().orElseThrow().invoke()
                                .cast(GenericApplicationContext.class).getValue();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .headless(true)
                .bannerMode(Banner.Mode.OFF)
                .initializers(ctx -> {
                    ctx.setClassLoader(classLoader);
                    ConfigurableEnvironment environment = ctx.getEnvironment();
                    try (InputStream input = classLoader.getResourceAsStream("application.properties")) {
                        if (input != null) {
                            Properties props = new Properties();
                            props.load(input);
                            PropertiesPropertySource source = new PropertiesPropertySource("properties", props);
                            environment.getPropertySources().addLast(source);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .run(args));
        GenericApplicationContext ctx = WebMorph.context.get();
        EventBus eventBus = ctx.getBean(EventBus.class);
        Reflect.on(eventBus).getMethodWithNameAndParameters("init", ApplicationContext.class)
                .orElseThrow()
                .invoke(ctx);
        eventBus.dispatchEvent(new ApplicationContextInitializeEvent(ctx));

        // Log registered transformers
        Set<String> transformers = transformer.getRegisteredTransformer();
        log.info("Registered mixin's ({}): {}", transformers.size(), transformers);

        return WebMorph.context.get();
    }
}
