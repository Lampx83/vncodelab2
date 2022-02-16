package com.vncodelab.controller;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.vncodelab.entity.ischolar.Item;
import com.vncodelab.json.ischolar.Jsmind;
import com.vncodelab.respository.PhraseRepository;
import com.vncodelab.service.ischolar.GenDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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



    @RequestMapping(
            value = "/test",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    String getBeers() {
        return "{\n" +
                "  \"draw\": 0,\n" +
                "  \"recordsTotal\": 9,\n" +
                "  \"recordsFiltered\": 9,\n" +
                "  \"data\": [\n" +
                "    [\n" +
                "      \"Tiger\",\n" +
                "      \"Nixon\",\n" +
                "      \"System Architect\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"25th Apr 11\",\n" +
                "      \"$320,800\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Garrett\",\n" +
                "      \"Winters\",\n" +
                "      \"Accountant\",\n" +
                "      \"Tokyo\",\n" +
                "      \"25th Jul 11\",\n" +
                "      \"$170,750\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Ashton\",\n" +
                "      \"Cox\",\n" +
                "      \"Junior Technical Author\",\n" +
                "      \"San Francisco\",\n" +
                "      \"12th Jan 09\",\n" +
                "      \"$86,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Cedric\",\n" +
                "      \"Kelly\",\n" +
                "      \"Senior Javascript Developer\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"29th Mar 12\",\n" +
                "      \"$433,060\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Airi\",\n" +
                "      \"Satou\",\n" +
                "      \"Accountant\",\n" +
                "      \"Tokyo\",\n" +
                "      \"28th Nov 08\",\n" +
                "      \"$162,700\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Brielle\",\n" +
                "      \"Williamson\",\n" +
                "      \"Integration Specialist\",\n" +
                "      \"New York\",\n" +
                "      \"2nd Dec 12\",\n" +
                "      \"$372,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Herrod\",\n" +
                "      \"Chandler\",\n" +
                "      \"Sales Assistant\",\n" +
                "      \"San Francisco\",\n" +
                "      \"6th Aug 12\",\n" +
                "      \"$137,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Rhona\",\n" +
                "      \"Davidson\",\n" +
                "      \"Integration Specialist\",\n" +
                "      \"Tokyo\",\n" +
                "      \"14th Oct 10\",\n" +
                "      \"$327,900\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Colleen\",\n" +
                "      \"Hurst\",\n" +
                "      \"Javascript Developer\",\n" +
                "      \"San Francisco\",\n" +
                "      \"15th Sep 09\",\n" +
                "      \"$205,500\"\n" +
                "    ]\n" +
                "  ]\n" +
                "}\n";
    }


}
