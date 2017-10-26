package io.openshift.appdev.missioncontrol.core.api.commands;

import java.util.Map;

import io.openshift.appdev.missioncontrol.core.api.CreateProjectile;
import io.openshift.appdev.missioncontrol.core.api.StatusMessage;

/**
 * Command interface.
 */
public interface Command {

    StatusMessage getStatusMessage();

    void execute(CreateProjectile projectile);
}
