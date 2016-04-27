package org.kontinuity.catapult.service.openshift.impl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kontinuity.catapult.service.openshift.api.OpenShiftProject;
import org.kontinuity.catapult.service.openshift.api.OpenShiftSettings;

import java.net.MalformedURLException;
import java.util.logging.Logger;

/**
 * Test cases to ensure the {@link OpenShiftProjectImpl} is working as contracted
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class OpenShiftProjectImplTest {

   private static final Logger log = Logger.getLogger(OpenShiftProjectImplTest.class.getName());
   private static final String PROJECT_NAME = "test-name";

   private static OpenShiftProject project;

   @BeforeClass
   public static void initProject() {
      project = new OpenShiftProjectImpl(PROJECT_NAME);
   }

   @Test(expected = IllegalArgumentException.class)
   public void nameCannotBeNull() {
      new OpenShiftProjectImpl(null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void nameCannotBeEmpty() {
      new OpenShiftProjectImpl("");
   }

   @Test
   public void name() {
      Assert.assertEquals(PROJECT_NAME, project.getName());
   }

   @Test
   public void consoleOverviewUrl() throws MalformedURLException {
      final String expectedUrl = OpenShiftSettings.getOpenShiftUrl() +
              "/console/project/" +
              PROJECT_NAME +
              "/overview/";
      log.info("Expected Console Overview URL: " + expectedUrl);
      final String actualUrl = project.getConsoleOverviewUrl().toExternalForm();
      log.info("Actual Console Overview URL: " + actualUrl);
      Assert.assertEquals(expectedUrl, actualUrl);
   }

}
