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
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kontinuity.catapult.core.api.StatusMessageEvent;

/**
 * A websocket based resource that informs clients about the status of the operations
 */
@ServerEndpoint("/status")
public class CatapultStatusResource {
    private static Map<UUID, Session> peers = Collections.synchronizedMap(new WeakHashMap<>());

    @OnClose
    public void onClose(Session session) throws IOException {
        for (Map.Entry<UUID, Session> key : peers.entrySet()) {
            if (Objects.equals(key.getValue().getId(), session.getId())) {
                peers.remove(key.getKey());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        peers.put(UUID.fromString(message), session);
    }

    public void onEvent(@Observes StatusMessageEvent msg) throws IOException {
        peers.get(msg.getId()).getBasicRemote().sendText(serialise(msg));
    }

    private String serialise(StatusMessageEvent msg) {
        try {
            return new ObjectMapper().writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("could not serialise message to json", e);
        }
    }
}
