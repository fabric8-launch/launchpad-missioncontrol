package org.rhd.katapult.test.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Validation of the GithubResource api endpoint
 */
@RunWith(Arquillian.class)
public class GithubResourceTest {

    /**
     * Deploy the katapult.ear as built since we only test via the rest endpoints
     * @return
     */
    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "application-ear.ear")
                .as(ZipImporter.class)
                .importFrom(new File("../ear/target/katapult.ear"))
                .as(EnterpriseArchive.class);
        return ear;
    }

    @ArquillianResource
    private URL deploymentUrl;

    /**
     * Validate that we can fork the jboss-developer/jboss-eap-quickstarts repo
     * @throws IOException
     */
    @Test
    public void should_fork_jboss_eap_quickstarts() throws IOException {
        String forkURL = deploymentUrl.toExternalForm() + "api/github/fork?repo=jboss-developer/jboss-eap-quickstarts";
        final WebClient webClient = new WebClient();

        System.out.printf("Calling for: %s\n", forkURL);
        HtmlPage page = webClient.getPage(forkURL);
        WebResponse response = page.getWebResponse();
        int statusCode = response.getStatusCode();
        String html = page.asXml();
        System.out.printf("Page#1 html: %s\n", html);
        // First need to login user into github
        String username = System.getenv("GITHUB_USERNAME");
        String password = System.getenv("GITHUB_PASSWORD");
        HtmlInput loginField = page.getElementById("login_field", false);
        HtmlInput passwordField = page.getElementById("password", false);
        HtmlInput commitBtn = page.getElementByName("commit");
        loginField.setValueAttribute(username);
        passwordField.setValueAttribute(password);
        HtmlPage page2 = commitBtn.click();
        List<NameValuePair> postParams = page2.getWebResponse().getWebRequest().getRequestParameters();
        html = page2.asXml();
        System.out.printf("\nPage#2(submit=%s; status=%d) html: %s\n", postParams, page2.getWebResponse().getStatusCode(), html);

        // See if we need to authorize our github app
        try {
            HtmlButton authorizeBtn = page2.getElementByName("authorize");
            HtmlPage nextPage = authorizeBtn.click();
            html = nextPage.asXml();
            System.out.printf("\nPage#3(submit=%s; status=%d) html: %s\n", postParams, nextPage.getWebResponse().getStatusCode(), html);
            page2 = nextPage;
        } catch (ElementNotFoundException e) {
            System.err.printf("No authorize found on page2, checking for repo fork...\n");
        }

        // Validate that we landed on the users fork of jboss-eap-quickstarts
        String title = page2.getTitleText();
        String expected = username+"/jboss-eap-quickstarts";
        if(title.startsWith(expected)) {
            System.out.printf("Successfully forked jboss-eap-quickstarts\n");
        } else {
            throw new IllegalStateException("Title, expected: "+expected+", actual: "+title);
        }
    }
}