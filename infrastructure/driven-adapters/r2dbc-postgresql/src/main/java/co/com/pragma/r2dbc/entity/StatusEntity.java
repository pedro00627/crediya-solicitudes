package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "estados", schema = "solicitudes")
public class StatusEntity {

    @Id
    @Column("id_estado")
    private Integer statusId;

    @Column("nombre")
    private String name;

    @Column("descripcion")
    private String description;
}