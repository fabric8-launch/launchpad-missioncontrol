package io.openshift.appdev.missioncontrol.service.openshift.impl.fabric8.openshift.client;

import java.net.URI;

import io.openshift.appdev.missioncontrol.base.identity.IdentityFactory;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class Teste {
    public static void main(String[] args) throws Exception {
        Fabric8OpenShiftServiceImpl openShiftService = new Fabric8OpenShiftServiceImpl("https://192.168.42.94:8443/", "https://192.168.42.94:8443/console/", IdentityFactory.createFromUserPassword("developer", "developer"));
        OpenShiftProject createdProject = openShiftService.createProject("foo");
        openShiftService.configureProject(createdProject, new URI("https://github.com/gastaldi/vertx-http-booster.git"));
        System.out.println("DELETE: " + openShiftService.deleteProject("foo"));
    }
}
