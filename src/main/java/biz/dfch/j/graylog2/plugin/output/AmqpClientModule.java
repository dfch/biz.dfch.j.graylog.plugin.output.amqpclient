package biz.dfch.j.graylog2.plugin.output;

import com.google.inject.multibindings.MapBinder;
import org.graylog2.plugin.PluginModule;
import org.graylog2.plugin.outputs.MessageOutput;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class AmqpClientModule extends PluginModule {
    @Override
    protected void configure() {
        final MapBinder<String, MessageOutput.Factory<? extends MessageOutput>> outputMapBinder = outputsMapBinder();
        installOutput(outputMapBinder, AmqpClientClass.class, AmqpClientClass.Factory.class);
    }
}