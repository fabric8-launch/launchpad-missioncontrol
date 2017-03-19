package org.kontinuity.catapult.web.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.kontinuity.catapult.base.identity.IdentityFactory;
import org.kontinuity.catapult.core.api.Boom;
import org.kontinuity.catapult.core.api.Catapult;
import org.kontinuity.catapult.core.api.CreateProjectile;
import org.kontinuity.catapult.core.api.ForkProjectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;

/**
 * Endpoint exposing the {@link org.kontinuity.catapult.core.api.Catapult} over HTTP
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@Path(CatapultResource.PATH_CATAPULT)
@ApplicationScoped
public class CatapultResource {

    /*
     Paths
     */
    static final String PATH_CATAPULT = "/catapult";

    static final String UTF_8 = "UTF-8";

    private static final String PATH_FLING = "/fling";

    private static final String PATH_UPLOAD = "/upload";

    /*
     Catapult Query Parameters
     */
    private static final String QUERY_PARAM_SOURCE_REPO = "sourceRepo";

    private static final String QUERY_PARAM_GIT_REF = "gitRef";

    private static final String QUERY_PARAM_PIPELINE_TEMPLATE_PATH = "pipelineTemplatePath";

    private static final String QUERY_PARAM_PROJECT_LOCATION = "projectLocation";

    private static Logger log = Logger.getLogger(CatapultResource.class.getName());

    @Inject
    private Catapult catapult;

    @GET
    @Path(PATH_FLING)
    public Response fling(
            @Context final HttpServletRequest request,
            @NotNull @QueryParam(QUERY_PARAM_SOURCE_REPO) final String sourceGitHubRepo,
            @NotNull @QueryParam(QUERY_PARAM_GIT_REF) final String gitRef,
            @NotNull @QueryParam(QUERY_PARAM_PIPELINE_TEMPLATE_PATH) final String pipelineTemplatePath) {
        String gitHubAccessToken = getGitHubAccessToken(request);
        if (gitHubAccessToken == null) {
            return createForkRedirectUrl(sourceGitHubRepo, gitRef, pipelineTemplatePath);
        }

        ForkProjectile projectile = ProjectileBuilder.newInstance()
                .gitHubIdentity(IdentityFactory.usingToken(gitHubAccessToken))
                .forkType()
                .sourceGitHubRepo(sourceGitHubRepo)
                .gitRef(gitRef)
                .pipelineTemplatePath(pipelineTemplatePath)
                .build();

        // Fling it
        Boom boom = catapult.fling(projectile);
        return processBoom(boom);
    }

    @POST
    @Path(PATH_UPLOAD)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(
            @Context final HttpServletRequest request,
            MultipartFormDataInput uploaded) {
        InputPart inputPart = uploaded.getFormDataMap().get("file").get(0);
        final String fileName = FileUploadHelper.getFileName(inputPart.getHeaders());

        try (InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
            final java.nio.file.Path tempFile = Files.createTempDirectory(fileName);
            final File zipFileName = new File(tempFile.toFile(), fileName);
            try (FileOutputStream output = new FileOutputStream(zipFileName)) {
                IOUtils.write(IOUtils.toByteArray(inputStream), output);
                FileUploadHelper.unzip(zipFileName);

                String path = new File(tempFile.toFile(), FilenameUtils.getBaseName(fileName)).getPath();
                String gitHubAccessToken = getGitHubAccessToken(request);
                if (gitHubAccessToken == null) {
                    return createCreateRedirectUrl(path);
                }
                CreateProjectile projectile = ProjectileBuilder.newInstance()
                        .gitHubIdentity(IdentityFactory.usingToken(gitHubAccessToken))
                        .createType()
                        .projectLocation(path)
                        .build();
                Boom boom = catapult.fling(projectile);
                return processBoom(boom);
            }
        } catch (final IOException e) {
            throw new WebApplicationException("could not unpack zip file into temp folder", e);
        }
    }

    @GET
    @Path(PATH_UPLOAD)
    public Response uploadRedirect(@Context final HttpServletRequest request,
                                   @QueryParam(QUERY_PARAM_PROJECT_LOCATION) final String projectLocation) {
        // came back from GitHub oath already unpacked the zip file just fling it.
        String gitHubAccessToken = getGitHubAccessToken(request);
        if (gitHubAccessToken == null) {
            return createCreateRedirectUrl(projectLocation);
        }
        CreateProjectile projectile = ProjectileBuilder.newInstance()
                .gitHubIdentity(IdentityFactory.usingToken(gitHubAccessToken))
                .createType()
                .projectLocation(projectLocation)
                .build();

        Boom boom = catapult.fling(projectile);
        return processBoom(boom);
    }

    /**
     * Encode the redirect uri and add it to the GitHub path
     *
     * @param redirectAfterOAuthPath the url to redirect to
     * @return a complete uri with the url encoded
     */
    private URI toUri(String redirectAfterOAuthPath) {
        final String urlEncodedRedirectAfterOauthPath;
        try {
            urlEncodedRedirectAfterOauthPath = URLEncoder.encode(redirectAfterOAuthPath, UTF_8);
        } catch (final UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
        // Create the full path
        return UriBuilder.fromPath(GitHubResource.PATH_GITHUB + GitHubResource.PATH_AUTHORIZE)
                .queryParam(GitHubResource.QUERY_PARAM_REDIRECT_URL, urlEncodedRedirectAfterOauthPath)
                .build();
    }

    /**
     * Create a redirect response for the fork "fling"
     *
     * @param sourceRepo           source repo to fork
     * @param gitRef               git ref to use from the fork
     * @param pipelineTemplatePath path of the pipeline template with in the project
     * @return the temporary redirect for GitHub oauth
     */
    private Response createForkRedirectUrl(String sourceRepo, String gitRef, String pipelineTemplatePath) {
        String redirectAfterOAuthPath = UriBuilder.fromPath(PATH_CATAPULT + PATH_FLING)
                .queryParam(QUERY_PARAM_SOURCE_REPO, sourceRepo)
                .queryParam(QUERY_PARAM_GIT_REF, gitRef)
                .queryParam(QUERY_PARAM_PIPELINE_TEMPLATE_PATH, pipelineTemplatePath)
                .build().toString();

        return Response.temporaryRedirect(toUri(redirectAfterOAuthPath)).build();
    }

    /**
     * Create a redirect response for the create "fling"
     *
     * @param projectLocation the location of the extracted project zip
     * @return the temporary redirect for GitHub oauth
     */
    private Response createCreateRedirectUrl(String projectLocation) {
        String redirectAfterOAuthPath = UriBuilder.fromPath(PATH_CATAPULT + PATH_UPLOAD)
                .queryParam(QUERY_PARAM_PROJECT_LOCATION, projectLocation)
                .build().toString();
        return Response.temporaryRedirect(toUri(redirectAfterOAuthPath)).build();
    }

    private String getGitHubAccessToken(HttpServletRequest request) {
        return (String) request
                .getSession().getAttribute(GitHubResource.SESSION_ATTRIBUTE_GITHUB_ACCESS_TOKEN);
    }

    private Response processBoom(Boom boom) {
        // Redirect to the console overview page
        final URI consoleOverviewUri;
        try {
            consoleOverviewUri = boom.getCreatedProject().getConsoleOverviewUrl().toURI();
            if (log.isLoggable(Level.FINEST)) {
                log.finest("Redirect issued to: " + consoleOverviewUri.toString());
            }
        } catch (final URISyntaxException urise) {
            throw new WebApplicationException("couldn't get console location do you have the environment variable set", urise);
        }
        return Response.temporaryRedirect(consoleOverviewUri).build();
    }
}