package co.com.pragma.model.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    private int statusId;
    private String name;
    private String description;
}
