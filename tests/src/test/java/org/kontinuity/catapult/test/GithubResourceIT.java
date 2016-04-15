package org.kontinuity.catapult.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Validation of the GithubResource api endpoint
 * This requires the following environment variables to be configured:
 * GITHUB_USERNAME
 * GITHUB_PASSWORD
 */
@RunWith(Arquillian.class)
public class GithubResourceIT {

    /**
     * Deploy the catapult.ear as built since we only test via the rest endpoints
     * @return
     */
    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "application-ear.ear")
                .as(ZipImporter.class)
                .importFrom(new File("../ear/target/catapult.ear"))
                .as(EnterpriseArchive.class);
        return ear;
    }

    @ArquillianResource
    private URL deploymentUrl;

    @Drone
    WebDriver driver;

    /**
     * Validate that we can fork the jboss-developer/jboss-eap-quickstarts repo
     * @throws IOException
     */
    @Test
    public void should_fork_jboss_eap_quickstarts() throws IOException {
        String forkURL = deploymentUrl.toExternalForm() + "api/github/fork?repo=jboss-developer/jboss-eap-quickstarts";

        System.out.printf("Using driver: %s, calling fork: %s\n", driver, forkURL);
        driver.get(forkURL);
        String html = driver.getPageSource();
        String title = driver.getTitle();

        System.out.printf("Page#1 html: %s\n", html);
        // First need to login user into github
        String username = System.getenv("GITHUB_USERNAME");
        String password = System.getenv("GITHUB_PASSWORD");
        if(username == null || password == null)
            throw new IOException("GITHUB_USERNAME and GITHUB_PASSWORD must be configured for this test");

        WebElement loginField = driver.findElement(By.id("login_field"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement commitBtn = driver.findElement(By.name("commit"));
        loginField.sendKeys(username);
        passwordField.sendKeys(password);
        commitBtn.click();


        html = driver.getPageSource();
        System.out.printf("\nPage#2 html: %s\n", html);

        // See if two-factor auth is enabled
        checkForTwoFactorAuth();

        // See if we need to authorize our github app
        try {
            WebElement authorizeBtn = driver.findElement(By.name("authorize"));
            authorizeBtn.click();
            html = driver.getPageSource();
            System.out.printf("\nPage#3 html: %s\n", html);
        } catch (NoSuchElementException e) {
            System.err.printf("No authorize found on page2, checking for repo fork...\n");
        }

        // Validate that we landed on the users fork of jboss-eap-quickstarts
        title = driver.getTitle();
        String expected = username+"/jboss-eap-quickstarts";
        if(title.startsWith(expected)) {
            System.out.printf("Successfully forked jboss-eap-quickstarts\n");
        } else {
            throw new IllegalStateException("Title, expected: "+expected+", actual: "+title);
        }
    }

    private void checkForTwoFactorAuth() throws IOException {
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