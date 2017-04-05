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
