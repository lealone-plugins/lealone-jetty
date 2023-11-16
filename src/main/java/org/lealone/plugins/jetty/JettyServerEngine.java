/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package org.lealone.plugins.jetty;

import org.lealone.plugins.service.http.HttpServerEngine;
import org.lealone.server.ProtocolServer;

public class JettyServerEngine extends HttpServerEngine {

    public static final String NAME = "Jetty";

    public JettyServerEngine() {
        super(NAME);
    }

    @Override
    protected ProtocolServer createProtocolServer() {
        return new JettyServer();
    }
}
