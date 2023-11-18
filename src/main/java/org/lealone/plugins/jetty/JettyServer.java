/*
 * Copyright Lealone Database Group.
 * Licensed under the Server Side Public License, v 1.
 * Initial Developer: zhh
 */
package org.lealone.plugins.jetty;

import java.io.File;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.lealone.common.exceptions.ConfigException;
import org.lealone.common.logging.Logger;
import org.lealone.common.logging.LoggerFactory;
import org.lealone.common.util.CaseInsensitiveMap;
import org.lealone.common.util.MapUtils;
import org.lealone.db.ConnectionInfo;
import org.lealone.db.scheduler.Scheduler;
import org.lealone.net.WritableChannel;
import org.lealone.plugins.service.http.HttpRouter;
import org.lealone.plugins.service.http.HttpServer;
import org.lealone.server.AsyncServer;

public class JettyServer extends AsyncServer<JettyServerConnection> implements HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);

    public static final int DEFAULT_PORT = 8080;

    private Map<String, String> config = new HashMap<>();
    private String webRoot;
    private String jdbcUrl;

    private String contextPath;
    private Server server;

    private boolean inited;

    @Override
    public String getType() {
        return JettyServerEngine.NAME;
    }

    @Override
    public String getWebRoot() {
        return webRoot;
    }

    @Override
    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
        config.put("web_root", webRoot);
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        config.put("jdbc_url", jdbcUrl);
        System.setProperty(org.lealone.db.Constants.JDBC_URL_KEY, jdbcUrl);
    }

    @Override
    public String getHost() {
        return super.getHost();
    }

    @Override
    public void setHost(String host) {
        config.put("host", host);
    }

    @Override
    public int getPort() {
        return super.getPort();
    }

    @Override
    public void setPort(int port) {
        config.put("port", String.valueOf(port));
    }

    @Override
    public synchronized void init(Map<String, String> config) {
        if (inited)
            return;
        config = new CaseInsensitiveMap<>(config);
        config.putAll(this.config);
        String url = config.get("jdbc_url");
        if (url != null) {
            if (this.jdbcUrl == null)
                setJdbcUrl(url);
            ConnectionInfo ci = new ConnectionInfo(url);
            if (!config.containsKey("default_database"))
                config.put("default_database", ci.getDatabaseName());
            if (!config.containsKey("default_schema"))
                config.put("default_schema", "public");
        }
        // 跟spring框架集成时在它那边初始化server
        boolean initTomcat = MapUtils.getBoolean(config, "init_server", true);
        super.init(config);
        // int schedulerCount = MapUtils.getSchedulerCount(config);

        contextPath = MapUtils.getString(config, "context_path", "/");
        webRoot = MapUtils.getString(config, "web_root", "./web");
        File webRootDir = new File(webRoot);
        if (initTomcat) {
            if (!webRootDir.exists())
                webRootDir.mkdirs();
        }

        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMinThreads(0);
        server = new Server(pool);
        JettyServerConnector c = new JettyServerConnector(server);
        c.setPort(8080);
        server.addConnector(c);
        connector = c;

        ContextHandler contextHandler = new ContextHandler(contextPath);
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirAllowed(true);
        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setBaseResourceAsString(webRootDir.getAbsolutePath());
        contextHandler.setHandler(resourceHandler);
        server.setHandler(contextHandler);

        try {
            HttpRouter router;
            String routerStr = config.get("router");
            if (routerStr != null) {
                try {
                    router = org.lealone.common.util.Utils.newInstance(routerStr);
                } catch (Exception e) {
                    throw new ConfigException("Failed to load router: " + routerStr, e);
                }
            } else {
                router = new JettyRouter();
            }
            if (initTomcat) {
                router.init(this, config);
                // server.init();
            }
        } catch (Exception e) {
            logger.error("Failed to init server", e);
        }

        inited = true;
        this.config = null;
    }

    @Override
    public synchronized void start() {
        if (isStarted())
            return;
        if (!inited) {
            init(new HashMap<>());
        }
        super.start();
        try {
            connector.setServerChannel(serverChannel);
            server.start();
            // 放到这里注册，否认调度线程执行select操作后就不能修改serverChannel.configureBlocking(false);
            super.registerAccepter(serverChannel);
        } catch (Exception e) {
            logger.error("Failed to start jetty server", e);
        }
        // ShutdownHookUtils.addShutdownHook(server, () -> {
        // try {
        // server.destroy();
        // } catch (LifecycleException e) {
        // logger.error("Failed to destroy jetty server", e);
        // }
        // });
    }

    @Override
    public synchronized void stop() {
        if (isStopped())
            return;
        super.stop();

        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                logger.error("Failed to stop jetty server", e);
            }
            server = null;
        }
        inited = false;
    }

    @Override
    protected int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    protected JettyServerConnection createConnection(WritableChannel channel, Scheduler scheduler) {
        return new JettyServerConnection(this, channel, scheduler);
    }

    private JettyServerConnector connector;
    private ServerSocketChannel serverChannel;

    public JettyServerConnector getJettyServerConnector() {
        return connector;
    }

    public ServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    @Override
    public void registerAccepter(ServerSocketChannel serverChannel) {
        this.serverChannel = serverChannel;
        // 延迟注册
        // super.registerAccepter(serverChannel);
    }
}
