/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package org.lealone.plugins.jetty;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.io.SelectorManager;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.Scheduler;

public class JettyServerConnector extends ServerConnector {

    private ServerSocketChannel serverChannel;

    public JettyServerConnector(Server server) {
        super(server, null, null, null, 0, 1, new HttpConnectionFactory());
    }

    public ServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    public void setServerChannel(ServerSocketChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    public void open() throws IOException {
        super.open();
        serverChannel.configureBlocking(false);
    }

    @Override
    protected ServerSocketChannel openAcceptChannel() throws IOException {
        return serverChannel;
    }

    @Override
    public long getIdleTimeout() {
        return -1;
    }

    public AbstractConnection accept(SocketChannel channel) {
        // channel.configureBlocking(false);
        Socket socket = channel.socket();
        configure(socket);
        return ((JettyServerConnectorManager) getSelectorManager()).accept0(channel);
    }

    @Override
    protected SelectorManager newSelectorManager(Executor executor, Scheduler scheduler, int selectors) {
        return new JettyServerConnectorManager(executor, scheduler, selectors);
    }

    protected class JettyServerConnectorManager extends ServerConnectorManager {

        private final JettyManagedSelector selector;
        private AbstractConnection conn;

        public JettyServerConnectorManager(Executor executor, Scheduler scheduler, int selectors) {
            super(executor, scheduler, selectors);
            selector = newSelector(0);
        }

        @Override
        public void accept(SelectableChannel channel, Object attachment) {
            super.accept(channel, attachment);
        }

        @Override
        protected JettyManagedSelector newSelector(int id) {
            return new JettyManagedSelector(this, id);
        }

        @Override
        protected ManagedSelector chooseSelector() {
            return selector;
        }

        @Override
        protected void execute(Runnable task) {
            if (isStarted())
                task.run();
            else
                super.execute(task);
        }

        @Override
        protected void doStart() throws Exception {
            super.doStart();
        }

        public AbstractConnection accept0(SelectableChannel channel) {
            super.accept(channel);
            AbstractConnection conn = this.conn;
            this.conn = null;
            return conn;
        }

        @Override
        public void connectionOpened(Connection connection, Object context) {
            super.connectionOpened(connection, context);
            conn = ((AbstractConnection) connection);
        }
    }

    protected class JettyManagedSelector extends ManagedSelector {

        private final Selector selector;

        public JettyManagedSelector(SelectorManager selectorManager, int id) {
            super(selectorManager, id);
            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void submit(SelectorUpdate update) {
            update.update(selector);
        }

        @Override
        protected void doStart() throws Exception {
            // super.doStart();
        }
    }
}
