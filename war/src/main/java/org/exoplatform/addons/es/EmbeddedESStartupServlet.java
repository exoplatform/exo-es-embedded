package org.exoplatform.addons.es;

import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.mapper.attachments.MapperAttachmentsPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin;
import org.elasticsearch.plugins.Plugin;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Servlet starting an embedded Elasticsearch node during PLF startup, and stopping it when PLF stops.
 * It reads configuration from /WEB-INF/elasticsearch.yml.
 */
public class EmbeddedESStartupServlet extends HttpServlet {

  private static final Log LOG = ExoLogger.getLogger(EmbeddedESStartupServlet.class);

  public static final String ES_EMBEDDED_ENABLED_PROPERTY_NAME = "exo.es.embedded.enabled";

  protected Node node;

  @Override
  public void init() throws ServletException {

    // check if embedded ES must be started or not (defaults to true)
    String esEmbeddedEnabled = PropertyManager.getProperty(ES_EMBEDDED_ENABLED_PROPERTY_NAME);
    if(esEmbeddedEnabled != null && esEmbeddedEnabled.trim().equals("false")) {
      LOG.debug("ES Embedded node startup has been disabled");
      return;
    }

    LOG.info("Initializing elasticsearch Node '" + getServletName() + "'");
    Settings.Builder settings = Settings.settingsBuilder();

    InputStream resourceAsStream = getServletContext().getResourceAsStream("/WEB-INF/elasticsearch.yml");
    if (resourceAsStream != null) {
      settings.loadFromStream("/WEB-INF/elasticsearch.yml", resourceAsStream);
      try {
        resourceAsStream.close();
      } catch (IOException e) {
        // ignore
      }
    }

    // replace variable in ${...} by their value
    settings.replacePropertyPlaceholders();

    if (settings.get("http.enabled") == null) {
      settings.put("http.enabled", false);
    }

    // use the custom EmbeddedNode class instead of Node directly to be able to load plugins from classpath
    Environment environment = new Environment(settings.build());
    Collection plugins = new ArrayList<>();
    Collections.<Class<? extends Plugin>>addAll(plugins, MapperAttachmentsPlugin.class, DeleteByQueryPlugin.class);
    node = new EmbeddedNode(environment, Version.CURRENT, plugins);
    node.start();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public void destroy() {
    if (node != null) {
      node.close();
    }
  }
}
