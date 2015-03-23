package biz.dfch.j.graylog.plugin.output;

import java.net.URI;
import java.util.Collection;

import org.graylog2.plugin.*;

import java.util.Collections;
import java.util.Set;

/**
 * Implement the Plugin interface here.
 */
public class AmqpClientPlugin implements Plugin
{
    @Override
    public PluginMetaData metadata()
    {
        PluginMetaData pluginMetaData = new AmqpClientMetadata();
        return pluginMetaData;
    }

//    @Override
//    public Collection<PluginModule> modules ()
//    {
//        return Collections.<PluginModule>singleton(new AmqpClientModule());
//    }
    @Override
    public Collection<PluginModule> modules () {
        return com.google.common.collect.Lists.newArrayList((PluginModule) new AmqpClientModule());
    }
}