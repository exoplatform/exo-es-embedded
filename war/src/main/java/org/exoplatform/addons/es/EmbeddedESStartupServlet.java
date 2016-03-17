package org.exoplatform.addons.es;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;

/**
 * Servlet starting an embedded Elasticsearch node during PLF startup, and stopping it when PLF stops.
 * It reads configuration from /WEB-INF/elasticsearch.yml.
 */
public class EmbeddedESStartupServlet extends HttpServlet {

  private static final Log LOG = ExoLogger.getLogger(EmbeddedESStartupServlet.class);

  protected Node node;

  @Override
  public void init() throws ServletException {

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

    if (settings.get("http.enabled") == null) {
      settings.put("http.enabled", false);
    }

    node = NodeBuilder.nodeBuilder().settings(settings).node();
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
