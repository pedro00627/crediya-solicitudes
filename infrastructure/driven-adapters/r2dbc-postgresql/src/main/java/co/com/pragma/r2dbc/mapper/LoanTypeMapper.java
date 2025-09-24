package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.r2dbc.entity.LoanTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class LoanTypeMapper {

    public LoanType toDomain(final LoanTypeEntity entity) {
        if (null == entity) {
            return null;
        }
        return new LoanType(
                entity.getLoanTypeId(),
                entity.getName(),
                entity.getMinAmount(),
                entity.getMaxAmount(),
                entity.getInterestRate(),
                entity.isAutoValidation()
        );
    }

    public LoanTypeEntity toEntity(final LoanType domain) {
        if (null == domain) {
            return null;
        }
        return new LoanTypeEntity(
                domain.getLoanTypeId(),
                domain.getName(),
                domain.getMinAmount(),
                domain.getMaxAmount(),
                domain.getInterestRate(),
                domain.isAutoValidation()
        );
    }
}