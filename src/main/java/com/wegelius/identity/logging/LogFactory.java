package com.wegelius.identity.logging;

import org.slf4j.Logger;

public interface LogFactory {
    Logger getLogger(Class<?> clazz);
    Logger getSecurityLogger();
}

