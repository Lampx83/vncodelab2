package com.vncodelab.entity;

import lombok.Data;

@Data
public class StudentInfo {
    Double timeDuration = 0.0;
    String email;
    String name;
    int totalAnswer = 0;
}
