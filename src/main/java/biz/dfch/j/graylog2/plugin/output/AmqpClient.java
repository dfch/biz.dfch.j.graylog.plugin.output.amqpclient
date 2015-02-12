package biz.dfch.j.graylog2.plugin.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.sun.jndi.toolkit.url.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.Map;

/**
 * Created by root on 2/10/15.
 */

public class AmqpClient extends AbstractAmqpClient
{
    private final static String SERVER_NAME = "localhost";
    private final static int SERVER_PORT = 5672;
    private final static String VIRTUAL_HOST = "/";
    private final static String USER_NAME = "guest";
    private final static String PASSWORD = "guest";

    private final static String QUEUE_NAME = "default";
    private final static String EXCHANGE_NAME = "EMOC";
    private final static String EXCHANGE_TYPE = "direct";
    private final static boolean EXCHANGE_DURABLE = true;
    private final static String ROUTING_KEY = "#";

    private static final Logger LOG = LoggerFactory.getLogger(AmqpClient.class);

    private URI uri;
    private String publishTarget;

    private final static ConnectionFactory connectionFactory = new ConnectionFactory();
    private static Connection connection;
    private static Channel channel;

    boolean connect()
            throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        boolean fReturn = false;
        try
        {
            fReturn = connect(null, 0, null, null, null);
        }
        finally
        {
            // N/A
        }
        return fReturn;
    }

    boolean connect
            (
                String serverName
                ,
                int serverPort
                ,
                String virtualHost
                ,
                String username
                ,
                String password
            )
            throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        boolean fReturn = false;
        try
        {
            if(null == serverName || serverName.isEmpty())
            {
                serverName = SERVER_NAME;
            }
            if(0 == serverPort)
            {
                serverPort = SERVER_PORT;
            }
            if(null == virtualHost || virtualHost.isEmpty())
            {
                virtualHost = VIRTUAL_HOST;
            }
            if(null == username || username.isEmpty())
            {
                username = USER_NAME;
            }
            if(null == password || password.isEmpty())
            {
                password = PASSWORD;
            }

            String amqpUriFormat = "amqp://%s:%s@%s:%s/%s";
            if(virtualHost.equals(VIRTUAL_HOST))
            {
                amqpUriFormat = "amqp://%s:%s@%s:%s";
            }
            String uri = String.format(amqpUriFormat, username, password, serverName, serverPort, virtualHost);
            fReturn = connect(uri);
        }
        finally
        {
            // N/A
        }
        return fReturn;
    }

    boolean bindToQueue(String name) throws IOException
    {
        boolean fReturn = false;
        if(null == name || name.isEmpty())
        {
            throw new IllegalArgumentException(String.format("%s: Parameter validation FAILED. Queue name cannot be null.", name));
        }
        channel.queueDeclare(name, false, false, false, null);
        publishTarget = name;
        fReturn = true;
        return fReturn;
    }

    boolean bindToExchange(String name, String type, boolean durable) throws IOException
    {
        if(null == name || name.isEmpty())
        {
            throw new IllegalArgumentException(String.format("%s: Parameter validation FAILED. Exchange name cannot be null.", name));
        }
        if(null == type || type.isEmpty())
        {
            throw new IllegalArgumentException(String.format("%s: Parameter validation FAILED. Exchange type cannot be null.", type));
        }
        boolean fReturn = false;

        channel.exchangeDeclare(name, type, durable);
        publishTarget = name;
        fReturn = true;

        return fReturn;
    }

    boolean bindToExchange(String name) throws IOException
    {
        if(null == name || name.isEmpty())
        {
            throw new IllegalArgumentException(String.format("%s: Parameter validation FAILED. Exchange name cannot be null.", name));
        }
        return bindToExchange(name, EXCHANGE_TYPE, EXCHANGE_DURABLE);
    }

    boolean connect
            (
                String uriString
            )
            throws IllegalArgumentException, IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, ProtocolException
    {
        boolean fReturn = false;

        try
        {
            if(null == uriString || uriString.isEmpty())
            {
                throw new IllegalArgumentException("uriString: Parameter validation FAILED. Parameter cannot be null or empty.");
            }
            URI uri = new URI(uriString);

            if(null != connection && connection.isOpen())
            {
                throw new ProtocolException(String.format("%s: Connection already established.", uriString));
            }
            if(null != channel && channel.isOpen())
            {
                throw new ProtocolException(String.format("%s: Connection already established.", uriString));
            }

            connectionFactory.setUri(uri);
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            this.uri = uri;

            fReturn = true;
        }
        catch(URISyntaxException ex)
        {
            LOG.error("uri: Parameter validation FAILED. Invalid syntax.", ex);
            throw ex;
        }
        finally
        {
            // N/A
        }
        return fReturn;
    }

    boolean disconnect() throws Throwable
    {
        LOG.debug("disconnect");
        if(null != channel && channel.isOpen())
        {
            channel.close();
            channel = null;
        }
        if(null != connection && connection.isOpen())
        {
            connection.close();
            connection = null;
        }
        return true;
    }

    boolean sendMessage
            (
                String message
            )
            throws IllegalArgumentException, IOException
    {
        boolean fReturn = false;

        try
        {
            fReturn = sendMessage(ROUTING_KEY, null, message);
        }
        catch(IOException ex)
        {
            throw new IOException(String.format("%s: Publishing message FAILED.", EXCHANGE_NAME), ex);
        }
        finally
        {
            // N/A
        }
        return fReturn;
    }

    boolean sendMessage
            (
                    Map<String, Object> header
                    ,
                    String message
            )
            throws IllegalArgumentException, IOException
    {
        boolean fReturn = false;

        try
        {
            fReturn = sendMessage(ROUTING_KEY, header, message);
        }
        catch(IOException ex)
        {
            throw new IOException(String.format("%s: Publishing message FAILED.", EXCHANGE_NAME), ex);
        }
        finally
        {
            // N/A
        }
        return fReturn;
    }

    boolean sendMessage
            (
                    String routingKey
                    ,
                    Map<String, Object> header
                    ,
                    String message
            )
            throws IllegalArgumentException, IOException
    {
        boolean fReturn = false;

        try
        {
            if(null == channel || !channel.isOpen())
            {
                throw new ProtocolException(String.format("channel: No connection established."));
            }
            if(null == message)
            {
                message = "";
            }
            if(null == routingKey)
            {
                routingKey = ROUTING_KEY;
            }

            System.out.println(String.format("message: '%s'", message));
            byte[] abMessage = message.getBytes();

            com.rabbitmq.client.AMQP.BasicProperties basicProperties = null;
            if(null != header) {
                basicProperties = new com.rabbitmq.client.AMQP.BasicProperties(null, null, header, null, null, null, null, null, null, null, null, null, null, null);
            }
            channel.basicPublish(publishTarget, routingKey, basicProperties, abMessage);

//            // This is actually not really working , so we leave it to send without headers
//            Map<java.lang.String, java.lang.Object> mapImmutable = Maps.newHashMap();
//            Map<java.lang.String, java.lang.Object> map = Maps.newHashMap();
//            map.putAll(mapImmutable);
//
//
//            // publish the message to the exchange or queue
//            //channel.basicPublish(publishTarget, ROUTING_KEY, null, abMessage);
//
            fReturn = true;
        }
        catch(IOException ex)
        {
            throw new IOException(String.format("%s: Publishing message FAILED.", publishTarget), ex);
        }
        catch(IllegalArgumentException ex)
        {
            throw ex;
        }
        finally
        {
            // N/A
        }
        return fReturn;
    }

    protected void finalize()
            throws Throwable
    {
        LOG.debug("finalize");
        try
        {
            if(null != channel && channel.isOpen())
            {
                channel.close();
                channel = null;
            }
            if(null != connection && connection.isOpen())
            {
                connection.close();
                connection = null;
            }
        }
        finally
        {
            super.finalize();
        }
    }
}
