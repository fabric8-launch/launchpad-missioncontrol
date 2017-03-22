package org.kontinuity.catapult.test;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.google.common.io.Files;
import org.glassfish.tyrus.client.ClientManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.Resolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kontinuity.catapult.base.identity.Identity;
import org.kontinuity.catapult.base.identity.IdentityFactory;
import org.kontinuity.catapult.core.api.CreateProjectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;
import org.kontinuity.catapult.core.api.StatusMessage;
import org.kontinuity.catapult.core.api.StatusMessageEvent;
import org.kontinuity.catapult.service.github.test.GitHubTestCredentials;
import org.kontinuity.catapult.web.api.CatapultStatusResource;

/**
 * Validation of the {@link org.kontinuity.catapult.web.api.CatapultResource}
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public class CatapultStatusResourceIT {

    private static final Logger log = Logger.getLogger(CatapultStatusResourceIT.class.getName());

    @Inject
    Event<StatusMessageEvent> testEvent;

//    @ArquillianResource
//    private URL deploymentUrl;

    @Deployment
    public static WebArchive getTestDeployment() {
//        final File[] ourTestDeps = Resolvers.use(MavenResolverSystem.class)
//                .resolve("org.java-websocket:tyrus-client:1.3.1")
//                .withTransitivity()
//                .asFile();

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackages(true, StatusMessageEvent.class.getPackage())
                .addClass(Identity.class)
                .addClass(StatusTestClientEndpoint.class)
                .addClass(CatapultStatusResource.class);
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

        //URI uri = new URI("ws://" + deploymentUrl.getHost() + ":" + deploymentUrl.getPort() + "/status");
        URI uri = new URI("ws://localhost:8080/status");

        StatusTestClientEndpoint endpoint = new StatusTestClientEndpoint();
        Session session = container.connectToServer(endpoint, uri);

        final CreateProjectile projectile = ProjectileBuilder.newInstance()
                .gitHubIdentity(GitHubTestCredentials.getToken())
                .openShiftIdentity(IdentityFactory.createFromToken("dummy-token"))
                .openShiftProjectName("projectName")
                .createType()
                .projectLocation(Files.createTempDir().toPath())
                .build();

        final HashMap<String, Object> data = new HashMap<>();
        data.put("GitHub project", "http://github.com/dummy-project-location");


        //when
        testEvent.fire(new StatusMessageEvent(projectile, StatusMessage.GITHUB_CREATE, data));
        session.getBasicRemote().sendText(projectile.getId().toString());

        //then
        Assert.assertEquals("one message should been received", 1, endpoint.getMessagesReceived().size());
    }

}
