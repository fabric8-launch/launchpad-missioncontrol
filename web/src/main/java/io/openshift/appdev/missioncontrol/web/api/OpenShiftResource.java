package io.openshift.appdev.missioncontrol.web.api;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import io.openshift.appdev.missioncontrol.service.keycloak.api.KeycloakService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path(OpenShiftResource.PATH_RESOURCE)
@ApplicationScoped
public class OpenShiftResource extends AbstractResource {

    static final String PATH_RESOURCE = "/openshift";

    @Inject
    OpenShiftClusterRegistry clusterRegistry;

    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray getSupportedOpenShiftClusters(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Set<OpenShiftCluster> clusters = clusterRegistry.getClusters();
        if (useDefaultIdentities()) {
            // Return all identities
            clusters
                    .stream()
                    .map(OpenShiftCluster::getId)
                    .forEach(arrayBuilder::add);
        } else {
            final KeycloakService keycloakService = this.keycloakServiceInstance.get();
            clusters.stream().map(OpenShiftCluster::getId)
                    .forEach(clusterId ->
                                     keycloakService.getIdentity(clusterId, authorization)
                                             .ifPresent(token -> arrayBuilder.add(clusterId)));
        }

        return arrayBuilder.build();
    }
}