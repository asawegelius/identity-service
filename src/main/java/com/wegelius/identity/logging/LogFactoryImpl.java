package com.wegelius.identity.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogFactoryImpl implements LogFactory {

    @Override
    public Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    @Override
    public Logger getSecurityLogger() {
        return LoggerFactory.getLogger("com.wegelius.identity.security");
    }
}


