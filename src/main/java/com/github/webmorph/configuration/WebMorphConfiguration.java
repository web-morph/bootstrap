package com.github.webmorph.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Root configuration class for the WebMorph application context.
 * <p>
 * Enables component scanning for the {@code dev.ckateptb.webmorph} base package,
 * allowing automatic detection of annotated components such as
 * {@code @Component}, {@code @Service}, {@code @Repository}, and {@code @Controller}.
 * <p>
 * This class serves as a central bootstrap for application-wide Spring configuration.
 */
@Configuration
@ComponentScan(basePackages = "com.github.webmorph")
public class WebMorphConfiguration {
}
