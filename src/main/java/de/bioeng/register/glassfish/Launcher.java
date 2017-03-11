package de.bioeng.register.glassfish;

import org.glassfish.embeddable.*;

import java.io.File;
import java.util.Map;
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

        final Logger logger = Logger.getLogger(Launcher.class.getName());
        final Map<String, String> env = System.getenv();
        logger.info("Prepare starting of GlassFish-Server");

        if (args.length == 0) {
            logger.warning("There was no WAR-File to deploy submitted. EXITING!");
            System.exit(1);
        }

        File war = new File(args[0]);
        if (!war.exists()) {
            logger.warning("The file submitted dose not exists. EXITING!");
            System.exit(1);
        }
        String[] deploymentArgs = new String[args.length - 1];
        System.arraycopy(args, 1, deploymentArgs, 0, args.length - 1);

        logger.info("Starting GlassFish-Server");

        GlassFishProperties glassFishProperties = new GlassFishProperties();

        String protocol = env.getOrDefault("LISTENER", "http");
        String listener = protocol.equalsIgnoreCase("https") ? "https-listener" : "http-listener";
        String port = env.getOrDefault("LISTENER_PORT", "8080");
        glassFishProperties.setPort(listener, Integer.parseInt(port));

        final GlassFish glassFish;
        try {
            glassFish = GlassFishRuntime.bootstrap().newGlassFish(glassFishProperties);
            glassFish.start();

            CommandRunner commandRunner = glassFish.getCommandRunner();
            CommandResult commandResult = commandRunner.run("create-jdbc-connection-pool",
                    "--datasourceclassname", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource",
                    "--restype", "javax.sql.DataSource",
                    "--property", "DatabaseName=auth:Password=s3cret:User=registerAuth:ServerName=localhost:Port=3306",
                    "AuthDBConnectionPool");
            if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)){
                throw commandResult.getFailureCause();
            }

            commandResult = commandRunner.run("create-jdbc-resource",
                    "--connectionpoolid", "AuthDBConnectionPool",
                    "jdbc/register/auth");
            if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)){
                throw commandResult.getFailureCause();
            }

            logger.info("Deploy web app " + war.getAbsolutePath());
            Deployer deployer = glassFish.getDeployer();
            deployer.deploy(war, deploymentArgs);
            if (deployer.getDeployedApplications().size() == 0) {
                throw new GlassFishException("Deploying of application failed");
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error on starting glassfish server", e);
            System.exit(1);
        }
    }
}
