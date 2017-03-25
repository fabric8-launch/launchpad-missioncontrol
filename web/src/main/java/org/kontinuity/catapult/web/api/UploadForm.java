package org.kontinuity.catapult.web.api;

import java.io.InputStream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class UploadForm {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @NotNull
    private InputStream file;

    @FormParam("gitHubRepositoryDescription")
    @PartType(MediaType.APPLICATION_FORM_URLENCODED)
    private String gitHubRepositoryDescription;

    public String getGitHubRepositoryDescription() {
        return gitHubRepositoryDescription;
    }

    public void setGitHubRepositoryDescription(String gitHubRepositoryDescription) {
        this.gitHubRepositoryDescription = gitHubRepositoryDescription;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }
}
