package org.kontinuity.catapult.test;

import java.io.IOException;

import org.jboss.arquillian.phantom.resolver.ResolvingPhantomJSDriverService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * This is a hack in place because newer versions of phantomjs (>2.x)
 * as well as version <1.9.7 have critical bugs that keep us from being
 * able to test.  So we don't rely on Drone to do injection of the WebDriver
 * in our tests, but rather we explicitly tell which version to resolve and use.
 * <p>
 * https://github.com/ariya/phantomjs/issues/13114#issuecomment-215074924
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class WebDriverProviderHack {

    /**
     * No instances
     */
    private WebDriverProviderHack() {
    }

    private static final DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();

    static {
        // enforce resolver to use given phantomjs version
        capabilities.setCapability("phantomjs.binary.version", "1.9.8");
        // And set up security to allow SSL without a certificate (as provided by OpenShift)
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                                   new String[]{"--web-security=no", "--ignore-ssl-errors=yes"});
    }

    /**
     * Obtains the {@link WebDriver} to be used in testing
     *
     * @return
     */
    static WebDriver getWebDriver() {
        try {
            return new PhantomJSDriver(
                    ResolvingPhantomJSDriverService.createDefaultService(capabilities),
                    capabilities);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
