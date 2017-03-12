package de.bioeng.register.glassfish;

/**
 * Created by luis- on 11.03.2017.
 */
class ASException extends Exception{

    final int exit;

    ASException(String message){
       this(1,message);
    }

    ASException(String message, Throwable cause) {
        this(1,message, cause);
    }

    ASException(int exit, String message){
        super(message);
        this.exit = exit;
    }

    ASException(int exit, String message, Throwable cause){
        super(message, cause);
        this.exit = exit;
    }
}
