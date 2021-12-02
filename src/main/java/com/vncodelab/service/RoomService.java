//
package com.vncodelab.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.entity.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class RoomService {

    public String save(Room room) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = dbFirestore.collection("rooms").document(room.getRoomID()).set(room);
        return future.get().getUpdateTime().toString();
    }

    public Room getByID(String roomID) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference docRef = dbFirestore.document("rooms/" + roomID);
        Room room = null;
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            room = document.toObject(Room.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return room;
    }

    public void delete(Room room) {
        Firestore db = FirestoreClient.getFirestore();
        deleteLogs(db.collection("rooms").document(room.getRoomID()).collection("logs"), 100); //2 Level
        deleteCollection(db.collection("rooms").document(room.getRoomID()).collection("submits"), 100);  //1 Level
        db.collection("rooms").document(room.getRoomID()).delete();
    }

    void deleteLogs(CollectionReference collection, int batchSize) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
                deleteCollection(document.getReference().collection("hands"), 100);
                deleteCollection(document.getReference().collection("steps"), 100);
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteLogs(collection, batchSize);
            }
        } catch (Exception e) {
            System.err.println("Error deleting collection : " + e.getMessage());
        }
    }

    public void deleteUserReport(Room room) {
        Firestore db = FirestoreClient.getFirestore();
        deleteCollection(db.collection("rooms").document(room.getRoomID()).collection("logs").document(room.getUserID()).collection("steps"), 100);
        deleteCollection(db.collection("rooms").document(room.getRoomID()).collection("logs").document(room.getUserID()).collection("hands"), 100);
        db.collection("rooms").document(room.getRoomID()).collection("logs").document(room.getUserID()).delete();
        db.collection("rooms").document(room.getRoomID()).collection("submits").document(room.getUserID()).delete();
    }

    void deleteCollection(CollectionReference collection, int batchSize) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
                deleteCollection(document.getReference().collection("hands"), 100);
                deleteCollection(document.getReference().collection("steps"), 100);
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteCollection(collection, batchSize);
            }
        } catch (Exception e) {
            System.err.println("Error deleting collection : " + e.getMessage());
        }
    }

    public ResponseEntity<InputStreamResource> genExcel(ArrayList<RoadMap> roadMap) {
        XSSFWorkbook wb = new XSSFWorkbook();
        HashMap<String, HashMap<String, StudentInfo>> roomMap = new HashMap<>();  //room  //User id   //Student Info
        int i = 1;
        for (RoadMap roomList : roadMap) {
            if (roomList.getLab() != null)
                for (Room room : roomList.getLab()) {
                    HashMap<String, StudentInfo> info = genRoomReport(room.getRoomID(), wb);
                    roomMap.put(room.getRoomID(), info);
                    System.out.println(i++ + " Done " + room.getRoomID());
                }
        }
        XSSFSheet sheet = wb.createSheet("Report");
        //    wb.setSheetOrder("Report", 0);
        int columnCount = 0;

        HashMap<String, HashMap<String, StudentInfo>> studentMap = new HashMap<>();  //studentID  //room id   //duration

        for (String roomID : roomMap.keySet()) {
            HashMap<String, StudentInfo> roomInfo = roomMap.get(roomID);  //Thong tin 1 phong

            for (String userID : roomInfo.keySet()) {
                HashMap<String, StudentInfo> studentInfo = studentMap.get(userID);
                if (studentInfo == null) {
                    studentInfo = new HashMap<>();
                    studentMap.put(userID, studentInfo);
                }
                studentInfo.put(roomID, roomInfo.get(userID));
            }
        }

        for (String userID : studentMap.keySet()) {
            HashMap<String, StudentInfo> studentInfo = studentMap.get(userID);
            for (String roomID : roomMap.keySet()) {
                if (studentInfo.get(roomID) == null)
                    studentInfo.put(roomID, new StudentInfo());
            }
        }

        boolean first = true;
        int rowCount = 0;
        for (String userID : studentMap.keySet()) {  //Duyet sinh vien
            HashMap<String, StudentInfo> studentInfo = studentMap.get(userID);
            columnCount = 0;
            if (first) {
                Row header = sheet.createRow(rowCount++);
                Row row = sheet.createRow(rowCount++);
                header.createCell(columnCount).setCellValue("No");
                row.createCell(columnCount++).setCellValue(rowCount - 1);
                header.createCell(columnCount).setCellValue("Name");
                row.createCell(columnCount++).setCellValue("None");
                header.createCell(columnCount).setCellValue("Email");

                String email = null;
                for (Map.Entry<String, StudentInfo> entry : studentInfo.entrySet())
                    if (entry.getValue().getEmail() != null) {
                        email = entry.getValue().getEmail();
                        break;
                    }

                row.createCell(columnCount++).setCellValue(email);
                for (String roomID : studentInfo.keySet()) {
                    header.createCell(columnCount).setCellValue(roomID);
                    if (studentInfo.get(roomID) != null)
                        row.createCell(columnCount++).setCellValue(studentInfo.get(roomID).getTimeDuration());
                    else {
                        System.out.println(studentInfo);
                    }
                }
                first = false;

            } else {
                Row row = sheet.createRow(rowCount++);
                row.createCell(columnCount++).setCellValue(rowCount - 1);
                row.createCell(columnCount++).setCellValue("None");
                String email = null;
                for (Map.Entry<String, StudentInfo> entry : studentInfo.entrySet())
                    if (entry.getValue().getEmail() != null) {
                        email = entry.getValue().getEmail();
                        break;
                    }
                row.createCell(columnCount++).setCellValue(email);
                for (String roomID : studentInfo.keySet()) {
                    if (studentInfo.get(roomID) != null)
                        row.createCell(columnCount++).setCellValue(studentInfo.get(roomID).getTimeDuration());
                    else {
                        System.out.println(studentInfo);
                    }
                }
            }
        }

        //Auto size all the columns
        for (i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }
        try {
            //Write file
            FileOutputStream fileOut = new FileOutputStream("Report.xlsx");
            wb.write(fileOut);
            fileOut.close();
            wb.close();
            System.out.println("DONE REPORT");
//            ByteArrayOutputStream b = new ByteArrayOutputStream();
//            wb.write(b);
//            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(b.toByteArray()));
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//            headers.add("Pragma", "no-cache");
//            headers.add("Expires", "0");
//            headers.add("Content-Disposition", "attachment; filename= report.xlsx");
//            wb.close();
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .contentLength(b.size())
//                    .contentType(MediaType.parseMediaType("application/octet-stream;charset=UTF-8"))
//                    .body(resource);

            //Xong
            //Gen toan bo

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ResponseEntity<InputStreamResource> genExcel(String roomID) {
        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            genRoomReport(roomID, wb);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            wb.write(b);
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(b.toByteArray()));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Content-Disposition", "attachment; filename= " + roomID + ".xlsx");
            wb.close();
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(b.size())
                    .contentType(MediaType.parseMediaType("application/octet-stream;charset=UTF-8"))
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, StudentInfo> genRoomReport(String roomID, XSSFWorkbook wb) {  //Return UserID //Time duration
        HashMap<String, StudentInfo> info = new HashMap<>();
        try {
            XSSFSheet sheet = wb.createSheet(roomID);
            Firestore dbFirestore = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = dbFirestore.collection("rooms/" + roomID + "/logs").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            ArrayList<Log> listLog = new ArrayList<>();
            documents.forEach(d -> {
                Log log = d.toObject(Log.class);
                if (log.getEmail() != null) {
                    log.setUserID(d.getId());
                    listLog.add(log);
                }
            });
            listLog.sort(Comparator.comparing(Log::getEmail));

            int rowCount = 0;
            Row header = sheet.createRow(0);
            int columnCount = 0;
            header.createCell(columnCount++).setCellValue("No");
            header.createCell(columnCount++).setCellValue("Name");
            header.createCell(columnCount++).setCellValue("Email");

            int step = 0;
            for (Log log : listLog) {
                for (int i = 0; i < 50; i++) {
                    try {
                        Field field = Log.class.getDeclaredField("s" + i);
                        field.setAccessible(true);
                        int s = field.getInt(log);
                        if (s > 0 && step < i) {
                            step = i;
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            //Survey
            HashMap<String, Double> mapAnswer = new HashMap<>();
            future = dbFirestore.collection("rooms/" + roomID + "/surveys").get();
            documents = future.get().getDocuments();
            ArrayList<Survey> listSurvey = new ArrayList<>();
            documents.forEach(d -> {
                Survey survey = d.toObject(Survey.class);
                if (survey.getAnswers() != null) {
                    ArrayList<String> answers = survey.getAnswers();
                    for (String answer : answers) {
                        String userID = answer.split(" - ")[0];
                        if (mapAnswer.get(userID) == null)
                            mapAnswer.put(userID, 1.0);
                        else
                            mapAnswer.put(userID, mapAnswer.get(userID) + 1);
                    }
                }
            });

            System.out.println(step);
            int numberofStep = 0;
            for (Log log : listLog) {
                columnCount = 0;
                Row row = sheet.createRow(++rowCount);
                //STT
                row.createCell(columnCount++).setCellValue(String.valueOf(rowCount));
                //Name
                row.createCell(columnCount++).setCellValue(log.getUserName());
                //Email
                row.createCell(columnCount++).setCellValue(log.getEmail());
                int tongThoigian = 0;

                for (int i = 0; i < step; i++) {
                    header.createCell(columnCount).setCellValue("S" + (i + 1) + " (min)");
                    try {
                        Field field = Log.class.getDeclaredField("s" + i);
                        field.setAccessible(true);
                        int duration = field.getInt(log);
                        row.createCell(columnCount++).setCellValue((double) Math.round(duration / 60.0f * 10d) / 10d);
                        tongThoigian = tongThoigian + duration;
                        if (numberofStep < i)
                            numberofStep = i;
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                double time_duration = (double) Math.round(tongThoigian / 60.0f * 10d) / 10d;
                row.createCell(columnCount++).setCellValue(time_duration);
                StudentInfo studentInfo = new StudentInfo();
                studentInfo.setTimeDuration(time_duration);
                studentInfo.setEmail(log.getEmail());
                info.put(log.getUserID(), studentInfo);
                if (mapAnswer.get(log.getUserID()) != null)
                    row.createCell(columnCount++).setCellValue(mapAnswer.get(log.getUserID()));
                else
                    row.createCell(columnCount++).setCellValue(0);
            }


            header.createCell(4 + numberofStep).setCellValue("Total Time (min)");

            header.createCell(5 + numberofStep).setCellValue("Total Answer");

            //Auto size all the columns
            for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
                sheet.autoSizeColumn(i);
            }


            ByteArrayOutputStream b = new ByteArrayOutputStream();
            wb.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
