package com.vncodelab.others;

import com.vncodelab.entity.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFunc {
    public static List<Category> getCateList() {
        List<Category> cateList = new ArrayList<>();
        cateList.add(new Category("Java", "Java"));
        cateList.add(new Category("Servlet", "Servlet"));
        cateList.add(new Category("JSP", "JSP"));
        cateList.add(new Category("NodeJS", "NodeJS"));
        cateList.add(new Category("Khác", "Khác"));
        return cateList;
    }

    public static List<Category> getMoreCateList() {
        List<Category> cateList = new ArrayList<>();
        cateList.add(new Category("C", "C"));
        cateList.add(new Category("C++", "C++"));

        return cateList;
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}
