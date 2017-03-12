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

    GlassFishFacade() throws ASException {
        String listener         = System.getProperty(Const.WEB_LISTENER , "http-listener");
        String port             = System.getProperty(Const.WEB_PORT     , "8080");

        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setPort(listener, Integer.parseInt(port));

        try {
            glassFish = GlassFishRuntime.bootstrap().newGlassFish(glassFishProperties);
        } catch (GlassFishException e){
            throw new ASException("Could not instantiate GlassFish-Server", e);
        }
    }

    void start() throws ASException {
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

    void stop(boolean dispose) throws ASException {
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

    void configureJDBCResource() throws ASException {
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
            throw new ASException("Could not configurate GlassFish-Server", e);
        }
    }

    private void checkResult(CommandResult commandResult) throws ASException {
        if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)){
            throw new ASException("Could not execute command",commandResult.getFailureCause());
        }
    }

    void deployApplication(File war, String[] args) throws ASException {
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
