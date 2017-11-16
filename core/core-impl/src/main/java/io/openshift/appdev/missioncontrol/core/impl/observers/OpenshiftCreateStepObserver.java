package io.openshift.appdev.missioncontrol.core.impl.observers;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;
import io.openshift.appdev.missioncontrol.core.api.Step;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;

import static io.openshift.appdev.missioncontrol.core.api.StatusEventType.OPENSHIFT_CREATE;
import static java.util.Collections.singletonMap;

/**
 * Creates an Openshift project if the project doesn't exist.
 */
@ApplicationScoped
public class OpenshiftCreateStepObserver {

    private final OpenShiftServiceFactory openShiftServiceFactory;

    private final OpenShiftClusterRegistry openShiftClusterRegistry;

    private final Event<StatusMessageEvent> statusEvent;

    @Inject
    public OpenshiftCreateStepObserver(OpenShiftServiceFactory openShiftServiceFactory,
                                       OpenShiftClusterRegistry openShiftClusterRegistry, Event<StatusMessageEvent> statusEvent) {
        this.statusEvent = statusEvent;
        this.openShiftServiceFactory = openShiftServiceFactory;
        this.openShiftClusterRegistry = openShiftClusterRegistry;
    }

    public void execute(@Observes @Step(OPENSHIFT_CREATE)CreateProjectile projectile) {
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());
        String projectName = projectile.getOpenShiftProjectName();
        OpenShiftProject openShiftProject = openShiftService.findProject(projectName).orElseGet(() -> openShiftService.createProject(projectName));
        statusEvent.fire(new StatusMessageEvent(projectile.getId(), OPENSHIFT_CREATE,
                                                singletonMap("location", openShiftProject.getConsoleOverviewUrl())));
    }
}
