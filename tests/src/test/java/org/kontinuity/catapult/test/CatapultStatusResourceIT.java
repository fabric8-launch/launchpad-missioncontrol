package org.kontinuity.catapult.test;

import java.net.URI;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kontinuity.catapult.core.api.StatusMessage;
import org.kontinuity.catapult.core.api.StatusMessageEvent;

import static java.util.Collections.singletonMap;

/**
 * Validation of the {@link org.kontinuity.catapult.web.api.CatapultStatusResource}
 */
@RunWith(Arquillian.class)
public class CatapultStatusResourceIT {

    @Inject
    Event<StatusMessageEvent> testEvent;

    @ArquillianResource
    private URI deploymentUrl;

    @Inject
    StatusTestClientEndpoint endpoint;

    @Deployment
    public static WebArchive getDeployment() {
        return Deployments.getMavenBuiltWar().addClass(StatusTestClientEndpoint.class);
    }

    /**
     * Ensures that CDI event is relayed over the webSocket status endpoint.
     *
     * @throws Exception when the test has failed
     */
    @Test
    public void webSocketsStatusTest() throws Exception {
        //given
        UUID uuid = UUID.randomUUID();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = UriBuilder.fromUri(deploymentUrl).scheme("ws").path("status/"+uuid).build();
        Session session = container.connectToServer(endpoint, uri);

        //when
        session.getBasicRemote().sendText(uuid.toString());
        //TODO there must be a way to not do these sleeps
        Thread.sleep(1000);
        testEvent.fire(new StatusMessageEvent(uuid, StatusMessage.GITHUB_CREATE, singletonMap("GitHub project", "http://github.com/dummy-project-location")));
        Thread.sleep(1000);

        //then
        Assert.assertEquals("one message should been received", 1, endpoint.getMessagesReceived().size());
    }

}
