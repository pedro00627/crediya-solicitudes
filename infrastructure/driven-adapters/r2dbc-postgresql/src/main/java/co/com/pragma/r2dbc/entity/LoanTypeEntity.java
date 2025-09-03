package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tipo_prestamo")
public class LoanTypeEntity {

    @Id
    private Integer loanTypeId;

    private String name;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal interestRate;

    private boolean autoValidation;
}
