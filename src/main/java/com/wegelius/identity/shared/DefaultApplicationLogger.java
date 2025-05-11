package com.wegelius.identity.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultApplicationLogger implements ApplicationLogger {

    private final Logger logger = LoggerFactory.getLogger(DefaultApplicationLogger.class);

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }
}
