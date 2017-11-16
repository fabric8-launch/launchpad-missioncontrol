package io.openshift.appdev.missioncontrol.core.api;

import java.util.UUID;

import javax.enterprise.event.Event;

import static java.util.Collections.singletonMap;

/**
 * Abstract command that can fire CDI events
 */
public abstract class AbstractCommand {

    private Event<StatusMessageEvent> statusEvent;

    public AbstractCommand(Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
    }

    protected void fireEvent(UUID id, Object result) {
        statusEvent.fire(new StatusMessageEvent(id, getStatusEventType(), singletonMap("location", result)));
    }

    protected void fireEvent(UUID id) {
        statusEvent.fire(new StatusMessageEvent(id, getStatusEventType()));
    }

    protected abstract StatusEventType getStatusEventType();
}
