package io.openshift.appdev.missioncontrol.service.openshift.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftCluster;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftClusterRegistry;
import io.openshift.appdev.missioncontrol.service.openshift.api.OpenShiftSettings;
import org.yaml.snakeyaml.Yaml;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Singleton
public class OpenShiftClusterRegistryImpl implements OpenShiftClusterRegistry {

    private final Set<OpenShiftCluster> CLUSTERS;

    private final OpenShiftCluster defaultCluster;

    public OpenShiftClusterRegistryImpl() {
        Set<OpenShiftCluster> clusters = new LinkedHashSet<>();
        String configFile = OpenShiftSettings.getOpenShiftClustersConfigFile();
        if (configFile == null) {
            defaultCluster = new OpenShiftCluster("openshift-v3",
                                                  OpenShiftSettings.getOpenShiftApiUrl(),
                                                  OpenShiftSettings.getOpenShiftConsoleUrl());
            clusters.add(defaultCluster);
        } else {
            Path configFilePath = Paths.get(configFile);
            try (BufferedReader reader = Files.newBufferedReader(configFilePath)) {
                Yaml yaml = new Yaml(new OpenShiftClusterConstructor());
                List<OpenShiftCluster> configClusters = (List<OpenShiftCluster>) yaml.loadAs(reader, List.class);
                assert configClusters != null : "Config file " + configFile + " is an invalid YAML file";
                assert configClusters.size() > 0 : "No entries found in " + configFile;
                clusters.addAll(configClusters);
                defaultCluster = configClusters.get(0);
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading OpenShift Config file", e);
            }
        }
        CLUSTERS = Collections.unmodifiableSet(clusters);
    }

    @Override
    public OpenShiftCluster getDefault() {
        return defaultCluster;
    }

    @Override
    public Set<OpenShiftCluster> getClusters() {
        return CLUSTERS;
    }
}
