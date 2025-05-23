package com.wegelius.identity_service.shared;

public interface ApplicationLogger {
    void info(String message);
    void warn(String message);
    void error(String message, Throwable throwable);
    void debug(String message);
}
