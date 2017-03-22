package org.kontinuity.catapult.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 * Websocket message listener for assertions.
 */
@ClientEndpoint
public class StatusTestClientEndpoint {
    private static final Logger log = Logger.getLogger(StatusTestClientEndpoint.class.getName());
    private Set<String> messagesReceived = Collections.synchronizedSet(new HashSet<String>());

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("session.getId() = " + session.getId());
    }

    @OnMessage
    public void onMessage(String message) {
        messagesReceived.add(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info(String.format("Session %s close because of %s", session.getId(), closeReason));
    }

    Set<String> getMessagesReceived() {
        return messagesReceived;
    }
}
