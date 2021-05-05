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
import com.vncodelab.service.serviceImpl.LabService;
import com.vncodelab.service.serviceImpl.RoomService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Controller
public class MainController {

    @Autowired
    private LabService labService;
    @Autowired
    private RoomService roomService;

    @GetMapping("/")
    public String index(Model model) {
        List<Lab> list = labService.getFeatureLabsByCate(null);
        model.addAttribute("labList", list);
        model.addAttribute("cateList", MyFunc.getCateList());
        model.addAttribute("cateListMore", MyFunc.getMoreCateList());
        return "index";
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
    public ResponseEntity<?> upload(@RequestParam("files") ArrayList<MultipartFile> files, @RequestParam("userID") String userID, @RequestParam("userName") String userName, @RequestParam("room") String room, @RequestParam("step") String step) throws IOException, InterruptedException, ExecutionException {
        String url = "";
        ArrayList fileNames = new ArrayList<>();
        ArrayList fileLinks = new ArrayList<>();
        StorageClient storageClient = StorageClient.getInstance();
        int i = 0;

        for (MultipartFile multipartFile : files) {
            File file = convertToFile(multipartFile, multipartFile.getOriginalFilename());
            InputStream is = new FileInputStream(file);
            Blob blob = storageClient.bucket().create("submits/" + room + "/" + userID + "/" + step + "/" + file.getName(), is);
            String newUrl = blob.signUrl(9999, TimeUnit.DAYS).toString();
            url = url + "<a class='text-primary' href = '" + newUrl + "' >" + file.getName() + "</a ><br>";
            fileLinks.add(newUrl);
            fileNames.add(file.getName());
        }

        Firestore dbFirestore = FirestoreClient.getFirestore();  //FireStorage
        DocumentReference userRef = dbFirestore.collection("rooms").document(room).collection("submits").document(userID);

        ApiFuture<DocumentSnapshot> future = userRef.get();
        DocumentSnapshot document = future.get();
        ArrayList<HashMap> submitedSteps = null;

        if (document.exists()) {
            if (document.getData().get("steps") != null) {
                submitedSteps = (ArrayList<HashMap>) document.getData().get("steps");
                Iterator itr = submitedSteps.iterator();
                while (itr.hasNext()) {
                    HashMap hash = (HashMap) itr.next();
                    if (hash.get("step").equals("" + step))
                        itr.remove();
                }
            }

        }
        if (submitedSteps == null) {
            submitedSteps = new ArrayList<>();
        }
        HashMap mapSubmit = new HashMap();
        mapSubmit.put("step", step);
        mapSubmit.put("comment", "");
        mapSubmit.put("fileNames", fileNames);
        mapSubmit.put("fileLinks", fileLinks);
        submitedSteps.add(mapSubmit);

        Collections.sort(submitedSteps, Comparator.comparing(o -> o.get("step").toString()));

        HashMap map = new HashMap<>();
        map.put("userName", userName);
        map.put("lastUpdate", FieldValue.serverTimestamp());
        map.put("steps", submitedSteps);
        userRef.set(map);

        return ResponseEntity.ok().body("<p>File đã nộp: <br>" + url);

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
                map.get(log.getStep()).setNumber(1);
            }
            User user = document.toObject(User.class);
            user.setUserID(document.getId());
            String step = "";
            for (int i = 0; i < room.getNumberOfStep(); i++) {
                if (map.get(i).getNumber() == 1) {
                    step = step + "<td><span class ='labStep blue' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span></td>";
                } else {
                    step = step + "<td><span class ='labStep' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span></td>";
                }
            }

            s = s + "<tr><td>" + user.getUserName() + "</td><td>" + step + "</td></tr>";
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
                Log log = document1.toObject(Log.class);
                Step cStep = map.get(log.getLeave());
                if (log.getDuration() > 0) {
                    System.out.println();
                }
                cStep.setNumber(cStep.getNumber() + log.getDuration());
            }
            User user = document.toObject(User.class);
            user.setUserID(document.getId());
            String step = "";
            for (int i = 0; i < room.getNumberOfStep(); i++) {
                if (map.get(i).getNumber() > 10 * 60) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize3' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='hideSmall report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else if (map.get(i).getNumber() > 6 * 60) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize2' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='hideSmall report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else if (map.get(i).getNumber() > 3 * 60) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue labStepSize1' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='hideSmall report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else if (map.get(i).getNumber() > 15) {
                    step = step + "<td class='tdcenter'><span class ='labStep blue' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='hideSmall report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                } else {
                    step = step + "<td class='tdcenter'><span class ='labStep' id=" + user.getUserID() + "_" + i + ">" + (i + 1) + "</span><span class='hideSmall report-detail d-none'>" + map.get(i).getNumber() + "s</span></td>";
                }
            }

            s = s + "<tr><td>" + user.getUserName() + "</td><td>" + step + "</td></tr>";
        }

        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setMsg(s);
        return ResponseEntity.ok().

                body(ajaxResponseBody);

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

        StorageClient storageClient = StorageClient.getInstance();  //Storage
        for (Element el : img) {
            File file = new File(folderName + "/" + el.attr("src"));
            InputStream is = new FileInputStream(file);
            Blob blob = storageClient.bucket().create("labs/" + newLab.getUserID() + "/" + folderName + "/" + file.getName(), is);
            String newUrl = blob.signUrl(9999, TimeUnit.DAYS).toString();
            el.attr("src", newUrl);
        }

        Element codelab = doc.getElementsByTag("google-codelab").get(0);
        newLab.setHtml(codelab.toString());
        labService.save(newLab);
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setUpdate(true);
        return ResponseEntity.ok().body(ajaxResponseBody);
    }

    @GetMapping("/lab/{labID}")
    public String lab(Model model, @PathVariable(name = "labID") String labID, @RequestParam("room") String room, @RequestParam("createdBy") String createdBy) {
        model.addAttribute("lab", labService.getByID(labID));
        Firestore db = FirestoreClient.getFirestore();  //FireStorage
        HashMap map = new HashMap();
        map.put("createdBy", createdBy);
        map.put("labID", labID);
        db.collection("rooms").document(room).set(map);
        return "lab";
    }

    @PostMapping("/createRoom")
    public ResponseEntity<?> createRoom(@RequestBody Room room) throws ExecutionException, InterruptedException {
        String s = roomService.save(room);
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ajaxResponseBody.setUpdate(true);
        return ResponseEntity.ok().body(ajaxResponseBody);
    }

    @GetMapping("/room/{roomID}")
    public String room(Model model, @PathVariable(name = "roomID") String roomID) {
        Room room = roomService.getByID(roomID);
        model.addAttribute("lab", labService.getByID(room.getDocID()));
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
