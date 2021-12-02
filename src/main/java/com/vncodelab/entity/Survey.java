package com.vncodelab.entity;

import com.google.cloud.Timestamp;
import lombok.Data;

import javax.management.openmbean.ArrayType;
import java.util.ArrayList;

@Data
public class Survey {

    private ArrayList<String> answers;


}
