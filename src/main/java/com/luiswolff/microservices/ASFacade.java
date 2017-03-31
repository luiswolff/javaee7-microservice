package com.luiswolff.microservices;

import java.io.File;

/**
 * An facade interface for configuring an embedded application server.
 *
 * Created by luis-manuel on 31.03.17.
 */
public interface ASFacade {

    void start() throws ASException;

    void stop(boolean dispose) throws ASException;

    void configureJDBCResource() throws ASException;

    void deployApplication(File war, String[] args) throws ASException;

    static ASFacade getInstance(){
        String impl = System.getProperty("javaee7.ms.AS", "com.luiswolff.microservices.glassfish.GlassFishFacade");

        try {
            Class<?> clazz = Class.forName(impl);
            return (ASFacade) clazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
