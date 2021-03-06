/*
 * Java-EE7-Microservice
 * Copyright (C) 2017  Luis-Manuel Wolff Heredia
 *
 * This file is part of Java-EE7-Microservice.
 *
 * Java-EE7-Microservice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java-EE7-Microservice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or  FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java-EE7-Microservice. It not, see <http://www.gnu.org/licenses/>.
 */

package com.luiswolff.microservices;

import com.luiswolff.microservices.utils.Constants;

import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This class will be called at start up by the java virtual machine. It's purpose is to start an embedded version
 * of the GlassFish-Server and to deploy the submitted web application. The artifact should be an valid archive file
 * that is supported by the GlassFish-Web-Profile. This could be files with the following extensions:</p>
 * <ul>
 * <li>*.war</li>
 * <li>*.jar</li>
 * </ul>
 * <p>ATTENTION: This application is designed to deploy only one artifact.</p>
 *
 * @author Luis-Manuel Wolff Heredia
 */
public class Launcher {

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName(), Constants.LOGGING_BUNDLE);

    private static ResourceManager rm = new ResourceManager();

    private static ASFacade facade;

    /**
     * <p>This method will try to start an instance of an GlassFish-Server. It will first check, weather the caller
     * process has submitted an file reference to an deployment artifact. If the is not the case, this process will be
     * immediately canceled.</p>
     *
     * <p>When the deployment artifact is given, this method will start the GlassFish-Server it self. This includes the
     * configuration of the web listener and the port to use. After the server is start the artifact will be deployed
     * and checked that it is running.</p>
     *
     * @param args The deployment artifact as first value followed by optional deployment parameters
     */
    public static void main(String[] args) {
        loadEnv();
        printLogo();

        LOGGER.info("server.prepare");

        if (args.length == 0) {
            LOGGER.warning("server.deployment.undefined");
            System.exit(1);
        }

        try {
            rm.findArchive(args[0]);
        } catch (ArchiveNotExistsException e) {
            LOGGER.warning("server.deployment.not_found");
            System.exit(1);
        }
        String[] deploymentArgs = new String[args.length - 1];
        System.arraycopy(args, 1, deploymentArgs, 0, args.length - 1);

        LOGGER.info("server.starting");

        try {
            facade = ASFacade.getInstance();
            facade.bootstrap(rm.getArchive(), deploymentArgs);
        } catch (ASException e) {
            LOGGER.log(Level.SEVERE, "server.starting.exception", e);
            System.exit(e.exit);
        }

        LOGGER.log(Level.INFO, "server.running", Constants.EXIT_COMMAND);
        final Thread shutdownHook = new Thread(Launcher::shutdown);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        final Thread inputListener = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String input;
            do {
                try {
                    input = scanner.next(".*");
                } catch (NoSuchElementException e){
                    //Input stream was closed maybe by shutdown hook.
                    return;
                }
                if (input.equalsIgnoreCase("restart")){
                    restart();
                }
            } while (!input.equalsIgnoreCase(Constants.EXIT_COMMAND));

            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            shutdown(false);
        });
        inputListener.setName("AS-Console-Listener");
        inputListener.setDaemon(true);
        inputListener.start();
    }

    private static void printLogo() {
        Properties systemProps = System.getProperties();
        if (!systemProps.containsKey("batch")){
            String notice = ResourceManager.readResource("notice");
            System.out.println(notice);
            System.out.println();
            System.out.println();
        }

        boolean abort = false;
        boolean printHeader = true;

        if (systemProps.containsKey("show.c")){
            printGPLHeader();
            printHeader = false;
            String conditions = ResourceManager.readResource("conditions");
            System.out.println(conditions);
            System.out.println();
            System.out.println();
            abort = true;
        }

        if (systemProps.containsKey("show.w")){
            if (printHeader){
                printGPLHeader();
            }
            String warranty = ResourceManager.readResource("warranty");
            System.out.println(warranty);
            System.out.println();
            System.out.println();
            abort = true;
        }

        if (abort){
            System.exit(0);
        }

    }

    private static void printGPLHeader() {
        String gpl = ResourceManager.readResource("gpl");
        System.out.println(gpl);
        System.out.println();
    }

    private static void shutdown(){
        shutdown(true);
    }

    private static void shutdown(boolean shutdownInProgress){
        LOGGER.info("server.stopping");
        try {
            facade.stop(true);
            LOGGER.info("server.stopped");
            if (!shutdownInProgress){
                System.exit(0);
            }
        } catch (ASException e) {
            LOGGER.log(Level.SEVERE, "server.stopping.exception", e);
            Runtime.getRuntime().halt(e.exit);
        }
    }

    private static void restart(){
        //TODO: This method dose not restart the server. Why?
        LOGGER.info("server.restarting");
        try {
            facade.stop(false);
            facade.start();
        } catch (ASException e){
            LOGGER.log(Level.SEVERE, "server.restarting.exception", e);
            Runtime.getRuntime().halt(1);
        }
    }

    private static void loadEnv(){
        Properties envMap = ResourceManager.loadProperties("envMap.properties", false);
        if (envMap.isEmpty()){
            envMap = ResourceManager.loadProperties("defaultEnvMap.properties", true);
        }
        for (String env : envMap.stringPropertyNames()) {
            String systemKey = envMap.getProperty(env);
            String value = System.getenv(env);
            if (value != null) {
                System.setProperty(systemKey, value);
            }
        }
    }
}
