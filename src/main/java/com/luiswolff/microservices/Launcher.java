package com.luiswolff.microservices;

import java.io.File;
import java.util.NoSuchElementException;
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
 * @author Luis-Manuel Wolff Heredia <a href='mailto:l.wolff@bioeng.de'>mailto:l.wolff@bioeng.de</a>
 */
public class Launcher {

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

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

        LOGGER.info("Prepare starting of GlassFish-Server");

        if (args.length == 0) {
            LOGGER.warning("There was no WAR-File to deploy submitted. EXITING!");
            System.exit(1);
        }

        File war = new File(args[0]);
        if (!war.exists()) {
            LOGGER.warning("The file submitted dose not exists. EXITING!");
            System.exit(1);
        }
        String[] deploymentArgs = new String[args.length - 1];
        System.arraycopy(args, 1, deploymentArgs, 0, args.length - 1);

        readEnv("WEB_LISTENER"  , "javaee7.ms.WEB_LISTENER");
        readEnv("WEB_PORT"      , "javaee7.ms.WEB_PORT");
        readEnv("DB_CLASS"      , "javaee7.ms.DB_CLASS");
        readEnv("DB_TYPE"       , "javaee7.ms.DB_TYPE");
        readEnv("DB_USER"       , "javaee7.ms.DB_USER");
        readEnv("DB_PASSWD"     , "javaee7.ms.DB_PASSWD");
        readEnv("DB_NAME"       , "javaee7.ms.DB_NAME");
        readEnv("DB_HOST"       , "javaee7.ms.DB_HOST");
        readEnv("DB_PORT"       , "javaee7.ms.DB_PORT");
        readEnv("DB_POOL"       , "javaee7.ms.DB_POOL");
        readEnv("DB_JNDI"       , "javaee7.ms.DB_JNDI");

        LOGGER.info("Starting GlassFish-Server");

        try {
            facade = ASFacade.getInstance();
            facade.start();
            facade.configureJDBCResource();
            facade.deployApplication(war, deploymentArgs);
        } catch (ASException e) {
            LOGGER.log(Level.SEVERE, "Error on start up of application server", e);
            System.exit(e.exit);
        }

        LOGGER.info("GlassFish-Server and deployment artifact started successfully. Type \"exit\" or \"^C\" to halt this process");
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
            } while (!input.equalsIgnoreCase("exit"));

            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            shutdown(false);
        });
        inputListener.setName("AS-Console-Listener");
        inputListener.setDaemon(true);
        inputListener.start();
    }

    private static void shutdown(){
        shutdown(true);
    }

    private static void shutdown(boolean shutdownInProgress){
        LOGGER.info("Received stop command");
        try {
            facade.stop(true);
            if (!shutdownInProgress){
                System.exit(0);
            }
        } catch (ASException e) {
            LOGGER.log(Level.SEVERE, "Could not stop application server correctly", e);
            Runtime.getRuntime().halt(e.exit);
        }
    }

    private static void restart(){
        //TODO: This method dose not restart the server. Why?
        LOGGER.info("Restart application server");
        try {
            facade.stop(false);
            facade.start();
        } catch (ASException e){
            LOGGER.log(Level.SEVERE, "Exception on restarting server", e);
            Runtime.getRuntime().halt(1);
        }
    }

    private static void readEnv(String env, String key){
        String value = System.getenv(env);
        if (value != null){
            System.setProperty(key, value);
        }
    }
}
