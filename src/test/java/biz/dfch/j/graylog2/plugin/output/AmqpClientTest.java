package biz.dfch.j.graylog2.plugin.output;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;


/**
 * Created by root on 2/10/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AmqpClientTest
{
    private AmqpClient _amqpClient;

    @BeforeClass
    public static void BeforeClass()
    {
        System.out.println("BeforeClass");
    }
    @Before
    public void Before()
    {
        System.out.println("Before");
        _amqpClient = new AmqpClient();
    }

    @After
    public void After() throws Throwable
    {
        _amqpClient.disconnect();
    }

    @Test(expected = ProtocolException.class)
    public void  a090_doSendMessage() throws ProtocolException, IOException
    {
        assertEquals(true, _amqpClient.sendMessage("If you see this message, the integration test is broken."));
    }

    @Test
    public void a100_doConnectReturnsTrue() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        assertEquals(true, _amqpClient.connect(null, 0, null, null, null));
    }

    @Test
    public void a102_doConnectReturnsTrue() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, Throwable
    {
        _amqpClient.disconnect();
        assertEquals(true, _amqpClient.connect());
    }

    @Test(expected = ProtocolException.class)
    public void a105_doConnectThrowsProtocolException() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        _amqpClient.connect();
        assertEquals(true, _amqpClient.connect(null, 0, null, null, null));
    }

    @Test(expected = IllegalArgumentException.class)
    @Category(biz.dfch.j.graylog2.plugin.output.AmqpClientTest.class)
    public void a110_doSendMessageThrowsIllegalArgumentException() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        _amqpClient.connect();
        _amqpClient.sendMessage(null);
    }

    @Test
    public void a120_doSendMessageReturnsTrue() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        _amqpClient.connect();
        assertEquals(true, _amqpClient.sendMessage("tralala"));
    }

    @Test
    public void a130_doSendMessageReturnsFalse() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException
    {
        _amqpClient.connect();
        assertEquals(false, _amqpClient.sendMessage(""));
    }

}
