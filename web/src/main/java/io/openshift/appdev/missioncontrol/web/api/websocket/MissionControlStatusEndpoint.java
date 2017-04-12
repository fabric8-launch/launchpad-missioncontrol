package io.openshift.appdev.missioncontrol.web.api.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openshift.appdev.missioncontrol.core.api.StatusMessage;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;

/**
 * A websocket based resource that informs clients about the status of the operations
 *
 * https://abhirockzz.wordpress.com/2015/02/10/integrating-cdi-and-websockets/
 */
@ServerEndpoint(value = "/status/{uuid}")
public class MissionControlStatusEndpoint {
    private static final Logger log = Logger.getLogger(MissionControlStatusEndpoint.class.getName());

    private static Map<UUID, Session> peers = Collections.synchronizedMap(new HashMap<>());

    private final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("uuid") String uuid) {
        peers.put(UUID.fromString(uuid), session);
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (StatusMessage statusMessage : StatusMessage.values()) {
            JsonObjectBuilder object = Json.createObjectBuilder();
            builder.add(object.add(statusMessage.name(), statusMessage.getMessage()).build());
        }

        session.getAsyncRemote().sendText(builder.build().toString());
    }

    @OnClose
    public void onClose(@PathParam("uuid") String uuid) throws IOException {
        peers.remove(UUID.fromString(uuid));
    }

    /**
     * Listen to status changes and pushes them to the registered sessions
     *
     * @param msg the status message to be send
     * @throws IOException when message could not be serialized to JSON
     */
    public void onEvent(@Observes StatusMessageEvent msg) throws IOException {
        Session session = peers.get(msg.getId());
        if (session != null) {
            session.getAsyncRemote().sendText(objectMapper.writeValueAsString(msg));
        } else {
            log.warning(String.format("no active websocket session found for projectile with ID '%s'", msg.getId()));
        }
    }
}