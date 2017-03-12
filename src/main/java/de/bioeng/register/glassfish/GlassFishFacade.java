package de.bioeng.register.glassfish;

import org.glassfish.embeddable.*;

import java.io.File;

/**
 * This class should encapsulate the GlassFish-API
 *
 * Created by luis- on 11.03.2017.
 */
class GlassFishFacade {

    private final GlassFish glassFish;

    GlassFishFacade() throws StartUpException{
        String listener         = System.getProperty(Const.WEB_LISTENER , "http-listener");
        String port             = System.getProperty(Const.WEB_PORT     , "8080");

        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setPort(listener, Integer.parseInt(port));

        try {
            glassFish = GlassFishRuntime.bootstrap().newGlassFish(glassFishProperties);
        } catch (GlassFishException e){
            throw new StartUpException("Could not instantiate GlassFish-Server", e);
        }
    }

    void start() throws StartUpException{
        try {
            glassFish.start();
        } catch (GlassFishException e){
            throw new StartUpException("Could not start up GlassFish-Server", e);
        }
    }

    void configureJDBCResource() throws StartUpException{
        final String className  = System.getProperty(Const.DB_CLASSNAME , "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        final String type       = System.getProperty(Const.DB_TYPE      , "javax.sql.DataSource");
        final String user       = System.getProperty(Const.DB_USER      , "sa");
        final String password   = System.getProperty(Const.DB_PASSWD    , "sa");
        final String database   = System.getProperty(Const.DB_NAME      , "test");
        final String host       = System.getProperty(Const.DB_HOST      , "localhost");
        final String port       = System.getProperty(Const.DB_PORT      , "3306");
        final String poolName   = System.getProperty(Const.DB_POOL      , "RegisterConnectionPool");
        final String jndi       = System.getProperty(Const.DB_JNDI      , "jdbc/__register");

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
            throw new StartUpException("Could not configurate GlassFish-Server", e);
        }
    }

    private void checkResult(CommandResult commandResult) throws StartUpException{
        if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)){
            throw new StartUpException("Could not execute command",commandResult.getFailureCause());
        }
    }

    void deployApplication(File war, String[] args) throws StartUpException {
        try {
            Deployer deployer = glassFish.getDeployer();
            deployer.deploy(war, args);
            if (deployer.getDeployedApplications().size() == 0) {
                throw new StartUpException("Deploying of application failed", null);
            }
        } catch (GlassFishException e){
            throw new StartUpException("Could not deploy artifact " + war.getAbsolutePath(), e);
        }
    }

}
