package com.vncodelab.json.ischolar;

import java.util.List;

public class Data {

    public String id;
    public String topic;
    public String usernote;
    public List<Child> children;


    public String getChild(String node) {
        String s = "";
        for (Child child : children) {
            if (child.topic.equals(node)) {
                if (child.children != null)
                    for (Child child1 : child.children) {
                        s = s + child1.topic + "|";
                    }
                else
                    s = node;
            }
        }
        return s;
    }

}