package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.status.Status;
import co.com.pragma.r2dbc.entity.StatusEntity;
import org.springframework.stereotype.Component;

@Component
public class StatusMapper {

    public Status toDomain(StatusEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Status(
                entity.getStatusId(),
                entity.getName(),
                entity.getDescription()
        );
    }

    public StatusEntity toEntity(Status domain) {
        if (domain == null) {
            return null;
        }
        return new StatusEntity(
                domain.getStatusId(),
                domain.getName(),
                domain.getDescription()
        );
    }
}