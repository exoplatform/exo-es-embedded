package org.exoplatform.addons.es;

import java.util.Collection;

import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;

/**
 * Node allowing to declare a list of plugins
 */
public class EmbeddedNode extends Node {

  private Collection<Class<? extends Plugin>> plugins;

  public EmbeddedNode(Settings settings, Version version, Collection<Class<? extends Plugin>> classpathPlugins) {
    // Use internal class (InternalSettingsPreparer) to create an Environment the same way it is done
    // in standalone mode (load config file, system properties, replace placeholders, ...)
    super(InternalSettingsPreparer.prepareEnvironment(settings, null), classpathPlugins);
    this.plugins = classpathPlugins;
  }

  public Collection<Class<? extends Plugin>> getPlugins() {
    return plugins;
  }
}
