package com.vncodelab.model.ischolar;

import lombok.Data;

import java.util.*;
@Data
public class DataTableRequest {
    private int draw;
    private List<Column> columns;
    private List<Order> order;
    private int start;
    private int length;
    private Search search;
    private String empty;
}
