package org.kontinuity.catapult.service.keycloak.impl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * To get a valid token:
 *
 * - Open Chrome and go to: http://prod-preview.openshift.io/
 * - After authentication, grab the token from the URL. There should be a http://prod-preview.openshift.io/?token=XXX (Use the network console if needed)
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class KeycloakServiceImplTest {
    @Test
    public void testBuildUrl() {
        String url = KeycloakServiceImpl.buildURL("http://sso.prod-preview.openshift.io", "fabric8", "github");
        Assert.assertEquals("http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token", url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTokenOpenshift() {
        String token = "token";
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://sso.prod-preview.openshift.io", "fabric8");
        Assert.assertNotNull(service.getOpenshiftV3Token(token));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTokenGithub() {
        String token = "InvalidToken";
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://sso.prod-preview.openshift.io", "fabric8");
        Assert.assertNotNull(service.getGithubToken(token));
    }

    @Test
    @Ignore("Need a valid token")
    public void testValidTokenGithub() {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ6RC01N29CRklNVVpzQVdxVW5Jc1Z1X3g3MVZJamQxaXJHa0dVT2lUc0w4In0.eyJqdGkiOiI1MzU5ZGJjMy05ZTJlLTQ5NmEtYWQ1ZS0yMmZjODc1MjZmMWQiLCJleHAiOjE0ODk2OTE4MjgsIm5iZiI6MCwiaWF0IjoxNDg5NjkwMDI4LCJpc3MiOiJodHRwOi8vc3NvLnByb2QtcHJldmlldy5vcGVuc2hpZnQuaW8vYXV0aC9yZWFsbXMvZmFicmljOCIsImF1ZCI6ImZhYnJpYzgtb25saW5lLXBsYXRmb3JtIiwic3ViIjoiMGY5MWRhYTEtOTdiYi00NWM4LWFhMGEtYzZhYTJjZDc0ZGY4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZmFicmljOC1vbmxpbmUtcGxhdGZvcm0iLCJhdXRoX3RpbWUiOjE0ODk2OTAwMjgsInNlc3Npb25fc3RhdGUiOiI1NWVlNGVhZC0zNWJkLTQzOWMtYmE3OS1hNDA3MWZmM2MwYmIiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiI3NzEyMTVkNC04N2EzLTQ3NzctYjViZS0xOTVlZjUwMDYxNmQiLCJhbGxvd2VkLW9yaWdpbnMiOltdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJicm9rZXIiOnsicm9sZXMiOlsicmVhZC10b2tlbiJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sIm5hbWUiOiJHZW9yZ2UgR2FzdGFsZGkiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJyaG4tc3VwcG9ydC1nZ2FzdGFsZCIsImdpdmVuX25hbWUiOiJHZW9yZ2UiLCJmYW1pbHlfbmFtZSI6Ikdhc3RhbGRpIiwiZW1haWwiOiJnZ2FzdGFsZEByZWRoYXQuY29tIn0.dNWa0nZR0AIWR4sLhEDepgSSQkKfhC91rIC_5n640MmpcX7YPQgN9ANxG09nKMG_o_ndCeuGZmxe9qHYSfyRLINKc2Kx1M4iBvEAF0qSQvPcqIUy1UVcF9USWC-CU3ZWNF5IFiLdMzbB-r9K76sSpTl2VMIsvGpvrlQArFBCf0Ci8C-HkNZijDmQMMxBBCddezq3Jhq0IsviuEgaVgnbhNNqCGuZmXzwm69bob-I4b0Svrah9n5zuSp-KzH7xWvV_f7aJrNs6bTxE9yBVEdu-U4Z-bfYpLhVRbuUwtVxisrwC1Atx4aLIJ0t4G7Ub2hSuQ7w--s0FZihDMWaL9dv2A";
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://sso.prod-preview.openshift.io", "fabric8");
        Assert.assertNotNull(service.getGithubToken(token));
    }

    @Test
    @Ignore("Need a valid token")
    public void testValidTokenOpenshift() {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ6RC01N29CRklNVVpzQVdxVW5Jc1Z1X3g3MVZJamQxaXJHa0dVT2lUc0w4In0.eyJqdGkiOiIzODc5Zjc0MC1iMTJjLTRiMzUtODVhYy00YzU1MTU4N2JmNWEiLCJleHAiOjE0ODk2OTUxNjMsIm5iZiI6MCwiaWF0IjoxNDg5NjkzMzYzLCJpc3MiOiJodHRwOi8vc3NvLnByb2QtcHJldmlldy5vcGVuc2hpZnQuaW8vYXV0aC9yZWFsbXMvZmFicmljOCIsImF1ZCI6ImZhYnJpYzgtb25saW5lLXBsYXRmb3JtIiwic3ViIjoiMGY5MWRhYTEtOTdiYi00NWM4LWFhMGEtYzZhYTJjZDc0ZGY4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZmFicmljOC1vbmxpbmUtcGxhdGZvcm0iLCJhdXRoX3RpbWUiOjE0ODk2OTMzNjMsInNlc3Npb25fc3RhdGUiOiIwZGNkMmJlNi1lNTdlLTQzYzQtYTE2ZC1hYzE1ZmIxNTNhYzMiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiI3NzQxMGY4YS0wYjZjLTQ1YzAtOGM4Ny03NzNmZTkxM2M3YmYiLCJhbGxvd2VkLW9yaWdpbnMiOltdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJicm9rZXIiOnsicm9sZXMiOlsicmVhZC10b2tlbiJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sIm5hbWUiOiJHZW9yZ2UgR2FzdGFsZGkiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJyaG4tc3VwcG9ydC1nZ2FzdGFsZCIsImdpdmVuX25hbWUiOiJHZW9yZ2UiLCJmYW1pbHlfbmFtZSI6Ikdhc3RhbGRpIiwiZW1haWwiOiJnZ2FzdGFsZEByZWRoYXQuY29tIn0.QPGkuaGQq9bgp0sq9DvwsGOM4v_KTmyIB6aVqrhcGTdYx68Pvk_LCCH4qabe6D16XcAOJOGHUp2sqULMNi5oEv8d0MiRQRFXnj4Puw5oUUSCa3QDIl3gYDnmwoiLDmMWWZVb3IK7NqJSAfeEzTunNrDfmECEiJ09Tj6akSEE8vu6AOIceNo8d6OHy5b-LvvMWRBzCL972HNOiK3M5dJJxdfUd7bDlFxh5M3NCkjQVvuakVep3x_NepTg-uTXy3RE44ceqLZnYxvFT4l-0jW5xmPk0NwGZg3uSTum9u6BX06ILHuEatcKBrpD5nazibjecNOs2b3rm_-gCzodRg9pqQ";
        KeycloakServiceImpl service = new KeycloakServiceImpl("http://sso.prod-preview.openshift.io", "fabric8");
        Assert.assertNotNull(service.getOpenshiftV3Token(token));
    }

}
