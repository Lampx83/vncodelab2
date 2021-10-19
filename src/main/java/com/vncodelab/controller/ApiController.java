package com.vncodelab.controller;

import com.vncodelab.json.ischolar.Row;
import com.vncodelab.respository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class ApiController {

    @Autowired
    SectionRepository sectionRepository;

    @GetMapping("/sections")
    public Object getSections() {
        return sectionRepository.getAllSection();
    }

    @RequestMapping(value = "/section", method = RequestMethod.POST)
    @ResponseBody
    public Row getSectionByID(@RequestBody String sectionID) {
        return sectionRepository.getSectionByID(sectionID);
    }


}
