package co.com.pragma.model.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    private UUID applicationId;
    private String documentId;
    private BigDecimal amount;
    private int term;
    private String email;
    private int statusId;
    private int loanTypeId;
}
