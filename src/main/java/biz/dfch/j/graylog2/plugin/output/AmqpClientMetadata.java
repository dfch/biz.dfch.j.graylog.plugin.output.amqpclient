package biz.dfch.j.graylog2.plugin.output;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class AmqpClientMetadata implements PluginMetaData
{
    @Override
    public String getUniqueId()
    {
        return "1e6abecf-e53a-4d57-8944-9d66537f3090";
    }
    @Override
    public String getName()
    {
        return "SCCloud AMQP Output";
    }
    @Override
    public String getAuthor()
    {
        return "Ronald Rink, SCCloud";
    }
    @Override
    public URI getURL()
    {
        return URI.create("http://www.swisscom.ch");
    }
    @Override
    public Version getVersion()
    {
        return new Version(1, 0, 0);
    }
    @Override
    public String getDescription()
    {
        return "SCCloud AMQP Output. With this plugin you can send arbitrary messages to an AMQP Exchange.";
    }
    @Override
    public Version getRequiredVersion()
    {
        return new Version(1, 0, 0);
    }
    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities()
    {
        return java.util.EnumSet.of(ServerStatus.Capability.SERVER);
    }
}
