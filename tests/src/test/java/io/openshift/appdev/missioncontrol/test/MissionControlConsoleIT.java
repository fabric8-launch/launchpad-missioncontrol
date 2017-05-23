package io.openshift.appdev.missioncontrol.test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Ensures the HTML Console for MissionControl is working as expected
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public class MissionControlConsoleIT extends MissionControlITBase {

    private static final Logger log = Logger.getLogger(MissionControlConsoleIT.class.getName());

    private static final String PROJECT_NAME = "demo";

    @Deployment(name = "real", testable = false)
    public static WebArchive getRealDeployment() {
        return Deployments.getMavenBuiltWar();
    }

    @Deployment(name = "test")
    public static WebArchive getTestDeployment() {
        return Deployments.getTestDeployment();
    }

    /**
     * Ensures that a launch operation initiated from the HTML console
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
        String path = new File("./src/test/resources/demo.zip").getAbsolutePath();
        final WebElement file = driver.findElement(By.id("file"));
        file.sendKeys(path);

        final String projectName = getProjectName();
        final WebElement openShiftProjectName = driver.findElement(By.id("openShiftProjectName"));
        openShiftProjectName.sendKeys(projectName);

        final WebElement gitHubRepositoryName = driver.findElement(By.id("gitHubRepositoryName"));
        gitHubRepositoryName.sendKeys(projectName);

        final WebElement gitHubRepositoryDescription = driver.findElement(By.id("gitHubRepositoryDescription"));
        gitHubRepositoryDescription.sendKeys("created by integration test");

        final File scrFile2 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile2,
                           new File(
                                   "target/" +
                                           this.getClass().getSimpleName() +
                                           "-2-consoleSelectedBeforeSubmission.png"));

        final WebElement submit = driver.findElement(By.id("flingSubmitButton"));
        submit.click();

        try {
            WebElement element = (new WebDriverWait(driver, 20)).until(ExpectedConditions.presenceOfElementLocated(By.id("process-done")));
            assertNotNull(element);
            assertTrue("all steps should have been done", element.isDisplayed());
        } finally {
            // Ensure we end up in the right place
            final File scrFile3 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile3,
                               new File(
                                       "target/" +
                                               this.getClass().getSimpleName() +
                                               "-3-consoleAfterSubmission.png"));
        }
    }

    @Override
    String getProjectName() {
        return PROJECT_NAME;
    }
}
