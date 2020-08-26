package eu.rekawek.coffeegb;

import eu.rekawek.coffeegb.serial.SerialPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public class LoggerFactory {

    public static Logger getLogger(Class<?> clazz){
        return LogManager.getLogger(clazz.getSimpleName());
    }
}
