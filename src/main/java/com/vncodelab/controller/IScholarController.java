package com.vncodelab.controller;

import com.vncodelab.entity.ischolar.Item;
import com.vncodelab.json.ischolar.Jsmind;
import com.vncodelab.json.ischolar.Row;
import com.vncodelab.respository.PhraseRepository;
import com.vncodelab.service.ischolar.GenDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class IScholarController {

    @Autowired
    PhraseRepository phraseRepository;

    @Autowired
    GenDocService genDocService;

    @GetMapping("/phrases/all")
    public Object getAllPhrases() {
        return phraseRepository.getAllPhrases();
    }

    //Example
    //https://localhost//api/v1/phrases?sectionID=Abstract
    @GetMapping(value = "/phrases")
    @ResponseBody
    public ArrayList<Item> getPhrasesBySectionID(@RequestParam("sectionID") String sectionID) {
        return phraseRepository.getSectionByID(sectionID);
    }


    @RequestMapping(value = "/mapgendoc", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<InputStreamResource> mapGenDoc(@RequestBody Jsmind jsmind) {
        return genDocService.genDoc(jsmind);
    }

}
