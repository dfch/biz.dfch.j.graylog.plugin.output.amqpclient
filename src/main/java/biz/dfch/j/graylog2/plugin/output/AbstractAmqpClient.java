package biz.dfch.j.graylog2.plugin.output;

import java.io.IOException;

/**
 * Pseudo-abstract class
 *
 * When time do some
 */
public abstract class AbstractAmqpClient
{
    abstract boolean sendMessage(String message) throws IllegalArgumentException, IOException;
    abstract boolean disconnect() throws Throwable;
}
