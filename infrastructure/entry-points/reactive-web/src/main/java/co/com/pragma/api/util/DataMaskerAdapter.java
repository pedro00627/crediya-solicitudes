package co.com.pragma.api.util;

import co.com.pragma.commonutils.LogHelper;
import co.com.pragma.model.security.gateways.DataMaskerPort;
import org.springframework.stereotype.Component;

@Component
public class DataMaskerAdapter implements DataMaskerPort {

    @Override
    public String maskEmail(String email) {
        return LogHelper.maskEmail(email);
    }
}