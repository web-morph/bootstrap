package com.github.webmorph.event;

import com.github.webmorph.eventbus.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.GenericApplicationContext;

@Getter
@RequiredArgsConstructor
public class ApplicationContextInitializeEvent extends Event {
    private final GenericApplicationContext context;

}
