package co.com.pragma.api.log;

import co.com.pragma.commonutils.LogHelper;
import co.com.pragma.model.log.gateways.LoggerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Slf4jLoggerAdapter implements LoggerPort {

    @Override
    public void info(String message, Object... args) {
        log.info(message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        log.debug(message, args);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    @Override
    public String maskEmail(String email) {
        return LogHelper.maskEmail(email);
    }
}