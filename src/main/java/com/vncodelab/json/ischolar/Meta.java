package com.vncodelab.json.ischolar;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Meta {
    public String id;
    public String title;
    public String teacher_name;
    public String student_name;
    public ArrayList<Author> authors;
    public String version;

    public Integer number_of_sample;
    public String language;

    public String getListAuthorName() {
        return authors
                .stream()
                .map(a -> String.valueOf(a.name))
                .collect(Collectors.joining(", "));
    }

    public String getListAuthorEmail() {
        return authors
                .stream()
                .map(a -> String.valueOf(a.email))
                .collect(Collectors.joining(", "));
    }

    public String getListAuthorAffiliation() {
        return authors
                .stream()
                .map(a -> String.valueOf(a.affiliation))
                .collect(Collectors.joining(", "));
    }
}