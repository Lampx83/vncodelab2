package com.vncodelab.model.ischolar;

import lombok.Data;

@Data
public class Order {
    private int column;
    private String dir;

    public int getOrder() {
        if (dir.equals("asc"))
            return 1;
        else return -1;
    }

}
