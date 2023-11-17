/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package org.lealone.plugins.jetty;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.eclipse.jetty.io.AbstractConnection;
import org.lealone.db.scheduler.Scheduler;
import org.lealone.net.NetBuffer;
import org.lealone.net.TransferConnection;
import org.lealone.net.WritableChannel;

public class JettyServerConnection extends TransferConnection {

    // private final JettyServer server;
    // private final Scheduler scheduler;

    private final JettyServerConnector connector;
    private final SocketChannel channel;
    private final AbstractConnection conn;

    public JettyServerConnection(JettyServer server, WritableChannel channel, Scheduler scheduler) {
        super(channel, true);
        // this.server = server;
        // this.scheduler = scheduler;

        this.connector = server.getJettyServerConnector();
        this.channel = channel.getSocketChannel();
        conn = connector.accept(this.channel);
    }

    @Override
    public ByteBuffer getPacketLengthByteBuffer() {
        return null;
    }

    @Override
    public int getPacketLength() {
        return 0;
    }

    @Override
    public void handle(NetBuffer buffer) {
        conn.onFillable();
    }
}
