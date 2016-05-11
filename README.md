# Kontinuity Catapult
Empower engineering teams using Red Hat technologies with an intuitive experience to quickly and confidently promote their code from development to production.

Kontinuity is the Public Effort at Red Hat to bring Continuous Delivery features and methodology to our communities and customers.

Catapult, as the name suggests, is a launcher.  Its responsibility is to take the following inputs:

* A GitHub project
* A GitHub user (via OAuth token)
* An OpenShift instance's API URL
* An OpenShift user

And perform the following actions:

* Fork the GitHub project into the GitHub user's namespace
* Create an OpenShift project
* Apply the pipeline template to the OpenShift project
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
    --  https://help.github.com/articles/creating-an-access-token-for-command-line-use/
        * Set scopes
            * `repo`
            * `admin:repo_hook`
            * `delete_repo`
    * Create 3 environment variables:
        * `GITHUB_USERNAME`
        * `GITHUB_TOKEN`
        * `GITHUB_PASSWORD`

    For instance you may put into your `~/.bash_profile`:

        export GITHUB_USERNAME=ALRubinger
        export GITHUB_TOKEN=[token created from above]
        export GITHUB_PASSWORD=mygithub-password
    
    Updates to `~./bash_profile` may require logging out of the shell to be visible; you may check by typing into a terminal:

        $ echo $GITHUB_USERNAME

    
2.  A GitHub OAuth Application
 
    Catapult forks repositories on behalf of users; in order to do this, we must ask the user permission and this is done via the GitHub OAuth Web Flow.  In production Catapult will have an OAuth application registered with GitHub to register the appropriate callbacks, but for security reasons we cannot give out the credentials publicly for testing.  Each developer must set up their own OAuth application.
    
    * Log into the GitHub Settings for your user account and create a new OAuth application
        * https://github.com/settings/applications/new
            * Application Name
                * e.g. `Catapult by Red Hat Kontinuity (Local Development for [@GitHubUsername])`
            * Homepage URL
                * e.g. `http://developers.redhat.com`
            * Application Description
                * Not required
            * Authorization callback URL
                * `http://localhost:8080/kontinuity-catapult/api/github/callback`
                * You may need to replace `localhost` with your local machine's loopback binding depending upon your configuration, but this should work for the majority of cases
        * Hit "Register application"
            
    * You will be shown your new application's `Client ID` and `Client Secret`
        * Set environment variables for these
            * `KONTINUITY_CATAPULT_GITHUB_APP_CLIENT_ID`
            * `KONTINUITY_CATAPULT_GITHUB_APP_CLIENT_SECRET`
        * You may need to log in again or `source` your `~/.bash_profile` or `~/.profile`, depending upon how you've set the environment variables (system-specific).
   
3. A locally-running instance of OpenShift 

    ALR has been running his tests against a local instance of Origin; instructions for getting this stood up are here:
    
    * https://github.com/openshift/origin/blob/master/CONTRIBUTING.adoc
    
    You may take a binary built by the OpenShift team, build an instance locally, obtain through the CDK, or use Vagrant; any way that boots OpenShift locally should be fine. 

    When running, this should give you a local API to execute against at https://localhost:8443, which is where the OpenShiftService tests currently look to make their calls (by default).  To override this, you may specify the environment variable or system property `KONTINUITY_CATAPULT_OPENSHIFT_URL`.


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
        
CI Environment
----------------------------

A Continuous Integration environment is available to build and test the application. This environment leverages many of the components from the [fabric8](http://fabric8.io/) tooling including Jenkins.

Access to the CI environment has the following requirements:

*  Connectivity to the Red Hat VPN.
*  Modification to the `hosts` file on the local machine to reference the hostname of the Jenkins instance as this environment is temporary and is not registered in DNS:

```
10.3.10.147 jenkins.master.distortion.example.com
```

 * The CI environment should now be available at [http://jenkins.master.distortion.example.com](http://jenkins.master.distortion.example.com)

 * Alternatively you can use http://jenkins.master.10.3.10.147.xip.io/ and you don't have to modify `hosts` file.
