package com.vncodelab.controller;

import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import com.google.gson.Gson;
import com.vncodelab.entity.Lab;
import com.vncodelab.json.LabInfo;
import com.vncodelab.model.AjaxResponseBody;
import com.vncodelab.others.MyFunc;
import com.vncodelab.service.serviceImpl.LabsServiceImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Controller
public class MainController {

    @Autowired
    private LabsServiceImpl labsServiceImpl;

    @GetMapping("/")
    public String index(Model model) {
        List<Lab> list = labsServiceImpl.getFeatureLabsByCate(null);
        model.addAttribute("labList", list);
        model.addAttribute("cateList", MyFunc.getCateList());
        model.addAttribute("cateListMore", MyFunc.getMoreCateList());
        return "index";
    }

    @GetMapping("/cate/{cateID}")
    public String index(Model model, @PathVariable(name = "cateID") String cateID) {
        List<Lab> list = labsServiceImpl.getFeatureLabsByCate(cateID);
        model.addAttribute("labList", list);
        model.addAttribute("cateList", MyFunc.getCateList());
        model.addAttribute("cateListMore", MyFunc.getMoreCateList());
        return "index";
    }


    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Lab newLab) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(System.getProperty("user.home") + "/go/bin/claat export " + newLab.getDocID());
       // Process p = Runtime.getRuntime().exec("/home/phamxuanlam/work/bin/claat export " + newLab.getDocID());  //For Google Cloud
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line = input.readLine();
        p.waitFor();
        String folderName = line.split("\t")[1];
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(folderName + "/codelab.json")));
        String totalLine = "";
        while ((line = br.readLine()) != null)
            totalLine = totalLine + line;
        LabInfo labInfo = new Gson().fromJson(totalLine, LabInfo.class);
        newLab.setName(labInfo.getTitle());

        File inputFile = new File(folderName + "/index.html");
        Document doc = Jsoup.parse(inputFile, "UTF-8");
        Elements img = doc.getElementsByTag("img");

        StorageClient storageClient = StorageClient.getInstance();
        for (Element el : img) {
            File file = new File(folderName + "/" + el.attr("src"));
            InputStream is = new FileInputStream(file);
            Blob blob = storageClient.bucket().create("labs/" + newLab.getUserID() + "/" + folderName + "/" + file.getName(), is);
            String newUrl = blob.signUrl(9999, TimeUnit.DAYS).toString();
            el.attr("src", newUrl);
        }

        Element codelab = doc.getElementsByTag("google-codelab").get(0);
        newLab.setHtml(codelab.toString());
        labsServiceImpl.saveObjectFirebase(newLab);

        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setUpdate(true);
        return ResponseEntity.ok().body(ajaxResponseBody);
    }

    @GetMapping("/lab/{labID}")
    public String lab(Model model, @PathVariable(name = "labID") String labID) {
        // Optional<Lab> optional = labRespository.findById(labID);
        //  model.addAttribute("lab", optional.get());
        model.addAttribute("lab", labsServiceImpl.getLab(labID));
        return "lab";
    }

    @GetMapping("/mylabs")
    public String labs(Model model) throws InterruptedException, ExecutionException {// Hien thi toan bo labs cua nguoi do tu Firebase
        model.addAttribute("cateList", MyFunc.getCateList());
        return "mylabs";
    }


//    public static void main(String[] args) throws IOException, InterruptedException {
//        Lab newLab = new Lab();
//        newLab.setDocID("11gRpdzlXHIwZ__YS0N9yNBo9E7vjyDgZ_0-wnQvCUDA");
//        new MainController().save(newLab);
//    }
}
