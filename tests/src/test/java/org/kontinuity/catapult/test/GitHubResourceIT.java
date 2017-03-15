package org.kontinuity.catapult.test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.swing.*;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.http.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kontinuity.catapult.service.github.test.GitHubTestCredentials;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Validation of the GithubResource api endpoint
 * This requires the following environment variables to be configured:
 * GITHUB_USERNAME
 * GITHUB_PASSWORD
 */
@RunWith(Arquillian.class)
public class GitHubResourceIT {

    private static final Logger log = Logger.getLogger(GitHubResourceIT.class.getName());

    /*
     Contracts (define here; do NOT link back to where these are defined in runtime code;
     if the runtime code changes that's a contract break)
     */
    private static final String PATH_VERIFY = "api/github/verify";

    private static final String PATH_AUTHORIZE = "api/github/authorize";

    private static final String PATH_AUTHORIZE_WITH_REDIRECT = PATH_AUTHORIZE + "?redirect_url=/github/verify";

    private static final String PATH_CALLBACK = "api/github/callback?code=c7c4d8631701b80b7759&state=eyJ1dWlkIjoiMzI0Y2I0ODMtODEzOS00NDk2LTg2OWMtOGI2MWFlODIxN2FiIiwicmVkaXJlY3RfdXJsIjoiL2dpdGh1Yi92ZXJpZnkifQ%3D%3D";

    private static final String MIME_TYPE_JSON = "application/json";

    private static final String JSON_KEY_SESSION_HAS_GH_ACCESS_TOKEN = "session_has_github_access_token";

    @ArquillianResource
    private URL deploymentUrl;

    private WebDriver driver = WebDriverProviderHack.getWebDriver();

    /*
    We cannot inject the WebDriver from Drone because of:
    https://github.com/ariya/phantomjs/issues/13114#issuecomment-215074924
    ...we need an older version before 2.x
    @Drone
    private WebDriver driver;
    */

    /**
     * Deploy the catapult.war as built since we only test via the API endpoints
     *
     * @return
     */
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.getMavenBuiltWar();
    }

    @After
    public void closeWebDriver() {
        driver.close();
        driver.quit();
    }

    @Test
    public void shouldNotHaveTokenAssociatedToStart() throws IOException {
        final String verifyUrl = deploymentUrl.toExternalForm() + PATH_VERIFY;
        log.info("Request URL: " + verifyUrl);

        final WebClient webClient = new WebClient();
        final Page page = webClient.getPage(verifyUrl);
        final WebResponse response = page.getWebResponse();
        final String responseString = response.getContentAsString();
        log.info(verifyUrl + ":\n" + responseString);
        final int statusCode = response.getStatusCode();
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);
        Assert.assertEquals(MIME_TYPE_JSON, response.getContentType());
        final JsonParser jsonParser = Json.createParser(new StringReader(responseString));
        jsonParser.next();
        jsonParser.next();
        final String actualKey = jsonParser.getString();
        final JsonParser.Event actualValue = jsonParser.next();
        Assert.assertEquals(JSON_KEY_SESSION_HAS_GH_ACCESS_TOKEN, actualKey);
        Assert.assertEquals(JsonParser.Event.VALUE_FALSE, actualValue);
    }

    @Test
    public void shouldHaveTokenAssociatedAfterOAuth() throws IOException {
        // Let's try to auth, redirecting back to the verification page when done
        final String authUrl = deploymentUrl.toExternalForm() + PATH_AUTHORIZE_WITH_REDIRECT;
        log.info("Starting URL: " + authUrl);

        driver.navigate().to(authUrl);
        performGitHubOAuth(driver);

        // Validate that we landed on the verification page, and that we've got a
        // GitHub access token in session
        final String finalContent = driver.getPageSource();
        String expected = "{\"session_has_github_access_token\":true}";
        log.info("Response from verification after OAuth: " + finalContent);
        // Assert on contains because phantomjs will wrap in HTML tags
        Assert.assertTrue(finalContent.contains(expected));
    }

    /**
     * Ensures that the redirect_url query param is specified in the "authorize"
     * endpoint
     *
     * @throws IOException
     */
    @Test
    public void redirectForAuthorizeIsRequired() throws IOException {
        // Try to auth but don't pass a redirect URL as a query param
        final String authUrl = deploymentUrl.toExternalForm() + PATH_AUTHORIZE;
        TestSupport.assertHttpClientErrorStatus(
                authUrl,
                400,
                "Was expecting an HTTP status code of 400");
    }

    /**
     * Ensures that the callback endpoint can not be invoked by just any caller;
     * the state UUID must be initiated from our own "authorize" endpoint, else
     * return HTTP status 401/Unauthorized
     *
     * @throws IOException
     */
    @Test
    public void noManInTheMiddle() throws IOException {
        // Try to invoke the callback with a BS state
        final String callbackUrl = deploymentUrl.toExternalForm() + PATH_CALLBACK;
        TestSupport.assertHttpClientErrorStatus(
                callbackUrl,
                401,
                "Was expecting an HTTP status code of 401/Unauthorized");
    }

    static void performGitHubOAuth(final WebDriver driver) throws IOException {
        assert driver != null : "driver must be specified";

        log.info("VERSION: " + ((RemoteWebDriver) driver).getCapabilities().getBrowserName() + " - " + ((RemoteWebDriver) driver).getCapabilities().getVersion());

        String html = driver.getPageSource();

        log.info("Page1 URL:" + driver.getCurrentUrl());
        if (log.isLoggable(Level.FINEST)) {
            log.finest(MessageFormat.format("Page#1 html: {0}\n", html));
        }
        log.info("Current URL: " + driver.getCurrentUrl());

        // First need to login user into github
        final String username = GitHubTestCredentials.getUsername();
        final String password = GitHubTestCredentials.getPassword();
        if (username == null || username.isEmpty()
                || password == null || password.isEmpty()) {
            throw new IllegalStateException(
                    "GITHUB_USERNAME and GITHUB_PASSWORD must be configured as sysprops or env vars for this test");
        }

        final WebElement loginField = driver.findElement(By.id("login_field"));
        final WebElement passwordField = driver.findElement(By.id("password"));
        final WebElement commitBtn = driver.findElement(By.name("commit"));
        loginField.sendKeys(username);
        passwordField.sendKeys(password);
        commitBtn.click();

        final String page2Url = driver.getCurrentUrl();
        log.info("Page2 URL: " + page2Url);
        String html2 = driver.getPageSource();
        if (log.isLoggable(Level.FINEST)) {
            log.finest(MessageFormat.format("Page#2 html: {0}\n", html2));
        }

        // See if we need to authorize our github app
        try {
            final WebElement authorizeBtn = driver.findElement(By.name("authorize"));
            authorizeBtn.click();
            final String html3 = driver.getPageSource();
            if (log.isLoggable(Level.FINEST)) {
                log.finest(MessageFormat.format("Page#3 html: {0}\n", html3));
            }
        } catch (NoSuchElementException e) {
            System.err.printf("No authorize button found on page2\n");
        }
    }

    private static void checkForTwoFactorAuth(final WebDriver driver) throws IOException {

        assert driver != null : "driver must be specified";

        /* See if two factor auth is enabled by looking for the one time password field. The page does not name the form
        or submit button for the otp code, so we have to find it by locating the form with an action="/sessions/two-factor"
        */
        try {
            WebElement otp = driver.findElement(By.id("otp"));
            WebElement submitBtn = driver.findElement(By.xpath("//button[@type='submit']"));
            // Yes, prompt for the otp
            String code = JOptionPane.showInputDialog(
                    null, "Enter your two-factor one time password/code: ",
                    "two-factor code",
                    JOptionPane.PLAIN_MESSAGE);
            otp.sendKeys(code);
            submitBtn.click();
        } catch (NoSuchElementException e) {
            // No, go on to verifying the page is the expected jboss-eap-quickstarts fork
            System.out.printf("No otp field indicating two-factor auth enabled\n");
        }
    }
}