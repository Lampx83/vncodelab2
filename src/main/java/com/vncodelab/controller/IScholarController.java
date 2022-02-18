package com.vncodelab.controller;

import com.vncodelab.entity.ischolar.Item;
import com.vncodelab.json.ischolar.Jsmind;
import com.vncodelab.model.ischolar.DataTableRequest;
import com.vncodelab.model.ischolar.JournalList;
import com.vncodelab.respository.PhraseRepository;
import com.vncodelab.service.ischolar.GenDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;

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
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    JournalList journal(@RequestBody DataTableRequest datatableRequest, @RequestParam("c") String c) {
        return phraseRepository.getJournal(datatableRequest,c);
    }


    @RequestMapping(
            value = "/test1",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    String test() {
        return "{\n" +
                "  \"draw\": 1,\n" +
                "  \"recordsTotal\": 57,\n" +
                "  \"recordsFiltered\": 57,\n" +
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
                "    ],\n" +
                "    [\n" +
                "      \"Sonya\",\n" +
                "      \"Frost\",\n" +
                "      \"Software Engineer\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"13th Dec 08\",\n" +
                "      \"$103,600\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Jena\",\n" +
                "      \"Gaines\",\n" +
                "      \"Office Manager\",\n" +
                "      \"London\",\n" +
                "      \"19th Dec 08\",\n" +
                "      \"$90,560\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Quinn\",\n" +
                "      \"Flynn\",\n" +
                "      \"Support Lead\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"3rd Mar 13\",\n" +
                "      \"$342,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Charde\",\n" +
                "      \"Marshall\",\n" +
                "      \"Regional Director\",\n" +
                "      \"San Francisco\",\n" +
                "      \"16th Oct 08\",\n" +
                "      \"$470,600\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Haley\",\n" +
                "      \"Kennedy\",\n" +
                "      \"Senior Marketing Designer\",\n" +
                "      \"London\",\n" +
                "      \"18th Dec 12\",\n" +
                "      \"$313,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Tatyana\",\n" +
                "      \"Fitzpatrick\",\n" +
                "      \"Regional Director\",\n" +
                "      \"London\",\n" +
                "      \"17th Mar 10\",\n" +
                "      \"$385,750\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Michael\",\n" +
                "      \"Silva\",\n" +
                "      \"Marketing Designer\",\n" +
                "      \"London\",\n" +
                "      \"27th Nov 12\",\n" +
                "      \"$198,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Paul\",\n" +
                "      \"Byrd\",\n" +
                "      \"Chief Financial Officer (CFO)\",\n" +
                "      \"New York\",\n" +
                "      \"9th Jun 10\",\n" +
                "      \"$725,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Gloria\",\n" +
                "      \"Little\",\n" +
                "      \"Systems Administrator\",\n" +
                "      \"New York\",\n" +
                "      \"10th Apr 09\",\n" +
                "      \"$237,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Bradley\",\n" +
                "      \"Greer\",\n" +
                "      \"Software Engineer\",\n" +
                "      \"London\",\n" +
                "      \"13th Oct 12\",\n" +
                "      \"$132,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Dai\",\n" +
                "      \"Rios\",\n" +
                "      \"Personnel Lead\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"26th Sep 12\",\n" +
                "      \"$217,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Jenette\",\n" +
                "      \"Caldwell\",\n" +
                "      \"Development Lead\",\n" +
                "      \"New York\",\n" +
                "      \"3rd Sep 11\",\n" +
                "      \"$345,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Yuri\",\n" +
                "      \"Berry\",\n" +
                "      \"Chief Marketing Officer (CMO)\",\n" +
                "      \"New York\",\n" +
                "      \"25th Jun 09\",\n" +
                "      \"$675,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Caesar\",\n" +
                "      \"Vance\",\n" +
                "      \"Pre-Sales Support\",\n" +
                "      \"New York\",\n" +
                "      \"12th Dec 11\",\n" +
                "      \"$106,450\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Doris\",\n" +
                "      \"Wilder\",\n" +
                "      \"Sales Assistant\",\n" +
                "      \"Sidney\",\n" +
                "      \"20th Sep 10\",\n" +
                "      \"$85,600\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Angelica\",\n" +
                "      \"Ramos\",\n" +
                "      \"Chief Executive Officer (CEO)\",\n" +
                "      \"London\",\n" +
                "      \"9th Oct 09\",\n" +
                "      \"$1,200,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Gavin\",\n" +
                "      \"Joyce\",\n" +
                "      \"Developer\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"22nd Dec 10\",\n" +
                "      \"$92,575\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Jennifer\",\n" +
                "      \"Chang\",\n" +
                "      \"Regional Director\",\n" +
                "      \"Singapore\",\n" +
                "      \"14th Nov 10\",\n" +
                "      \"$357,650\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Brenden\",\n" +
                "      \"Wagner\",\n" +
                "      \"Software Engineer\",\n" +
                "      \"San Francisco\",\n" +
                "      \"7th Jun 11\",\n" +
                "      \"$206,850\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Fiona\",\n" +
                "      \"Green\",\n" +
                "      \"Chief Operating Officer (COO)\",\n" +
                "      \"San Francisco\",\n" +
                "      \"11th Mar 10\",\n" +
                "      \"$850,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Shou\",\n" +
                "      \"Itou\",\n" +
                "      \"Regional Marketing\",\n" +
                "      \"Tokyo\",\n" +
                "      \"14th Aug 11\",\n" +
                "      \"$163,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Michelle\",\n" +
                "      \"House\",\n" +
                "      \"Integration Specialist\",\n" +
                "      \"Sidney\",\n" +
                "      \"2nd Jun 11\",\n" +
                "      \"$95,400\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Suki\",\n" +
                "      \"Burks\",\n" +
                "      \"Developer\",\n" +
                "      \"London\",\n" +
                "      \"22nd Oct 09\",\n" +
                "      \"$114,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Prescott\",\n" +
                "      \"Bartlett\",\n" +
                "      \"Technical Author\",\n" +
                "      \"London\",\n" +
                "      \"7th May 11\",\n" +
                "      \"$145,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Gavin\",\n" +
                "      \"Cortez\",\n" +
                "      \"Team Leader\",\n" +
                "      \"San Francisco\",\n" +
                "      \"26th Oct 08\",\n" +
                "      \"$235,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Martena\",\n" +
                "      \"Mccray\",\n" +
                "      \"Post-Sales support\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"9th Mar 11\",\n" +
                "      \"$324,050\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Unity\",\n" +
                "      \"Butler\",\n" +
                "      \"Marketing Designer\",\n" +
                "      \"San Francisco\",\n" +
                "      \"9th Dec 09\",\n" +
                "      \"$85,675\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Howard\",\n" +
                "      \"Hatfield\",\n" +
                "      \"Office Manager\",\n" +
                "      \"San Francisco\",\n" +
                "      \"16th Dec 08\",\n" +
                "      \"$164,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Hope\",\n" +
                "      \"Fuentes\",\n" +
                "      \"Secretary\",\n" +
                "      \"San Francisco\",\n" +
                "      \"12th Feb 10\",\n" +
                "      \"$109,850\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Vivian\",\n" +
                "      \"Harrell\",\n" +
                "      \"Financial Controller\",\n" +
                "      \"San Francisco\",\n" +
                "      \"14th Feb 09\",\n" +
                "      \"$452,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Timothy\",\n" +
                "      \"Mooney\",\n" +
                "      \"Office Manager\",\n" +
                "      \"London\",\n" +
                "      \"11th Dec 08\",\n" +
                "      \"$136,200\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Jackson\",\n" +
                "      \"Bradshaw\",\n" +
                "      \"Director\",\n" +
                "      \"New York\",\n" +
                "      \"26th Sep 08\",\n" +
                "      \"$645,750\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Olivia\",\n" +
                "      \"Liang\",\n" +
                "      \"Support Engineer\",\n" +
                "      \"Singapore\",\n" +
                "      \"3rd Feb 11\",\n" +
                "      \"$234,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Bruno\",\n" +
                "      \"Nash\",\n" +
                "      \"Software Engineer\",\n" +
                "      \"London\",\n" +
                "      \"3rd May 11\",\n" +
                "      \"$163,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Sakura\",\n" +
                "      \"Yamamoto\",\n" +
                "      \"Support Engineer\",\n" +
                "      \"Tokyo\",\n" +
                "      \"19th Aug 09\",\n" +
                "      \"$139,575\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Thor\",\n" +
                "      \"Walton\",\n" +
                "      \"Developer\",\n" +
                "      \"New York\",\n" +
                "      \"11th Aug 13\",\n" +
                "      \"$98,540\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Finn\",\n" +
                "      \"Camacho\",\n" +
                "      \"Support Engineer\",\n" +
                "      \"San Francisco\",\n" +
                "      \"7th Jul 09\",\n" +
                "      \"$87,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Serge\",\n" +
                "      \"Baldwin\",\n" +
                "      \"Data Coordinator\",\n" +
                "      \"Singapore\",\n" +
                "      \"9th Apr 12\",\n" +
                "      \"$138,575\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Zenaida\",\n" +
                "      \"Frank\",\n" +
                "      \"Software Engineer\",\n" +
                "      \"New York\",\n" +
                "      \"4th Jan 10\",\n" +
                "      \"$125,250\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Zorita\",\n" +
                "      \"Serrano\",\n" +
                "      \"Software Engineer\",\n" +
                "      \"San Francisco\",\n" +
                "      \"1st Jun 12\",\n" +
                "      \"$115,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Jennifer\",\n" +
                "      \"Acosta\",\n" +
                "      \"Junior Javascript Developer\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"1st Feb 13\",\n" +
                "      \"$75,650\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Cara\",\n" +
                "      \"Stevens\",\n" +
                "      \"Sales Assistant\",\n" +
                "      \"New York\",\n" +
                "      \"6th Dec 11\",\n" +
                "      \"$145,600\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Hermione\",\n" +
                "      \"Butler\",\n" +
                "      \"Regional Director\",\n" +
                "      \"London\",\n" +
                "      \"21st Mar 11\",\n" +
                "      \"$356,250\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Lael\",\n" +
                "      \"Greer\",\n" +
                "      \"Systems Administrator\",\n" +
                "      \"London\",\n" +
                "      \"27th Feb 09\",\n" +
                "      \"$103,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Jonas\",\n" +
                "      \"Alexander\",\n" +
                "      \"Developer\",\n" +
                "      \"San Francisco\",\n" +
                "      \"14th Jul 10\",\n" +
                "      \"$86,500\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Shad\",\n" +
                "      \"Decker\",\n" +
                "      \"Regional Director\",\n" +
                "      \"Edinburgh\",\n" +
                "      \"13th Nov 08\",\n" +
                "      \"$183,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Michael\",\n" +
                "      \"Bruce\",\n" +
                "      \"Javascript Developer\",\n" +
                "      \"Singapore\",\n" +
                "      \"27th Jun 11\",\n" +
                "      \"$183,000\"\n" +
                "    ],\n" +
                "    [\n" +
                "      \"Donna\",\n" +
                "      \"Snider\",\n" +
                "      \"Customer Support\",\n" +
                "      \"New York\",\n" +
                "      \"25th Jan 11\",\n" +
                "      \"$112,000\"\n" +
                "    ]\n" +
                "  ]\n" +
                "}";
    }


}
