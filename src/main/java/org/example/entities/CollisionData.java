package org.example.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CollisionData {
    private final boolean status;
    private final int id;

    public CollisionData(boolean b, int id) {
        this.status = b;
        this.id = id;
    }
}
