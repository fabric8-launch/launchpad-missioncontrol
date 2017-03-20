package org.kontinuity.catapult.service.keycloak.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.kontinuity.catapult.base.EnvironmentSupport;
import org.kontinuity.catapult.service.keycloak.api.KeycloakService;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class KeycloakServiceProducer {

    @Produces
    @ApplicationScoped
    public KeycloakService createService() {
        String user = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("CATAPULT_OPENSHIFT_USERNAME");
        String password = EnvironmentSupport.INSTANCE.getEnvVarOrSysProp("CATAPULT_OPENSHIFT_PASSWORD");
        if (user != null && password != null) {
            return new KeycloakServiceMock();
        }
        return new KeycloakServiceImpl();
    }
}
