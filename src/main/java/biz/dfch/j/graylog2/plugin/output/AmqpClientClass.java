package biz.dfch.j.graylog2.plugin.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.*;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AmqpClientClass implements MessageOutput
{
    private static final String CONFIG_AMQP_SERVER_NAME = "CONFIG_AMQP_SERVER_NAME";
    private static final String CONFIG_AMQP_SERVER_PORT = "CONFIG_AMQP_SERVER_PORT";
    private static final String CONFIG_AMQP_VIRTUAL_HOST = "CONFIG_AMQP_VIRTUAL_HOST";
    private static final String CONFIG_AMQP_USER_NAME = "CONFIG_AMQP_USER_NAME";
    private static final String CONFIG_AMQP_PASSWORD = "CONFIG_AMQP_PASSWORD";
    private static final String CONFIG_AMQP_PUBLISH_TARGET = "CONFIG_AMQP_PUBLISH_TARGET";
    private static final String CONFIG_AMQP_EXCHANGE_NAME = "CONFIG_AMQP_EXCHANGE_NAME";
    private static final String CONFIG_AMQP_QUEUE_NAME = "CONFIG_AMQP_QUEUE_NAME";
    private static final String CONFIG_AMQP_ROUTING_KEY = "CONFIG_AMQP_ROUTING_KEY";

    private static final Logger LOG = LoggerFactory.getLogger(AmqpClientClass.class);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Configuration configuration;
    private AmqpClient amqpClient = new AmqpClient();

    @Inject
    public AmqpClientClass
            (
                    @Assisted Stream stream,
                    @Assisted Configuration configuration
            )
            throws MessageOutputConfigurationException
    {
        if(null == stream)
        {
            throw new NullPointerException("stream: Parameter validation FAILED. Parameter cannot be null");
        }
        if(null == configuration)
        {
            throw new NullPointerException("configuration: Parameter validation FAILED. Parameter cannot be null");
        }

        this.configuration = configuration;

        try
        {
            amqpClient.connect
                (
                    configuration.getString("CONFIG_AMQP_SERVER_NAME")
                    ,
                    configuration.getInt("CONFIG_AMQP_SERVER_PORT")
                    ,
                    configuration.getString("CONFIG_AMQP_VIRTUAL_HOST")
                    ,
                    configuration.getString("CONFIG_AMQP_USER_NAME")
                    ,
                    configuration.getString("CONFIG_AMQP_PASSWORD")
                );
            if (configuration.getString(CONFIG_AMQP_PUBLISH_TARGET).equals("EXCHANGE"))
            {
                amqpClient.bindToExchange(configuration.getString(CONFIG_AMQP_EXCHANGE_NAME));
            }
            else if (configuration.getString(CONFIG_AMQP_PUBLISH_TARGET).equals("QUEUE"))
            {
                amqpClient.bindToQueue(configuration.getString(CONFIG_AMQP_QUEUE_NAME));
            }
            else
            {
                throw new ProtocolException(String.format("%s: Unsupported publish target.", configuration.getString(CONFIG_AMQP_PUBLISH_TARGET)));
            }
        }
        catch (NoSuchAlgorithmException ex)
        {
            LOG.error("Can not connect to AMQP server " + configuration.getString(CONFIG_AMQP_SERVER_NAME), ex);
        }
        catch (KeyManagementException ex)
        {
            LOG.error("Can not connect to AMQP server " + configuration.getString(CONFIG_AMQP_SERVER_NAME), ex);
        }
        catch (URISyntaxException ex)
        {
            LOG.error("Can not connect to AMQP server " + configuration.getString(CONFIG_AMQP_SERVER_NAME), ex);
        }
        catch (IOException ex)
        {
            LOG.error("Can not connect to AMQP server " + configuration.getString(CONFIG_AMQP_SERVER_NAME), ex);
        }

        isRunning.set(true);
    }

    @Override
    public boolean isRunning()
    {
        return isRunning.get();
    }

    @Override
    public void write(Message message) throws Exception
    {
        if(!isRunning.get())
        {
            return;
        }
        try
        {
            //Map<String, Object> fieldsImmutable = message.getFields();
            Map<String, Object> fields = new HashMap<String, Object>();
            //fields.putAll(fieldsImmutable);

            if(message.hasField("action"))
            {
                fields.put("action", message.getField("action"));
            }
            if(message.hasField("processId"))
            {
                fields.put("processId", message.getField("processId"));
            }
            if(message.hasField("apiVersion"))
            {
                fields.put("apiVersion", message.getField("apiVersion"));
            }
            fields.put("taskStatus", "completed");
//            fields.put("message", message.getMessage());
//            fields.put("source", message.getSource());
//            fields.put("id", message.getId());
//            fields.put("sourceInputId", message.getSourceInputId());
//            fields.put("dateTime", message.getTimestamp().toDateTimeISO());
//            ObjectMapper objectMapper = new ObjectMapper();
//            String json = objectMapper.writeValueAsString(fields);
            amqpClient.sendMessage(fields, message.getMessage());
            //amqpClient.sendMessage(message.getMessage());
        }
        catch (Exception ex)
        {
            LOG.error("Can not send message to AMQP server.", ex);
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public void write(List<Message> messages) throws Exception
    {
        if (!isRunning.get())
        {
            return;
        }
        for (Message message : messages)
        {
            write(message);
        }
    }

    @Override
    public void stop()
    {
        try
        {
            LOG.info("Stopping");
            isRunning.set(false);
            if (null != amqpClient)
            {
                amqpClient.disconnect();
                amqpClient = null;
            }
        }
        catch(Throwable ex)
        {
            LOG.error("Disconnect FAILED.");
        }
    }

    public interface Factory extends MessageOutput.Factory<AmqpClientClass>
    {
        @Override
        AmqpClientClass create(Stream stream, Configuration configuration);

        @Override
        Config getConfig();

        @Override
        Descriptor getDescriptor();
    }

    public static class Config extends MessageOutput.Config
    {
        @Override
        public ConfigurationRequest getRequestedConfiguration()
        {
            final ConfigurationRequest configurationRequest = new ConfigurationRequest();

            configurationRequest.addField(new TextField(
                            CONFIG_AMQP_SERVER_NAME, "AMQP Server Name", "",
                            "Hostname of your AMQP Server",
                            ConfigurationField.Optional.NOT_OPTIONAL)
            );

            configurationRequest.addField(new NumberField(
                            CONFIG_AMQP_SERVER_PORT, "AMQP Port", 5672,
                            "Port of your AMQP Server",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_AMQP_VIRTUAL_HOST, "AMQP Virtual Host Name", "/",
                            "Virtual Host name of your AMQP Server",
                            ConfigurationField.Optional.OPTIONAL)
            );

            final Map<String, String> targets = ImmutableMap.of(
                    "EXCHANGE", "EXCHANGE",
                    "QUEUE", "QUEUE");
            configurationRequest.addField(new DropdownField(
                            CONFIG_AMQP_PUBLISH_TARGET, "Publish Target", "EXCHANGE", targets,
                            "Specify whether to publish to an exchange or a queue",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                        CONFIG_AMQP_EXCHANGE_NAME, "Exchange or Queue Name", "default",
                            "Exchange or queue name",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_AMQP_ROUTING_KEY, "Routing Key", "#",
                            "Routing Key",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_AMQP_USER_NAME, "Username", "guest",
                            "Username to use and connect to AMQP Server",
                            ConfigurationField.Optional.OPTIONAL)
            );

            configurationRequest.addField(new TextField(
                            CONFIG_AMQP_PASSWORD, "Password", "guest",
                            "Password to use and connect to AMQP Server",
                            ConfigurationField.Optional.OPTIONAL,
                            TextField.Attribute.IS_PASSWORD)
            );
            return configurationRequest;
        }
    }

    public static class Descriptor extends MessageOutput.Descriptor
    {
        public Descriptor()
        {
            super((new AmqpClientMetadata()).getName(), false, "", (new AmqpClientMetadata()).getDescription());
        }
    }
}
