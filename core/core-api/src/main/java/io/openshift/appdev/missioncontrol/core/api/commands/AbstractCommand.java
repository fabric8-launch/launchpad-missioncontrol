package io.openshift.appdev.missioncontrol.core.api.commands;

import java.util.Map;
import java.util.UUID;

import javax.enterprise.event.Event;

import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;

import static java.util.Collections.singletonMap;

/**
 * Abstract command that can fire CDI events
 */
public abstract class AbstractCommand implements Command {

    private Event<StatusMessageEvent> statusEvent;

    public AbstractCommand(Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
    }

    protected void fireEvent(UUID id, Object result) {
        statusEvent.fire(new StatusMessageEvent(id, getStatusMessage(), singletonMap("location", result)));
    }

    protected void fireEvent(UUID id) {
        statusEvent.fire(new StatusMessageEvent(id, getStatusMessage()));
    }
}
