package io.openshift.appdev.missioncontrol.core.impl.commands;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.StatusMessage;
import io.openshift.appdev.missioncontrol.core.api.StatusMessageEvent;
import io.openshift.appdev.missioncontrol.core.api.commands.AbstractCommand;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftProject;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftService;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftServiceFactory;

/**
 * Creates an Openshift project if the project doesn't exist.
 */
@ApplicationScoped
public class OpenshiftCreateCommand extends AbstractCommand {

    private final OpenShiftServiceFactory openShiftServiceFactory;

    private final OpenShiftClusterRegistry openShiftClusterRegistry;

    @Inject
    public OpenshiftCreateCommand(OpenShiftServiceFactory openShiftServiceFactory,
                                  OpenShiftClusterRegistry openShiftClusterRegistry, Event<StatusMessageEvent> statusEvent) {
        super(statusEvent);
        this.openShiftServiceFactory = openShiftServiceFactory;
        this.openShiftClusterRegistry = openShiftClusterRegistry;
    }

    @Override
    public StatusMessage getStatusMessage() {
        return StatusMessage.OPENSHIFT_CREATE;
    }

    @Override
    public void execute(CreateProjectile projectile) {
        Optional<OpenShiftCluster> cluster = openShiftClusterRegistry.findClusterById(projectile.getOpenShiftClusterName());
        OpenShiftService openShiftService = openShiftServiceFactory.create(cluster.get(), projectile.getOpenShiftIdentity());
        String projectName = projectile.getOpenShiftProjectName();
        OpenShiftProject openShiftProject = openShiftService.findProject(projectName).orElseGet(() -> openShiftService.createProject(projectName));
        fireEvent(projectile.getId(), openShiftProject.getConsoleOverviewUrl());
    }
}
