package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("status") // Asegúrate de que "status" sea el nombre exacto de tu tabla
public class StatusEntity {

    @Id
    private Integer statusId;

    private String name;

    private String description;
}