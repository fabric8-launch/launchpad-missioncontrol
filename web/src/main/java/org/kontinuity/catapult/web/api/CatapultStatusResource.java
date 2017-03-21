package org.kontinuity.catapult.web.api;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.enterprise.event.Observes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.kontinuity.catapult.core.api.StatusMessageEvent;

/**
 * A websocket based resource that informs clients about the status of the operations
 */
@ServerEndpoint("/status")
public class CatapultStatusResource {
    private static Map<UUID, String> mapping = Collections.synchronizedMap(new WeakHashMap<>());
    private static Map<String, Session> peers = Collections.synchronizedMap(new WeakHashMap<>());

    @OnOpen
    public void onOpen(Session session) throws IOException {
        peers.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        peers.remove(session.getId());
        for (Map.Entry<UUID, String> key : mapping.entrySet()) {
            if (Objects.equals(key.getValue(), session.getId())) {
                mapping.remove(key.getKey());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        mapping.put(UUID.fromString(message), session.getId());
    }

    public void onEvent(@Observes StatusMessageEvent msg) throws IOException {
        String clientId = mapping.get(msg.getId());
        peers.get(clientId).getBasicRemote().sendText(msg.getFormattedMessage());
    }
}
