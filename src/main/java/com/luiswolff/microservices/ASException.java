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
