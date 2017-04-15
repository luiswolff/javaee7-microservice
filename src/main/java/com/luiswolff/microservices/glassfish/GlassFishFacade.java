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

package com.luiswolff.microservices.glassfish;

import com.luiswolff.microservices.ASFacade;
import org.glassfish.embeddable.*;

import java.io.File;
import java.util.Arrays;

/**
 * This class should encapsulate the GlassFish-API
 *
 * Created by luis- on 11.03.2017.
 */
@SuppressWarnings("unused")
public class GlassFishFacade implements ASFacade{

    private final GlassFish glassFish;

    public GlassFishFacade() throws GFException {
        String listener = System.getProperty("javaee7.ms.WEB_LISTENER", "http")
                .equalsIgnoreCase("https") ? "https-listener" : "http-listener";
        String port     = System.getProperty("javaee7.ms.WEB_PORT"    , "8080");

        GlassFishProperties glassFishProperties = new GlassFishProperties();
        glassFishProperties.setPort(listener, Integer.parseInt(port));

        try {
            glassFish = GlassFishRuntime.bootstrap().newGlassFish(glassFishProperties);
        } catch (GlassFishException e){
            throw GFException.createInstance(e, "glassfish.exception.no_instance");
        }
    }

    public void start() throws GFException {
        try {
            GlassFish.Status status = glassFish.getStatus();
            switch (status){
                case INIT:
                    //and
                case STOPPED:
                    glassFish.start();
                    break;
                default:
                    throw GFException.createInstance("glassfish.exception.start_invalid_status", status);
            }

        } catch (GlassFishException e){
            throw GFException.createInstance(e, "glassfish.exception.start_up");
        }
    }

    public void stop(boolean dispose) throws GFException {
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
                    throw GFException.createInstance("glassfish.exception.stop_invalid_status", status);
            }
        } catch(GlassFishException e){
            throw GFException.createInstance(e, "glassfish.exception.shutdown");
        }
    }

    public void configureJDBCResource() throws GFException {
        final String className  = System.getProperty("javaee7.ms.DB_CLASS"     , "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        final String type       = System.getProperty("javaee7.ms.DB_TYPE"      , "javax.sql.DataSource");
        final String user       = System.getProperty("javaee7.ms.DB_USER"      , "sa");
        final String password   = System.getProperty("javaee7.ms.DB_PASSWD"    , "sa");
        final String database   = System.getProperty("javaee7.ms.DB_NAME"      , "test");
        final String host       = System.getProperty("javaee7.ms.DB_HOST"      , "localhost");
        final String port       = System.getProperty("javaee7.ms.DB_PORT"      , "3306");
        final String url        = System.getProperty("javaee7.ms.DB_URL"       , "");
        final String attributes = System.getProperty("javaee7.ms.DB_ATTRIBUTES", "");
        final String poolName   = System.getProperty("javaee7.ms.DB_POOL"      , "MySQLPool");
        final String jndi       = System.getProperty("javaee7.ms.DB_JNDI"      , "jdbc/__default");

        final String properties =
                (!database.isEmpty()    ? "databaseName="           + database      + ":" : "") +
                (!password.isEmpty()    ? "password="               + password      + ":" : "") +
                (!user.isEmpty()        ? "user="                   + user          + ":" : "") +
                (!host.isEmpty()        ? "serverName="             + host          + ":" : "") +
                (!port.isEmpty()        ? "port="                   + port          + ":" : "") +
                (!url.isEmpty()         ? "url="                    + url           + ":" : "") +
                (!attributes.isEmpty()  ? "connectionAttributes="   + escape(attributes)    + ":" : "");

        try {
            CommandRunner commandRunner = glassFish.getCommandRunner();

            // If a connection pool with the same name as the new one already exists, it will be deleted.
            if (Arrays.asList("DerbyPool", "__TimerPool").contains(poolName)){
                //FIXME: Is this really a good idea?
                commandRunner.run("delete-jdbc-connection-pool", "--cascade=true", poolName);
            }
            CommandResult commandResult = commandRunner.run("create-jdbc-connection-pool",
                    "--datasourceclassname", className,
                    "--restype", type,
                    "--property", properties,
                    poolName);
            checkResult(commandResult);

            // If a datasource with the same name as the new one already exists, it will be deleted.
            if (Arrays.asList("jdbc/__default", "jdbc/__TimerPool").contains(jndi)){
                commandRunner.run("delete-jdbc-resource", jndi);
            }
            commandResult = commandRunner.run("create-jdbc-resource",
                    "--connectionpoolid", poolName,
                    jndi);
            checkResult(commandResult);
        } catch (GlassFishException e){
            throw GFException.createInstance(e, "glassfish.exception.config");
        }
    }

    private String escape(String attributes) {
        return "\\;" + attributes
                .replace(";", "\\;")
                .replace("=", "\\=");
    }

    private void checkResult(CommandResult commandResult) throws GFException {
        if (commandResult.getExitStatus().equals(CommandResult.ExitStatus.FAILURE)){
            throw GFException.createInstance(commandResult.getFailureCause(), "glassfish.exception.command");
        }
    }

    public void deployApplication(File war, String[] args) throws GFException {
        try {
            Deployer deployer = glassFish.getDeployer();
            String result = deployer.deploy(war, args);
            if (result == null) {
                throw GFException.createInstance("glassfish.exception.deploy_failed");
            }
        } catch (GlassFishException e){
            throw GFException.createInstance(e, "glassfish.exception.deploy", war.getAbsolutePath());
        }
    }

}
