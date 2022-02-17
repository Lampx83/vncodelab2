package com.vncodelab.model.ischolar;

import com.vncodelab.entity.ischolar.Journal;
import lombok.Data;

import java.util.ArrayList;

@Data
public class JournalList {
    int draw;
    int recordsTotal;
    int recordsFiltered;
    ArrayList<ArrayList<String>> data = new ArrayList<>();
}
