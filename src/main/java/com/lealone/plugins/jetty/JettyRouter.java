/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package com.lealone.plugins.jetty;

import java.util.Map;

import com.lealone.plugins.service.http.HttpRouter;
import com.lealone.plugins.service.http.HttpServer;

public class JettyRouter implements HttpRouter {
    @Override
    public void init(HttpServer server, Map<String, String> config) {
    }
}
