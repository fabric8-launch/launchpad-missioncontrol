package org.kontinuity.catapult.test;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
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
import org.kontinuity.catapult.base.identity.IdentityFactory;
import org.kontinuity.catapult.core.api.CreateProjectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;
import org.kontinuity.catapult.core.api.StatusMessage;
import org.kontinuity.catapult.core.api.StatusMessageEvent;

/**
 * Validation of the {@link org.kontinuity.catapult.web.api.CatapultResource}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public class CatapultStatusResourceIT {

    private static final Logger log = Logger.getLogger(CatapultStatusResourceIT.class.getName());

    @Inject
    BeanManager manager;

    @ArquillianResource
    private URI deploymentUrl;

    @Deployment
    public static WebArchive getDeployment() {
        return Deployments.getMavenBuiltWar().addClass(StatusTestClientEndpoint.class);
    }

    /**
     * Ensures that CDI event is relayed over the websocket.
     *
     * @throws Exception when the test has failed
     */
    @Test
    public void websocketsStatusTest() throws Exception {
        //given
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = UriBuilder.fromUri(deploymentUrl).scheme("ws").path("status").build();

        StatusTestClientEndpoint endpoint = new StatusTestClientEndpoint();
        Session session = container.connectToServer(endpoint, uri);

        final CreateProjectile projectile = ProjectileBuilder.newInstance()
                .gitHubIdentity(IdentityFactory.createFromToken("dummy-token"))
                .openShiftIdentity(IdentityFactory.createFromToken("dummy-token"))
                .openShiftProjectName("projectName")
                .createType()
                .projectLocation(Files.createTempDirectory("dummy"))
                .build();

        final HashMap<String, Object> data = new HashMap<>();
        data.put("GitHub project", "http://github.com/dummy-project-location");

        //when
        manager.fireEvent(new StatusMessageEvent(projectile, StatusMessage.GITHUB_CREATE, data));
//        testEvent.fire(new StatusMessageEvent(projectile, StatusMessage.GITHUB_CREATE, data));
        session.getBasicRemote().sendText(projectile.getId().toString());

        //then
        Assert.assertEquals("one message should been received", 1, endpoint.getMessagesReceived().size());
    }

}
