package io.openshift.appdev.missioncontrol.web.api;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.openshift.appdev.missioncontrol.base.identity.Identity;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubService;
import io.openshift.appdev.missioncontrol.service.github.api.GitHubServiceFactory;
import io.openshift.appdev.missioncontrol.service.keycloak.api.KeycloakService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Path(ValidationResource.PATH_RESOURCE)
@ApplicationScoped
public class ValidationResource extends AbstractResource {

    /**
     * Paths
     **/
    static final String PATH_RESOURCE = "/validate";

    @Inject
    private Instance<KeycloakService> keycloakServiceInstance;

    @Inject
    private GitHubServiceFactory gitHubServiceFactory;

    @Inject
    private OpenShiftServiceFactory openShiftServiceFactory;


    @HEAD
    @Path("/repository/{repo}")
    public Response repositoryExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                     @NotNull @PathParam("repo") String repository) {
        Identity githubIdentity;
        if (useDefaultIdentities()) {
            githubIdentity = getDefaultGithubIdentity();
        } else {
            KeycloakService keycloakService = this.keycloakServiceInstance.get();
            githubIdentity = keycloakService.getGitHubIdentity(authorization);
        }
        GitHubService gitHubService = gitHubServiceFactory.create(githubIdentity);
        if (gitHubService.repositoryExists(gitHubService.getLoggedUser().getLogin() + "/" + repository)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/project/{project}")
    public Response projectExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                  @NotNull @PathParam("project") String project) {
        Identity openShiftIdentity;
        if (useDefaultIdentities()) {
            openShiftIdentity = getDefaultOpenShiftIdentity();
        } else {
            KeycloakService keycloakService = this.keycloakServiceInstance.get();
            openShiftIdentity = keycloakService.getOpenShiftIdentity(authorization);
        }

        OpenShiftService openShiftService = openShiftServiceFactory.create(openShiftIdentity);
        if (openShiftService.projectExists(project)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/token/openshift")
    public Response openShiftTokenExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization,
                                         @QueryParam("cluster") String cluster) {
        boolean tokenExists;
        try {
            if (useDefaultIdentities()) {
                tokenExists = getDefaultOpenShiftIdentity() != null;
            } else {
                KeycloakService keycloakService = this.keycloakServiceInstance.get();
                if (cluster == null) {
                    tokenExists = keycloakService.getOpenShiftIdentity(authorization) != null;
                } else {
                    tokenExists = keycloakService.getIdentity(cluster, authorization).isPresent();
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            tokenExists = false;
        }
        if (tokenExists) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("/token/github")
    public Response gitHubTokenExists(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {
        Identity githubIdentity;
        try {
            if (useDefaultIdentities()) {
                githubIdentity = getDefaultGithubIdentity();
            } else {
                KeycloakService keycloakService = this.keycloakServiceInstance.get();
                githubIdentity = keycloakService.getGitHubIdentity(authorization);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            githubIdentity = null;
        }
        if (githubIdentity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok().build();
        }
    }
}