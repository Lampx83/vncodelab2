package com.vncodelab.controller;

import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.google.gson.Gson;
import com.vncodelab.entity.*;
import com.vncodelab.json.LabInfo;
import com.vncodelab.model.AjaxResponseBody;
import com.vncodelab.others.MyFunc;
import com.vncodelab.service.FileStorageService;
import com.vncodelab.service.LabService;
import com.vncodelab.service.RoomService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Controller
public class VncodelabController {

    @Autowired
    private LabService labService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("page", "home");
        return "index";
    }

    @GetMapping("/mylabs")
    public String mylabs(Model model) {
        model.addAttribute("page", "mylabs");
        return "index";
    }

    @GetMapping("/img/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/roadmap/{roadID}")
    public String roadmap() {
        return "roadmap";
    }


    public void updateHTML(@RequestBody Lab newLab, HttpServletRequest request) throws IOException, InterruptedException {
        String host = getHost(request);
        ProcessBuilder builder;
        if (host.equals("localhost"))
            builder = new ProcessBuilder("./claat", "export", newLab.getDocID()).inheritIO();
        else
            builder = new ProcessBuilder("/home/phamxuanlam/go/bin/claat", "export", newLab.getDocID()).inheritIO();
        builder.redirectErrorStream(true);
        File fileOutput = new File("output.txt");
        builder.redirectOutput(fileOutput);
        Process process = builder.start();
        process.waitFor();
        String content = FileUtils.readFileToString(fileOutput, StandardCharsets.UTF_8);
        String folderName = content.split("\t")[1].trim();
        LabInfo labInfo = new Gson().fromJson(FileUtils.readFileToString(new File(folderName + "/codelab.json"), StandardCharsets.UTF_8), LabInfo.class);
        newLab.setName(labInfo.getTitle());

        File inputFile = new File(folderName + "/index.html");
        Document doc = Jsoup.parse(inputFile, "UTF-8");
        Elements img = doc.getElementsByTag("img");

        //Save to Fire Store
//        {
//            //Save to Storage {userID}/labs/{lab_name}
//            StorageClient storageClient = StorageClient.getInstance();  //Storage
//            for (Element el : img) {
//                File file = new File(folderName + "/" + el.attr("src"));
////                System.out.println(folderName + "/" + el.attr("src"));
//                InputStream is = new FileInputStream(file);
//                Blob blob = storageClient.bucket().create("labs/" + newLab.getUserID() + "/" + newLab.getDocID() + "/" + file.getName(), is);
//                String newUrl = blob.signUrl(9999, TimeUnit.DAYS).toString();
//                el.attr("src", newUrl);
//            }
//        }

        {
            for (Element el : img) {
                if (!el.attr("src").isEmpty()) {
                    File file = new File(folderName + "/" + el.attr("src"));
                    FileInputStream input1 = new FileInputStream(file);
                    MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input1));
                    String fileName = fileStorageService.storeFile(multipartFile);
                    el.attr("src", "/img/" + file.getName());
                }
            }
        }

        FileUtils.deleteDirectory(new File(folderName));
        Element codelab = doc.getElementsByTag("google-codelab").get(0);
        newLab.setHtml(codelab.toString());
    }

    @GetMapping("/preview/{labID}")
    public String previewLab(Model model, @PathVariable(name = "labID") String labID, HttpServletRequest request) throws IOException, InterruptedException {
        Lab newLab = new Lab();
        newLab.setDocID(labID);
        updateHTML(newLab, request);  //Claat
        model.addAttribute("lab", newLab);
        return "lab";
    }

    String getHost(HttpServletRequest request) throws MalformedURLException {
        URL url = new URL(request.getRequestURL().toString());
        return url.getHost();
    }
    @GetMapping(path = "/post/{docID}")
    public String post(Model model, @PathVariable(name = "docID") String docID)  {
        String url = "https://docs.google.com/feeds/download/documents/export/Export?id="+docID+"&exportFormat=html";
        StringBuilder textBuilder = new StringBuilder();
        try {
            InputStream inputStream = new URL(url).openStream();

            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("content", textBuilder.toString());
        return "post";
    }

    @PostMapping("/createLab")
    public ResponseEntity<?> createLab(@RequestBody Lab newLab, @RequestParam String action, HttpServletRequest request) throws IOException, InterruptedException {
        try {
            String docID = newLab.getDocID();
            if (docID.contains("docs.google.com")) {
                URL url = new URL(docID);
                String path = url.getPath();
                String[] arr = path.split("/");
                int maxLength = 0;
                String longestString = null;
                for (String s : arr) {
                    if (s.length() > maxLength) {
                        maxLength = s.length();
                        longestString = s;
                    }
                }
                newLab.setDocID(longestString);
            } else if (docID.contains("codelabs-preview.appspot.com")) {
                Map<String, String> map = MyFunc.getQueryMap(new URL(docID).getQuery());
                if (map.get("file_id") != null) {
                    newLab.setDocID(map.get("file_id"));
                }
            }
            if (action.equals("insert")) {
                updateHTML(newLab, request);  //Claat
                newLab.setOrder(999);
                labService.save(newLab);
            } else if (action.equals("updateAll")) {
                updateHTML(newLab, request);  //Claat
                labService.save(newLab);
            } else if (action.equals("updateHTML")) {
                Lab lab = labService.getByID(newLab.getDocID());
                updateHTML(lab, request);  //Claat
                labService.save(lab);
                return ResponseEntity.ok().body(lab);
            } else if (action.equals("updateInfo")) {
                Lab lab = labService.getByID(newLab.getDocID());
                lab.setDescription(newLab.getDescription());
                lab.setFeature(newLab.getFeature());
                lab.setSlides(newLab.isSlides());
                lab.setUserID(newLab.getUserID());
                lab.setCateID(newLab.getCateID());
                labService.save(lab);
            }

            return ResponseEntity.ok().body(newLab);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @PostMapping("/deleteLab")
    public ResponseEntity<?> deleteLab(@RequestBody Lab lab) {
        try {
            labService.delete(lab);
            AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
            ajaxResponseBody.setUpdate(true);
            return ResponseEntity.ok().body(ajaxResponseBody);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @PostMapping("/deleteRoom")
    public ResponseEntity<?> deleteRoom(@RequestBody Room room) {
        try {
            roomService.delete(room);
            AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
            ajaxResponseBody.setUpdate(true);
            return ResponseEntity.ok().body(ajaxResponseBody);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @GetMapping("/cate/{cateID}")
    public String index(Model model, @PathVariable(name = "cateID") String cateID) {
        List<Lab> list = labService.getFeatureLabsByCate(cateID);
        model.addAttribute("labList", list);
        model.addAttribute("cateList", MyFunc.getCateList());
        model.addAttribute("cateListMore", MyFunc.getMoreCateList());
        return "index";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("files") ArrayList<MultipartFile> files, @RequestParam("userID") String userID, @RequestParam("uname") String uname, @RequestParam("room") String room, @RequestParam("survey_id") String survey_id) throws IOException, InterruptedException, ExecutionException {
        String url = "";
        ArrayList fileNames = new ArrayList<>();
        ArrayList fileLinks = new ArrayList<>();
        StorageClient storageClient = StorageClient.getInstance();

        for (MultipartFile multipartFile : files) {
            if (multipartFile.getOriginalFilename() != null && !multipartFile.getOriginalFilename().isEmpty()) {
                File file = convertToFile(multipartFile, multipartFile.getOriginalFilename());
                InputStream is = new FileInputStream(file);
                Blob blob = storageClient.bucket().create("submits/" + room + "/" + userID + "/" + survey_id + "/" + file.getName(), is);
                String newUrl = blob.signUrl(9999, TimeUnit.DAYS).toString();
                url = url + "<br><a class='text-primary' href = '" + newUrl + "' >" + file.getName() + "</a ><br>";
                fileLinks.add(newUrl);
                fileNames.add(file.getName());
                //Xoa file
                file.delete();

                DocumentReference userRef = FirestoreClient.getFirestore().collection("rooms").document(room).collection("surveys").document(survey_id).collection("answers").document(userID);
                HashMap mapSubmit = new HashMap();
                mapSubmit.put("uname", uname);
                mapSubmit.put("time", FieldValue.serverTimestamp());
                mapSubmit.put("fileNames", fileNames);
                mapSubmit.put("fileLinks", fileLinks);
                userRef.set(mapSubmit);
            }
        }

        String output = "<p>";
        if (url != null && url != "")
            output = output + "<b>File đã nộp</b>: " + url;

        return ResponseEntity.ok().body(output);

    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }


    @GetMapping("/room/{roomID}")
    public String room(Model model, @PathVariable(name = "roomID") String roomID) {
        Room room = roomService.getByID(roomID);
        Lab lab = labService.getByID(room.getDocID());
        modifyLab(lab);
        model.addAttribute("lab", lab);
        return "lab";
    }

    @GetMapping("/lab/{labID}")
    public String lab(Model model, @PathVariable(name = "labID") String labID) {
        Lab lab = labService.getByID(labID);
        modifyLab(lab);
        model.addAttribute("lab", lab);
        return "lab";
    }

    private void modifyLab(Lab lab) {
        Document doc = Jsoup.parse(lab.getHtml(), "UTF-8");
        Elements selector = doc.select("script");
        for (Element element : selector) {
            element.remove();
        }
        if (lab.isSlides()) {
            selector = doc.select("google-codelab");
            selector.attr("no-arrows", "true");
        }
        if (lab.getDocID().equals("1X-ACutohLWIP9wmlT7ZF8KrkngS6Fuj2hssxpeTToAY")) {
            Elements step = doc.getElementsByTag("google-codelab-step");
            Collections.shuffle(step);

            doc.getElementsByTag("google-codelab").get(0).html(step.toString());
        }
        lab.setHtml(doc.html());
    }

    @PostMapping("/createRoom")
    public ResponseEntity<?> createRoom(@RequestBody Room room) throws ExecutionException, InterruptedException {
        String s = roomService.save(room);
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setUpdate(true);
        return ResponseEntity.ok().body(ajaxResponseBody);
    }

    @PostMapping("/deleteUserReport")
    public ResponseEntity<?> deleteUserReport(@RequestBody Room room) throws ExecutionException, InterruptedException {
        roomService.deleteUserReport(room);
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setUpdate(true);
        return ResponseEntity.ok().body(ajaxResponseBody);
    }

    @GetMapping("/room/export_report/{roomID}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> room(@PathVariable(name = "roomID") String roomID) {
        return roomService.genExcel(roomID);
    }

    @PostMapping("/roadmap/export_report")
    @ResponseBody
    public ResponseEntity<InputStreamResource> roadmap_export(@RequestBody ArrayList<RoadMap> roadMap) {
        return roomService.genExcel(roadMap);
    }
}

