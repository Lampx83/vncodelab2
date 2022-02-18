package com.vncodelab.model.ischolar;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;

@Data
public class Column {
    private int data;
    private String name;
    private String searchable;
    private String orderable;
    private Search search;
}
