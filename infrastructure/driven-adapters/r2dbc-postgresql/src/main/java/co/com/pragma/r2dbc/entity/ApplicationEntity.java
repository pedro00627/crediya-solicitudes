package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "solicitud", schema = "solicitudes")
public class ApplicationEntity {
    @Id
    @Column("id_solicitud")
    java.util.UUID applicationId;

    @Column("documento_identidad")
    String documentId;

    @Column("monto")
    java.math.BigDecimal amount;
    @Column("plazo")
    int term;

    @Column("email")
    String email;

    @Column("id_estado")
    int statusId;

    @Column("id_tipo_prestamo")
    int loanTypeId;
}
