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

import java.io.*;
import java.util.Properties;

/**
 * This class provides access to an Java EE archive and has utility methods for reading resources.
 *
 * Created by luis- on 05.04.2017.
 */
class ResourceManager {

    private File archive;

    void findArchive(String archivePath) throws ArchiveNotExistsException{
        archive = new File(archivePath);
        if (!archive.exists()){
            throw new ArchiveNotExistsException();
        }
    }

    File getArchive() {
        if (archive == null){
            throw new IllegalStateException();
        }
        return archive;
    }

    static String readResource(String resource) {
        InputStream in = ResourceManager.class.getResourceAsStream(resource);
        if (in == null){
            throw new RuntimeException("Could not find Resource " + resource);
        }
        InputStreamReader reader = new InputStreamReader(in);
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[1024];
        int i;
        try {
            while ((i = reader.read(buffer)) > 0){
                out.append(buffer, 0, i);
            }
        } catch (IOException ioe){
            throw new RuntimeException(ioe);
        }
        return out.toString();
    }

    static Properties loadProperties(String file, boolean resource) {
        Properties properties = new Properties();
        try {
            InputStream in;
            if (!resource){
                in = new FileInputStream(file);
            } else {
                in = ResourceManager.class.getResourceAsStream(file);
                if (in == null){
                    throw new IllegalStateException();
                }
            }
            properties.load(in);
        } catch (IOException e) {
            properties.clear();
        }
        return properties;
    }
}
