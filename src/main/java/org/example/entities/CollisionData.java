package org.example.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CollisionData {
    private final boolean status;
    private final Long id;

    public CollisionData(boolean b, Long id) {
        this.status = b;
        this.id = id;
    }
}
