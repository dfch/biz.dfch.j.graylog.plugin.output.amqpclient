package biz.dfch.j.graylog.plugin.output;

import java.io.IOException;

/**
 * Pseudo-abstract class
 *
 * When time do some proper abstract class
 */
public abstract class AbstractAmqpClient
{
    abstract boolean sendMessage(String message) throws IllegalArgumentException, IOException;
    abstract boolean disconnect() throws Throwable;
}
