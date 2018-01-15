package org.exoplatform.addons.es;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.ingest.attachment.IngestAttachmentPlugin;
import org.elasticsearch.ingest.common.IngestCommonPlugin;
import org.elasticsearch.mapper.attachments.MapperAttachmentsPlugin;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Servlet starting an embedded Elasticsearch node during PLF startup, and stopping it when PLF stops.
 * It reads configuration from /WEB-INF/elasticsearch.yml.
 */
public class EmbeddedESStartupServlet extends HttpServlet {
  private static final long serialVersionUID = -7795035468127583905L;

  private static final Log LOG = ExoLogger.getLogger(EmbeddedESStartupServlet.class);

  public static final String ES_EMBEDDED_ENABLED_PROPERTY_NAME = "exo.es.embedded.enabled";
  public static final String ES_EMBEDDED_CONFIGURATION_FILE = "exo.es.embedded.configuration.file";

  protected Node node;

  @Override
  public void init() throws ServletException {
    // Make sure RootContainer is started before getting properties
    // This is used to load exo.properties before checking Embedded ES
    // is enabled or not
    RootContainer.getInstance();
    // check if embedded ES must be started or not (defaults to true)
    String esEmbeddedEnabled = PropertyManager.getProperty(ES_EMBEDDED_ENABLED_PROPERTY_NAME);
    if(esEmbeddedEnabled != null && esEmbeddedEnabled.trim().equals("false")) {
      LOG.info("ES Embedded node startup has been disabled");
      return;
    }

    String esEmbeddedConfigurationPath = PropertyManager.getProperty(ES_EMBEDDED_CONFIGURATION_FILE);
    if (StringUtils.isBlank(esEmbeddedConfigurationPath)) {
      LOG.info("Use default ES Embedded configuration file location: /WEB-INF/elasticsearch.yml");
      esEmbeddedConfigurationPath = "/WEB-INF/elasticsearch.yml";
    } else {
      LOG.info("Loading ES Embedded configuration file from custom location : " + esEmbeddedConfigurationPath);
    }

    LOG.info("Initializing elasticsearch Node '" + getServletName() + "'");
    Settings.Builder settings = Settings.builder();

    InputStream resourceAsStream = null;
    try {
      resourceAsStream = getServletContext().getResourceAsStream(esEmbeddedConfigurationPath);
      if (resourceAsStream == null) {
        resourceAsStream = new FileInputStream(esEmbeddedConfigurationPath);
      }
      settings.loadFromStream("/WEB-INF/elasticsearch.yml", resourceAsStream);
      // Parse eXo custom ES settings
      settings.internalMap().forEach((key, value) -> {
        if (!StringUtils.isBlank(value)) {
          while (value.contains("${")) {
            String newValue = Deserializer.resolveString(value);
            if (newValue.equals(value)) {
              LOG.warn("can't resolve expression " + value);
              break;
            }
            value = newValue;
          }
          settings.put(key, value);
        }
      });
    } catch (IOException e) {
      throw new ServletException("Error while initializing elasticsearch node '" + getServletName() + "'", e);
    } finally {
      if(resourceAsStream != null) {
        try {
          resourceAsStream.close();
        } catch (IOException e) {
          LOG.warn("Can't close Input Stream", e);
        }
      }
    }

    // use the custom EmbeddedNode class instead of Node directly to be able to load plugins from classpath
    Collection<Class<? extends Plugin>> plugins = new ArrayList<>();
    Collections.<Class<? extends Plugin>>addAll(plugins, Netty4Plugin.class, IngestCommonPlugin.class, IngestAttachmentPlugin.class, ReindexPlugin.class, MapperAttachmentsPlugin.class);
    node = new EmbeddedNode(settings.build(), Version.CURRENT, plugins);
    try {
      node.start();
    } catch (NodeValidationException e) {
      LOG.error("Error when starting ES in embedded mode", e);
    }
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
