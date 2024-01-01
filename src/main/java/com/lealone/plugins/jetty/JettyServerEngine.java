/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package com.lealone.plugins.jetty;

import com.lealone.plugins.service.http.HttpServerEngine;
import com.lealone.server.ProtocolServer;

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
