package org.kontinuity.catapult.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

/**
 * Ensures the HTML Console for Catapult is working as expected
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
//TODO Issue #166 disabled this as stopgap
@Ignore
public class CatapultConsoleIT extends CatapultITBase {

    private static final Logger log = Logger.getLogger(CatapultConsoleIT.class.getName());

    private static final String SOURCE_REPO = "redhat-kontinuity/jboss-eap-quickstarts";

    @Deployment(name = "real", testable = false)
    public static WebArchive getRealDeployment() {
        return Deployments.getMavenBuiltWar();
    }

    @Deployment(name = "test")
    public static WebArchive getTestDeployment() {
        return Deployments.getTestDeployment();
    }

    /**
     * Ensures that a fling operation initiated from the HTML console
     * is working as contracted
     *
     * @throws IOException
     */
    @Test
    @RunAsClient
    @InSequence(1)
    @OperateOnDeployment("real")
    public void shouldFlingViaCatapultConsoleButton() throws IOException {

        // Define the request URL
        final String consoleUrl = this.getDeploymentUrl().toExternalForm();
        log.info("Request URL: " + consoleUrl);

        // Execute the Fling URL which should perform all actions
        driver.navigate().to(consoleUrl);

        final File scrFile1 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile1,
                           new File(
                                   "target/" +
                                           this.getClass().getSimpleName() +
                                           "-1-consoleBeforeSubmission.png"));

        // Fill out the form and submit
        final WebElement select = driver.findElement(By.id("flingSourceRepo"));
        final List<WebElement> options = select.findElements(By.tagName("option"));
        for (final WebElement option : options) {
            if (option.getAttribute("value").equals(this.getSourceRepo())) {
                option.click();
                break;
            }
        }

        final File scrFile2 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile2,
                           new File(
                                   "target/" +
                                           this.getClass().getSimpleName() +
                                           "-2-consoleSelectedBeforeSubmission.png"));

        final WebElement submit = driver.findElement(By.id("flingSubmitButton"));
        submit.click();

        // Ensure we end up in the right place
        final File scrFile3 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile3,
                           new File(
                                   "target/" +
                                           this.getClass().getSimpleName() +
                                           "-3-consoleAfterSubmission.png"));
        this.assertLanding(driver);
    }

    @Override
    String getSourceRepo() {
        return SOURCE_REPO;
    }
}
