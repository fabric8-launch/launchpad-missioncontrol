# katapult
Empower engineering teams using Red Hat technologies with an intuitive experience to quickly and confidently promote their code from development to production.

Prerequisites to Build
----------------------
1. Java
2. Apache Maven

Prerequisites to Run Integration Tests
--------------------------------------
1. A GitHub Account

    * Log into GitHub and generate an access token for use here:
    --  https://help.github.com/articles/creating-an-access-token-for-command-line-use/
    * Create 3 environment variables:
    -- `GITHUB_USERNAME`, `GITHUB_TOKEN`, `GITHUB_PASSWORD`

    For instance you may put into your `~/.bash_profile`:

        export GITHUB_USERNAME=ALRubinger
        export GITHUB_TOKEN=[token created from above]
        export GITHUB_PASSWORD=mygithub-password
    
    Updates to `~./bash_profile` may require logging out of the shell to be visible; you may check by typing into a terminal:

        $ echo $GITHUB_USERNAME

    Associate the developer application client id/secret with webapp. This should be a developer app that has a callback url
    configured to work with the testing environment, e.g., http://localhost:8080/katapult/api/github/callback.
    Configure the `GITHUB_DEV_APP_CLIENT_ID` and `GITHUB_DEV_APP_SECRET` environment variables
    to contain the id and secret for the github developer application you want to use. You can also specify system properties
    of the same name to override any environment variable setting.
    
2. A locally-running instance of OpenShift 

    ALR has been running his tests against a local instance of Origin; instructions for getting this stood up are here:
        https://github.com/openshift/origin/blob/master/CONTRIBUTING.adoc

    When running, this should give you a local API to execute against at https://localhost:8443, which is where the OpenShiftService tests currently look to make their calls.


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
        
