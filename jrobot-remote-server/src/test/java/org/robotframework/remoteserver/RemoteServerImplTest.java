package org.robotframework.remoteserver;

import java.util.Map;
import org.robotframework.remoteserver.testlibraries.StaticOne;
import org.robotframework.remoteserver.testlibraries.StaticTwo;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.robotframework.remoteserver.RemoteLibraryClient.runKeyword;

public class RemoteServerImplTest {
    RemoteServerImpl server;

    @Test
    public void allowRemoteStop() throws Exception {
        Assert.assertEquals(server.getAllowStop(), true);
        server.setAllowStop(false);
        server.putLibrary("/", new StaticOne());
        server.start();
        String result = (String) runKeyword("/", "stop_remote_server").get("output");
        Assert.assertEquals(result, "This Robot Framework remote server does not allow stopping");
        Thread.sleep(2000);
        result = (String) runKeyword("/", "getName").get("return");
        Assert.assertEquals(result, "StaticOne");
        server.setAllowStop(true);
        // actually stopping the server through the keyword will exit the JVM
        // executing the acceptance tests will stop the server
    }

    @Test
    public void serverIsRestartable() throws Exception {
        server.putLibrary("/1", new StaticOne());
        server.start();
        String result = (String) runKeyword("/1", "getName").get("return");
        Assert.assertEquals(result, "StaticOne");
        server.stop();
        server.putLibrary("/2", new StaticTwo());
        server.start();
        result = (String) runKeyword("/2", "getName").get("return");
        Assert.assertEquals(result, "StaticTwo");
        server.stop();
    }

    @Test
    public void putLibrariesAfterStarting() throws Exception {
        server.start();
        server.putLibrary("/1", new StaticOne());
        String result = (String) runKeyword("/1", "getName").get("return");
        Assert.assertEquals(result, "StaticOne");
    }

    @Test
    public void ephemeralPort() throws Exception {
        RemoteServerImpl server = new RemoteServerImpl();
        Assert.assertEquals(server.getPort(), -1);
        server.putLibrary("/", new StaticOne());
        server.start();
        int port = server.getPort();
        String result = (String) runKeyword(port, "/", "getName").get("return");
        Assert.assertEquals(result, "StaticOne");
        server.stop();
        Assert.assertEquals(server.getPort(), -2);
    }

    @Test
    public void addTwoLibrariesOnDifferentPorts() {
        server.addLibrary(StaticOne.class, 8270);
        Exception ex = null;
        try {
            server.addLibrary(StaticOne.class, 8271);
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertEquals(ex.getMessage(),
                "Serving on multiple ports is no longer supported. Please use putLibrary with different paths instead.");
    }

    @Test
    public void mixAddLibraryWithSetPort() {
        server.setPort(8270);
        Exception ex = null;
        try {
            server.addLibrary(StaticOne.class, 8271);
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertEquals(ex.getMessage(),
                "Serving on multiple ports is no longer supported. Please use putLibrary with different paths instead.");
    }

    @Test
    public void libraryMap() {
        server.putLibrary("/", new StaticOne());
        Assert.assertTrue(server.getLibraryMap().containsKey("/"));
    }

    @Test
    public void onlyRequiredEntriesInResultsWhenPassed() throws Exception {
        server.putLibrary("/1", new StaticOne());
        server.start();
        Map<?, ?> results = runKeyword("/1", "noReturnValue");
        Assert.assertEquals(results.get("status"), "PASS");
        Assert.assertEquals(results.size(), 1);
    }

    @BeforeMethod
    public void setup() throws Exception {
        server = new RemoteServerImpl();
        server.setPort(8270);
    }

    @AfterMethod
    public void cleanup() throws InterruptedException {
        try {
            server.stop();
        } catch (Throwable t) {
            // ignore
        }
    }
}