package com.vncodelab.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Geo {
    private  String type;
    private  ArrayList<Double> coordinates;
}

