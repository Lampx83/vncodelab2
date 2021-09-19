package com.vncodelab.controller;

import com.google.api.core.ApiFuture;
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
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Controller
public class MainController {

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
    public String roadmap(Model model, @PathVariable(name = "roadID") String roadID) {
        return "roadmap";
    }


    public void updateHTML(@RequestBody Lab newLab) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("./claat export " + newLab.getDocID()); //Localhost
        //   Process p = Runtime.getRuntime().exec("/home/phamxuanlam/go/bin/claat export " + newLab.getDocID());  //For Google Cloud

        //  ProcessBuilder builder = new ProcessBuilder();
        //builder.command("classpath:claat", "export", newLab.getDocID());
        //    Process p = builder.start();
        p.waitFor();
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line = input.readLine();
        System.out.println(line);


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

        //Save to Fire Store
        {
            //Save to Storage {userID}/labs/{lab_name}
            StorageClient storageClient = StorageClient.getInstance();  //Storage
            for (Element el : img) {
                File file = new File(folderName + "/" + el.attr("src"));
                InputStream is = new FileInputStream(file);
                Blob blob = storageClient.bucket().create("labs/" + newLab.getUserID() + "/" + newLab.getDocID() + "/" + file.getName(), is);
                String newUrl = blob.signUrl(9999, TimeUnit.DAYS).toString();
                el.attr("src", newUrl);
            }
        }
//        {
//            for (Element el : img) {
//                if(!el.attr("src").isEmpty()) {
//                    File file = new File(folderName + "/" + el.attr("src"));
//                    FileInputStream input1 = new FileInputStream(file);
//                    MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input1));
//                    String fileName = fileStorageService.storeFile(multipartFile);
//                    el.attr("src", "/img/" + file.getName());
//                }
//            }
//        }

        FileUtils.deleteDirectory(new File(folderName));
        Element codelab = doc.getElementsByTag("google-codelab").get(0);
        newLab.setHtml(codelab.toString());
    }


    @PostMapping("/createLab")
    public ResponseEntity<?> createLab(@RequestBody Lab newLab, @RequestParam String action) throws IOException, InterruptedException {
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
                updateHTML(newLab);  //Claat
                newLab.setOrder(999);
                labService.save(newLab);
            } else if (action.equals("updateAll")) {
                updateHTML(newLab);  //Claat
                labService.save(newLab);
            } else if (action.equals("updateHTML")) {
                Lab lab = labService.getByID(newLab.getDocID());
                updateHTML(lab);  //Claat
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

    @PostMapping("/report_raisehand")
    public ResponseEntity<?> report_raisehand(@RequestBody Room room) throws InterruptedException, ExecutionException {
        Firestore db = FirestoreClient.getFirestore();  //FireStorage
        ApiFuture<QuerySnapshot> future = db.collection("rooms").document(room.getRoomID()).collection("logs").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        String s = "";
        for (DocumentSnapshot document : documents) {
            TreeMap<Integer, Step> map = new TreeMap<>();

            for (int i = 0; i < room.getNumberOfStep(); i++) {
                Step step = new Step();
                map.put(i, step);
            }

            ApiFuture<QuerySnapshot> future1 = document.getReference().collection("hands").whereEqualTo("type", 0).get();
            List<QueryDocumentSnapshot> documents1 = future1.get().getDocuments();
            for (DocumentSnapshot document1 : documents1) {
                Log log = document1.toObject(Log.class);
                map.get(log.getStep()).setNumber(map.get(log.getStep()).getNumber() + 1);
            }
            User user = document.toObject(User.class);
            user.setUserID(document.getId());
            String step = "";
            for (int i = 0; i < room.getNumberOfStep(); i++) {
                String detail = "<span class='report-detail d-none'>" + map.get(i).getNumber() + "</span>";
                if (map.get(i).getNumber() >= 1) {
                    step = step + "<td><span class ='labStep blue' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span>" + detail + "</td>";
                } else {
                    step = step + "<td><span class ='labStep' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span>" + detail + "</td>";
                }
            }
            String tdThreeDots = "<td class='text-right align-middle'><a href='#' class='bi bi-three-dots-vertical' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteUserReport(\"" + user.getUserID() + "\")'>Xóa</a> </div></td>";
            s = s + "<tr id='tr-report-" + user.getUserID() + "' ><td  class='user-name'>" + user.getUserName() + "</td><td>" + step + "</td>" + tdThreeDots + "</tr>";
        }
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setMsg(s);
        return ResponseEntity.ok().body(ajaxResponseBody);
    }

    @PostMapping("/report_practice")
    public ResponseEntity<?> report_practice(@RequestBody Room room) throws InterruptedException, ExecutionException {
        Firestore db = FirestoreClient.getFirestore();  //FireStorage
        ApiFuture<QuerySnapshot> future = db.collection("rooms").document(room.getRoomID()).collection("logs").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        String s = "";
        for (DocumentSnapshot document : documents) {
            TreeMap<Integer, Step> map = new TreeMap<>();

            for (int i = 0; i < room.getNumberOfStep(); i++) {
                Step step = new Step();
                map.put(i, step);
            }

            ApiFuture<QuerySnapshot> future1 = document.getReference().collection("steps").get();
            List<QueryDocumentSnapshot> documents1 = future1.get().getDocuments();
            for (DocumentSnapshot document1 : documents1) {
                try {
                    Log log = document1.toObject(Log.class);
                    Step cStep = map.get(log.getLeave());
                    if (cStep != null)
                        cStep.setNumber(cStep.getNumber() + log.getDuration());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            User user = document.toObject(User.class);
            user.setUserID(document.getId());
            String step = "";
            for (int i = 0; i < room.getNumberOfStep(); i++) {
                if (map.get(i).getNumber() > 10 * 60) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize3' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else if (map.get(i).getNumber() > 6 * 60) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize2' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else if (map.get(i).getNumber() > 3 * 60) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize1' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else if (map.get(i).getNumber() > 15) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else {
                    step = step + "<td class='tdcenter'><span class ='labStep' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                }
            }
            String tdThreeDots = "<td class='text-right align-middle'><a href='#' class='bi bi-three-dots-vertical' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='deleteUserReport(\"" + user.getUserID() + "\")'>Xóa</a> </div></td>";
            s = s + "<tr id='tr-report-" + user.getUserID() + "'><td class='user-name'>" + user.getUserName() + "</td><td>" + step + "</td>" + tdThreeDots + "</tr>";
        }

        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setMsg(s);
        return ResponseEntity.ok().body(ajaxResponseBody);
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
        if (lab.isSlides())
            selector = doc.select("google-codelab");
        selector.attr("no-arrows", "true");
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

//    public static void main(String[] args) throws IOException, InterruptedException {
//        Lab newLab = new Lab();
//        newLab.setDocID("11gRpdzlXHIwZ__YS0N9yNBo9E7vjyDgZ_0-wnQvCUDA");
//        new MainController().save(newLab);
//    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
            System.out.println("run");
        }
    }
}

