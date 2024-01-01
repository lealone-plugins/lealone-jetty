/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package com.lealone.plugins.jetty.test;

import java.nio.ByteBuffer;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import com.lealone.main.Lealone;

public class JettyServerStart {

    public static void main(String[] args) throws Exception {
        startLealoneJettyServer(args);
        // startRawJettyServer();
    }

    public static void startLealoneJettyServer(String[] args) throws Exception {
        Lealone.main(args);
    }

    public static void startRawJettyServer() throws Exception {
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMinThreads(2);
        Server server = new Server(pool);
        ServerConnector cc = new ServerConnector(server);
        cc.setPort(8080);
        server.addConnector(cc);
        server.setHandler(new HelloHandler());
        server.start();
        server.join();
    }

    // http://localhost:8080/hello
    // public static class HelloHandler extends Handler.Abstract.NonBlocking {
    public static class HelloHandler extends Handler.Abstract {
        @Override
        public boolean handle(Request request, Response response, Callback callback) {
            if (request.getHttpURI().getPath().startsWith("/hello")) {
                response.setStatus(200);
                response.write(true, ByteBuffer.wrap("hello".getBytes()), callback);
                return true;
            }
            return false;
        }
    }

    // public static void main(String[] args) throws Exception {
    // Server server = new Server(8080);
    // ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    // context.setContextPath("/");
    // server.setHandler(context);
    // // http://localhost:8080/hello
    // context.addServlet(new ServletHolder(new HelloServlet()), "/hello");
    //
    // server.start();
    // server.join();
    // }
    //
    // public static class HelloServlet extends HttpServlet {
    // @Override
    // protected void doGet(HttpServletRequest request, HttpServletResponse response)
    // throws ServletException, IOException {
    // response.setContentType("application/json");
    // response.setStatus(HttpServletResponse.SC_OK);
    // response.getWriter().println("{ \"status\": \"ok\"}");
    // }
    // }
}
