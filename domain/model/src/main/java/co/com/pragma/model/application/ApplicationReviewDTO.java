package co.com.pragma.model.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationReviewDTO {
    private BigDecimal amount;
    private Integer term;
    private String email;
    private String name;
    private String loanType;
    private Double interestRate;
    private String applicationStatus;
    private BigDecimal baseSalary;
    private BigDecimal monthlyApplicationAmount;
}