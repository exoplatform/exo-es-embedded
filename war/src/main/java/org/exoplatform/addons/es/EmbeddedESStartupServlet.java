package org.exoplatform.addons.es;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.ingest.attachment.IngestAttachmentPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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
    Settings.Builder settings = Settings.builder();

    InputStream resourceAsStream = getServletContext().getResourceAsStream("/WEB-INF/elasticsearch.yml");
    if (resourceAsStream == null) {
      throw new ServletException("Error while initializing elasticsearch node, configuration file '/WEB-INF/elasticsearch.yml' couldn't be found");
    }
    try {
      settings.loadFromStream("/WEB-INF/elasticsearch.yml", resourceAsStream);
      resourceAsStream.close();
    } catch (IOException e) {
      throw new ServletException("Error while initializing elasticsearch node '" + getServletName() + "'", e);
    }

    String pathData = settings.get("path.data");
    if (pathData == null) {
      settings.put("path.data", System.getProperty("exo.data.dir"));
    } else if (pathData.startsWith("${")) {
      pathData = pathData.replace("${", "");
      pathData = pathData.replace("}", "");
      settings.put("path.data", System.getProperty(pathData));
    }

    // use the custom EmbeddedNode class instead of Node directly to be able to load plugins from classpath
    Collection plugins = new ArrayList<>();
    Collections.<Class<? extends Plugin>>addAll(plugins, Netty4Plugin.class, IngestAttachmentPlugin.class, ReindexPlugin.class);
    node = new EmbeddedNode(settings.build(), Version.CURRENT, plugins);
    try {
      node.start();
    } catch (NodeValidationException e) {
      LOG.error("Error when starting ES in embedded mode", e);
    }
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public void destroy() {
    if (node != null) {
      try {
        node.close();
      } catch (IOException e) {
        LOG.error("Error when stopping ES in embedded mode", e);
      }
    }
  }
}
