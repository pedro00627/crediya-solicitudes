package co.com.pragma.api.util;

import com.github.pedro00627.commonlogging.LogHelper;
import co.com.pragma.model.security.gateways.DataMaskerPort;
import org.springframework.stereotype.Component;

@Component
public class DataMaskerAdapter implements DataMaskerPort {

    @Override
    public String maskEmail(String email) {
        return LogHelper.maskEmail(email);
    }
}