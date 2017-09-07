# openshift.io Launchpad Mission Control
Empower engineers to quickly and confidently promote their code from development to production.

The Launchpad hosted at launch.openshift.io is a service bringing Continuous Delivery features and methodology to our communities and customers, at the push of a button.

Mission Control, as the name suggests, coordinates actions among dependent services.  Its responsibility is to take the following inputs:

* A GitHub project
* A GitHub user (via OAuth token)
* An OpenShift instance's API URL
* An OpenShift user (via OAuth token)

And perform the following actions:

* Fork the GitHub project into the GitHub user's namespace
* Create an OpenShift project
* Create a Jenkins Pipeline BuildConfig
* Associate the OpenShift project with the newly-forked GitHub repo
* Create a GitHub webhook on the newly-forked GitHub project to register push events to the OpenShift project

This will result in a fully-pipelined OpenShift project from a source GitHub repo.  The pipeline definition itself is expected to reside in a Groovy-based Jenkins Pipeline (https://github.com/jenkinsci/workflow-plugin/blob/master/README.md#introduction) script called a Jenkinsfile.

Prerequisites to Build
----------------------
1. Java
2. Apache Maven

Prerequisites to Run Integration Tests
--------------------------------------

1. A GitHub Account

    * Log into GitHub and generate an access token for use here:
    --  https://github.com/settings/tokens
        * Set scopes
            * `repo`
            * `admin:repo_hook`
            * `delete_repo`
    * Create 2 environment variables:
        * `LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME`
        * `LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN`

    For instance you may create a `~/launchpad-missioncontrol-env.sh` file and add:
    
        #!/bin/sh
        export LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME=<your github username>
        export LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN=<token created from above>
    
    You can also reuse what's already defined in your `.gitconfig` file:
    
        #!/bin/sh
        export LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME=`git config github.user`
        export LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN=`git config github.token`

    Use `source ~/launchpad-missioncontrol-env.sh` to make your changes visible; you may check by typing into a terminal:

        $ echo $LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME
        $ echo $LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN

     
2. A locally-running instance of OpenShift 

    * Install minishift and prerequisite projects by following these instructions
        * https://github.com/minishift/minishift#installing-minishift
	
    * Check everything works okay by loggin in to the OpenShift console
        * Run `minishift start --memory=4096`
        * Open the URL found in the output of the previous command in a browser. You can get the same URL by executing `minishift console --url` as well.
        * Log in with user `developer` and password `developer`
        * You may have to accept some security exceptions in your browser because of missing SSL Certificates

    * Set up the following environment variables (possibly in your `launchpad-missioncontrol-env.sh` file):
        ```
        LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_API_URL=<insert minishift console url something like https://192.168.99.128:8443>
        LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=<insert minishift console url something like https://192.168.99.128:8443>
        ```
        
        You can do this automatically in the following way:       
        ```
        export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_API_URL=`minishift console --url`
        export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=`minishift console --url`
        ```

3. A Keycloak server

    * Make sure your Federated Identity settings are correct
        * Open Chrome and go to: https://prod-preview.openshift.io/
        * Click Sign-in (in the upper right corner), you should be redirected to developers.redhat.com
        * Navigate to https://sso.prod-preview.openshift.io/auth/realms/fabric8/account/identity
        * Make sure that the Github and Openshift v3 tokens are set

    * Set up the following environment variables (possibly in your `launchpad-missioncontrol-env.sh` file): 
      ```
        export LAUNCHPAD_KEYCLOAK_URL=https://sso.prod-preview.openshift.io/auth
        export LAUNCHPAD_KEYCLOAK_REALM=fabric8
      ```
    IMPORTANT: Mission Control will not use the keycloak server if you provide the following environment variables:
      ```    
        export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME=<user>
        export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD=<pass>
      ```

4. (Optional) Ensure from the previous steps all environment variables are properly set up and sourced into your terminal:

For instance, in a Unix-like environment you may like to create a `launchpad-missioncontrol-env.sh` file to hold the following; this may be executed using `source launchpad-missioncontrol-env.sh`: 

```
#!/bin/sh 

export LAUNCHPAD_MISSIONCONTROL_GITHUB_USERNAME=<replace with your github username>
export LAUNCHPAD_MISSIONCONTROL_GITHUB_TOKEN=<replace with your personal token (see step 1)>
export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_API_URL=`minishift console --url`
export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL=`minishift console --url`
export LAUNCHPAD_KEYCLOAK_URL=https://sso.prod-preview.openshift.io/auth
export LAUNCHPAD_KEYCLOAK_REALM=rh-developers-launch
export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME=developer
export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD=developer
unset LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN
# LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN, if set, will override username/password authentication scheme
#export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN=<token here>

# If Keycloak is enabled, set the LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE parameter
# export LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CLUSTERS_FILE=/etc/openshift-clusters.yaml 
# unset LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_API_URL
# unset LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_CONSOLE_URL

``` 
     
Build and Run the Unit Tests
----------------------------

* Execute:

        $ mvn clean install
        
Run the Integration Tests, Optionally Building
----------------------------------------------

* To build the project and run the integration tests, allowing Maven to start the WildFly server:
 
        $ mvn clean install -Pit


* To skip building and just run the integration tests, allowing Maven to start the WildFly server:

        $ mvn integration-test -Pit
        
* By default the above will install and control the lifecycle for the WildFly server when running full system tests in the "tests" module.  If you would prefer to not have the Maven lifecycle install a WildFly server for you, you may instead:
    * Download and install WildFly 10.0.0.Final from http://wildfly.org/downloads/
    * Start up the WildFly server by going to `$INSTALL_DIR/bin` and executing `standalone.sh` (*nix) or `standalone.bat` (Windows)
    * Run the integration tests and have Maven skip start/stop of the WildFly server by using the `server-remote` profile.  This may speed up your development cycle if you're doing many runs by starting your server on your own and letting it run through several test runs.
        * `$ mvn integration-test -Pit,server-remote` or `$ mvn clean install -Pit,server-remote`

Testing manually
----------------------------
* If you have Mission Control runing , you may upload a ZIP file containing a valid project (you can download one from the Uber generator) by using the following command: 
```
  curl -F "file=@demo.zip" http://localhost:8080/api/missioncontrol/upload
```

IMPORTANT: The `Authorization` header is needed, but it won't be used if you provided the `LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_USERNAME` and `LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_PASSWORD` or the `LAUNCHPAD_MISSIONCONTROL_OPENSHIFT_TOKEN` env vars. 
Therefore run the command as-is.
        
Contributing
------------

* Clone the repository
```
	git clone https://github.com/openshiftio/mission-control.git
```

* In the cloned repository, run the following commands to have your git configuration for this repository all set up: 
```
git config commit.template .github/gitcommit.txt --local
git config commit.gpgsign true --local
```
