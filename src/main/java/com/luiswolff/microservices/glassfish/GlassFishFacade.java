package com.luiswolff.microservices.glassfish;

import com.luiswolff.microservices.ASException;
import com.luiswolff.microservices.ASFacade;
import org.glassfish.embeddable.*;

import java.io.File;

/**
 * This class should encapsulate the GlassFish-API
 *
 * Created by luis- on 11.03.2017.
 */
public class GlassFishFacade implements ASFacade{

    private final GlassFish glassFish;

    public GlassFishFacade() throws ASException {
        String listener = System.getProperty("javaee7.ms.WEB_LISTENER", "http")
                .equalsIgnoreCase("https") ? "https-listener" : "http-listener";
        String port     = System.getProperty("javaee7.ms.WEB_PORT"    , "8080");

        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setPort(listener, Integer.parseInt(port));

        try {
            glassFish = GlassFishRuntime.bootstrap().newGlassFish(glassFishProperties);
        } catch (GlassFishException e){
            throw new ASException("Could not instantiate GlassFish-Server", e);
        }
    }

    public void start() throws ASException {
        try {
            GlassFish.Status status = glassFish.getStatus();
            switch (status){
                case INIT:
                    //and
                case STOPPED:
                    glassFish.start();
                    break;
                default:
                    throw new ASException("GlassFish-Server has invalid state for starting: " + status);
            }

        } catch (GlassFishException e){
            throw new ASException("Could not start up GlassFish-Server", e);
        }
    }

    public void stop(boolean dispose) throws ASException {
        try {
            GlassFish.Status status = glassFish.getStatus();
            switch (status){
                case STARTED:
                    glassFish.stop();
                    if (dispose){
                        glassFish.dispose();
                    }
                    break;
                default:
                    throw new ASException(2, "GlassFish-Server has invalid state for stopping: " + status);
            }
        } catch(GlassFishException e){
            throw new ASException(2, "Could not shutdown GlassFish-Server", e);
        }
    }

    public void configureJDBCResource() throws ASException {
        final String className  = System.getProperty("javaee7.ms.DB_CLASS"     , "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        final String type       = System.getProperty("javaee7.ms.DB_TYPE"      , "javax.sql.DataSource");
        final String user       = System.getProperty("javaee7.ms.DB_USER"      , "sa");
        final String password   = System.getProperty("javaee7.ms.DB_PASSWD"    , "sa");
        final String database   = System.getProperty("javaee7.ms.DB_NAME"      , "test");
        final String host       = System.getProperty("javaee7.ms.DB_HOST"      , "localhost");
        final String port       = System.getProperty("javaee7.ms.DB_PORT"      , "3306");
        final String poolName   = System.getProperty("javaee7.ms.DB_POOL"      , "RegisterConnectionPool");
        final String jndi       = System.getProperty("javaee7.ms.DB_JNDI"      , "jdbc/__register");

        final String properties
                = "DatabaseName="   + database  + ":"
                + "Password="       + password  + ":"
                + "User="           + user      + ":"
                + "ServerName="     + host      + ":"
                + "Port="           + port      + ":";

        try {
            CommandRunner commandRunner = glassFish.getCommandRunner();
            CommandResult commandResult = commandRunner.run("create-jdbc-connection-pool",
                    "--datasourceclassname", className,
                    "--restype", type,
                    "--property", properties,
                    poolName);
            checkResult(commandResult);

            commandResult = commandRunner.run("create-jdbc-resource",
                    "--connectionpoolid", poolName,
                    jndi);
            checkResult(commandResult);
        } catch (GlassFishException e){
            throw new ASException("Could not configurate GlassFish-Server", e);
        }
    }

    private void checkResult(CommandResult commandResult) throws ASException {
        if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)){
            throw new ASException("Could not execute command",commandResult.getFailureCause());
        }
    }

    public void deployApplication(File war, String[] args) throws ASException {
        try {
            Deployer deployer = glassFish.getDeployer();
            deployer.deploy(war, args);
            if (deployer.getDeployedApplications().size() == 0) {
                throw new ASException("Deploying of application failed", null);
            }
        } catch (GlassFishException e){
            throw new ASException("Could not deploy artifact " + war.getAbsolutePath(), e);
        }
    }

}
