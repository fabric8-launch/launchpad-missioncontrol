# katapult
Empower engineering teams using Red Hat technologies with an intuitive experience to quickly and confidently promote their code from development to production.

Prerequisites
-------------
1. Java
2. Apache Maven
3. A GitHub Account

* Log into GitHub and generate an access token for use here:
--  https://help.github.com/articles/creating-an-access-token-for-command-line-use/
* Create 3 environment variables:
-- `GITHUB_USERNAME`, `GITHUB_TOKEN`, `GITHUB_PASSWORD`

For instance you may put into your `~/.bash_profile`:

    export GITHUB_USERNAME=ALRubinger
    export GITHUB_TOKEN=[token created from above]
    export GITHUB_PASSWORD=mygithub-password

Updates to `~./bash_profile` may require logging out of the shell to be visible; you may check 
by typing into a terminal:

    $ echo $GITHUB_USERNAME

* Associate the developer application client id/secret with webapp
Edit the web/src/main/webapp/WEB-INF/web.xml to configure the GITHUB_CLIENT_ID and CLIENT_SECRET entries:

```
  <env-entry>  
    <env-entry-name>java:global/GITHUB_CLIENT_ID</env-entry-name>
    <env-entry-type>java.lang.String</env-entry-type>
    <env-entry-value>***</env-entry-value>
  </env-entry>
  
  <env-entry>
    <env-entry-name>java:global/CLIENT_SECRET</env-entry-name>
    <env-entry-type>java.lang.String</env-entry-type>
    <env-entry-value>***</env-entry-value>
  </env-entry>
```

Build and Run the Tests
-----------------------

1. Execute:

        $ mvn clean install
