package org.kontinuity.catapult.web.api;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines our HTTP endpoints as singletons
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
@ApplicationPath(HttpEndpoints.PATH_API)
public class HttpEndpoints extends Application
{
   public static final String PATH_API = "/api";

   @Inject
   private CatapultResource catapultResource;

   @Inject
   private GitHubResource gitHubResource;

   @Inject
   private AppStoreResource appStoreResource;

   @Override
   public Set<Object> getSingletons()
   {
      final Set<Object> singletons = new HashSet<>();
      singletons.add(catapultResource);
      singletons.add(gitHubResource);
      singletons.add(appStoreResource);

      CorsFilter corsFilter = new CorsFilter();
      corsFilter.getAllowedOrigins().add("*");
      corsFilter.setExposedHeaders("Content-Disposition");
      singletons.add(corsFilter);
      return singletons;
   }
}
