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

/**
 *
 *
 * Created by luis- on 11.03.2017.
 */
public class ASException extends Exception {

    final int exit;

    public ASException(String message) {
        this(1, message);
    }

    public ASException(String message, Throwable cause) {
        this(1, message, cause);
    }

    public ASException(int exit, String message) {
        super(message);
        this.exit = exit;
    }

    public ASException(int exit, String message, Throwable cause) {
        super(message, cause);
        this.exit = exit;
    }
}
