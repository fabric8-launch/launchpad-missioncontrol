package org.kontinuity.catapult.test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
public class StatusTestClientEndpoint {
    private static final Logger log = Logger.getLogger(StatusTestClientEndpoint.class.getName());

    private List<String> messagesReceived = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("************************ CLIENT OPEN SESSION: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("************************ CLIENT RECEIVED MESSAGE: " + message);
        messagesReceived.add(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("****************** CLIENT CLOSING SESSION");
        log.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }

    List<String> getMessagesReceived() {
        return messagesReceived;
    }
}
