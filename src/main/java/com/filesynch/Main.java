package com.filesynch;

import com.filesynch.rmi.ServerGuiInt;
import com.filesynch.rmi.ServerRmi;
import com.filesynch.server.Server;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@SpringBootApplication
@PropertySource("classpath:application.yml")
public class Main extends SpringBootServletInitializer {
    public static Server server;
    public static ServerRmi serverRmi;
    public static ServerGuiInt serverGui;
    private static String[] stringArgs;
    private static ConfigurableApplicationContext ctx;
    public static Environment environment;
    public static String port;

    public static void main(String[] args) {
        stringArgs = args;
        try {
            Registry registry = LocateRegistry.createRegistry(8089);
            serverRmi = new ServerRmi();
            Naming.rebind("rmi://localhost:8089/gui", serverRmi);
            serverRmi.setStringArgs(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
