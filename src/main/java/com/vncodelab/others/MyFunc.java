package com.vncodelab.others;

import com.vncodelab.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class MyFunc {
    public static List<Category> getCateList() {
        List<Category> cateList = new ArrayList<>();
        cateList.add(new Category("Java", "Java"));
        cateList.add(new Category("Servlet", "Servlet"));
        cateList.add(new Category("NodeJS", "NodeJS"));
        return cateList;
    }

    public static List<Category> getMoreCateList() {
        List<Category> cateList = new ArrayList<>();
        cateList.add(new Category("C", "C"));
        cateList.add(new Category("C++", "C++"));

        return cateList;
    }
}
