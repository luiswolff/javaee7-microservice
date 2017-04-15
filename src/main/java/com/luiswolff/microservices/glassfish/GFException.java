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

import com.luiswolff.microservices.ASException;
import com.luiswolff.microservices.utils.Constants;

import java.text.MessageFormat;
import java.util.ResourceBundle;

class GFException extends ASException{

    private static ResourceBundle messages = ResourceBundle.getBundle(Constants.LOGGING_BUNDLE);

    private GFException(int exit, String message) {
        super(exit, message);
    }

    private GFException(int exit, String message, Throwable cause) {
        super(exit, message, cause);
    }

    static GFException createInstance(String message, Object ... params){
        return createInstance(null, message, params);
    }

    static GFException createInstance(Throwable cause, String message, Object ... params){
        message = messages.getString(message);
        message = MessageFormat.format(message, params);
        if (cause != null){
            return new GFException(2, message, cause);
        } else {
            return new GFException(2, message);
        }
    }
}
