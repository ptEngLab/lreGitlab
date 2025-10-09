package com.lre.actions.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

public class LogHelper {
    private static boolean isInitialized = false;

    /**
     * Configure logback logging to file (and optionally console).
     *
     * @param logLevelStr          Log level string (DEBUG, INFO, WARN, ERROR)
     * @param enableConsoleLogging true to also log to console
     */
    public static synchronized void setup(String logLevelStr, boolean enableConsoleLogging) {
        String logFileName = CommonUtils.createLogFileName();

        if (isInitialized) return;

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

        Level logLevel;
        try {
            logLevel = Level.valueOf(logLevelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("[LogHelper] Invalid log level: " + logLevelStr + " → falling back to INFO");
            logLevel = Level.INFO;
        }

        rootLogger.setLevel(logLevel);

        if (!isInitialized) rootLogger.detachAndStopAllAppenders();

        if (isAppenderMissing(rootLogger, "FileAppender"))
            rootLogger.addAppender(createFileAppender(context, logFileName));

        if (enableConsoleLogging && isAppenderMissing(rootLogger, "ConsoleAppender")) {
            rootLogger.addAppender(createConsoleAppender(context));
        }

        // Silence noisy loggers
        context.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.ERROR);

        isInitialized = true;
    }

    private static boolean isAppenderMissing(Logger logger, String appenderName) {
        return logger.getAppender(appenderName) == null;
    }

    private static FileAppender<ILoggingEvent> createFileAppender(LoggerContext context, String logFilePath) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%date{yyyy-MM-dd HH:mm:ss} %-5level %-40(%logger{1}:%L) - %msg%n");
        encoder.start();
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("FileAppender");
        fileAppender.setFile(logFilePath);
        fileAppender.setEncoder(encoder);
        fileAppender.setAppend(false);
        fileAppender.start();
        return fileAppender;
    }

    private static ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%date{yyyy-MM-dd HH:mm:ss} %-5level %-40(%logger{1}:%L) - %msg%n");
        encoder.start();
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("ConsoleAppender");
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        return consoleAppender;
    }
}
