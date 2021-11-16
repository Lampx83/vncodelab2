package com.vncodelab.controller;

import com.vncodelab.entity.ischolar.Item;
import com.vncodelab.json.ischolar.Row;
import com.vncodelab.respository.PhraseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class ApiController {

    @Autowired
    PhraseRepository phraseRepository;

    @GetMapping("/phrases/all")
    public Object getAllPhrases() {
        return phraseRepository.getAllPhrases();
    }

    @GetMapping(value = "/phrases")
    @ResponseBody
    public ArrayList<Item> getPhrasesBySectionID(@RequestParam("sectionID") String sectionID) {
        return phraseRepository.getSectionByID(sectionID);
    }

}
