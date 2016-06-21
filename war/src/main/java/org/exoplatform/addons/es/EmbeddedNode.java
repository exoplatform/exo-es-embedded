package org.exoplatform.addons.es;

import org.elasticsearch.Version;
import org.elasticsearch.common.cli.Terminal;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;

import java.util.Collection;

/**
 * Node allowing to declare a list of plugins
 */
public class EmbeddedNode extends Node {

  private Version version;
  private Collection<Class<? extends Plugin>> plugins;

  public EmbeddedNode(Settings settings, Version version, Collection<Class<? extends Plugin>> classpathPlugins) {
    // Use internal class (InternalSettingsPreparer) to create an Environment the same way it is done
    // in standalone mode (load config file, system properties, replace placeholders, ...)
    super(InternalSettingsPreparer.prepareEnvironment(settings, null), version, classpathPlugins);
    this.version = version;
    this.plugins = classpathPlugins;
  }

  public Collection<Class<? extends Plugin>> getPlugins() {
    return plugins;
  }

  public Version getVersion() {
    return version;
  }
}
