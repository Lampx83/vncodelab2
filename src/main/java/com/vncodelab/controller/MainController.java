package com.vncodelab.controller;

import com.google.gson.Gson;
import com.vncodelab.entity.Home;
import com.vncodelab.entity.LabF;
import com.vncodelab.json.LabInfo;
import com.vncodelab.model.AjaxResponseBody;

import com.vncodelab.service.serviceImpl.HomeServiceImpl;
import com.vncodelab.service.serviceImpl.LabsServiceImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class MainController {

    @Autowired
    private HomeServiceImpl homeServiceImpl;

    @Autowired
    private LabsServiceImpl labsServiceImpl;

//    @PostMapping("/save")
//    public ResponseEntity<?> save(@RequestBody Lab newLab) throws IOException, InterruptedException {
//        Lab lab = labRespository.findByDocID(newLab.getDocID());
//        if (lab == null)
//            lab = newLab;
//
//        Process p = Runtime.getRuntime().exec(System.getProperty("user.home") + "/bin/claat export " + newLab.getDocID());
//        BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//        String line = input.readLine();
//        p.waitFor();
//        String arr[] = line.split("\t");
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(arr[1] + "/codelab.json")));
//        String totalLine = "";
//        while ((line = br.readLine()) != null) {
//            totalLine = totalLine + line;
//        }
//        LabInfo labInfo = new Gson().fromJson(totalLine, LabInfo.class);
//        lab.setName(labInfo.getTitle());
//        br = new BufferedReader(new InputStreamReader(new FileInputStream(arr[1] + "/index.html")));
//        totalLine = "";
//        while ((line = br.readLine()) != null) {
//            totalLine = totalLine + line + "\n";
//        }
//        lab.setHtml(totalLine);
//        lab.setCateID(newLab.getCateID());
//        lab.setDescription(newLab.getDescription());
//        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
//        ajaxResponseBody.setUpdate(labRespository.existsByDocID(newLab.getDocID()));
//
//
//        labRespository.save(lab);
//        return ResponseEntity.ok().body(ajaxResponseBody);
//    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody LabF newLab) throws IOException, InterruptedException {


        Process p = Runtime.getRuntime().exec(System.getProperty("user.home") + "/go/bin/claat export " + newLab.getDocID());
        //Process p = Runtime.getRuntime().exec("/home/phamxuanlam/work/bin/claat export " + newLab.getDocID());
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line = input.readLine();
        p.waitFor();
        String arr[] = line.split("\t");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(arr[1] + "/codelab.json")));
        String totalLine = "";
        while ((line = br.readLine()) != null) {
            totalLine = totalLine + line;
        }
        LabInfo labInfo = new Gson().fromJson(totalLine, LabInfo.class);
        newLab.setName(labInfo.getTitle());


        File inputFile = new File(arr[1] + "/index.html");
        Document doc = Jsoup.parse(inputFile, "UTF-8");

        Element codelab = doc.getElementsByTag("google-codelab").get(0);
        newLab.setHtml(codelab.toString());
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setUpdate(true);

        labsServiceImpl.saveObjectFirebase(newLab);
        return ResponseEntity.ok().body(ajaxResponseBody);
    }
//
//    @GetMapping("/createCate")
//    public String createCate() {
//        cateRespository.save(new Cate("Java", ""));
//        cateRespository.save(new Cate("Swing", ""));
//        cateRespository.save(new Cate("JSP", ""));
//        cateRespository.save(new Cate("Servlet", ""));
//        cateRespository.save(new Cate("JPA", ""));
//        cateRespository.save(new Cate("Hibernate", ""));
//        cateRespository.save(new Cate("Spring", ""));
//        cateRespository.save(new Cate("Android", ""));
//
//        cateRespository.save(new Cate("C", "", 1));
//        cateRespository.save(new Cate("C++", "", 1));
//        cateRespository.save(new Cate("PHP", "", 1));
//        cateRespository.save(new Cate("JavaScript", "", 1));
//        return "done";
//    }

    @GetMapping("/lab/{labID}")
    public String lab(Model model, @PathVariable(name = "labID") String labID) {
        // Optional<Lab> optional = labRespository.findById(labID);
        //  model.addAttribute("lab", optional.get());
        model.addAttribute("lab", labsServiceImpl.getLab(labID));
        return "lab";
    }


    @GetMapping("/")
    public String index(Model model) throws InterruptedException, ExecutionException {
       // showHome(model, 0);
        return "index";
    }

    @GetMapping("/mylabs")
    public String labs(Model model) throws InterruptedException, ExecutionException {// Hien thi toan bo labs cua nguoi do tu Firebase
        List<LabF> list = labsServiceImpl.getObjectFirebase();
        model.addAttribute("labList", list);
        return "mylabs";
    }

//    @GetMapping("/cate/{cateID}")
//    public String cate(Model model, @PathVariable(name = "cateID") int cateID) throws InterruptedException, ExecutionException {
//        showHome(model, cateID);
//        return "index";
//    }

//    void showHome(Model model, int cateID) throws ExecutionException, InterruptedException {
//        Map<String, Object> infor = homeServiceImpl.getObjectFirebase();
//        Home newInfor = homeServiceImpl.getInforFirebase(infor);
//        model.addAttribute("infor", newInfor);
//        if (cateID == 0)
//            model.addAttribute("labList", labRespository.findAll());
//        else
//            model.addAttribute("labList", labRespository.findAllByCateID(cateID));
//        model.addAttribute("cateList", cateRespository.findAllByType(0));
//        model.addAttribute("cateListMore", cateRespository.findAllByType(1));
//
//    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
