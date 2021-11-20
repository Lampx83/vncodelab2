//
package com.vncodelab.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.entity.Log;
import com.vncodelab.entity.Room;
import org.apache.poi.ss.usermodel.Cell;
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
import java.util.List;
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

    public ResponseEntity<InputStreamResource> genExcel(String roomID) {

        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet("Test");
            int rowCount = 0;
            int columnCount = 0;
            Row row = sheet.createRow(rowCount);
            Cell cell = row.createCell(++columnCount);

            Firestore dbFirestore = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = dbFirestore.collection("rooms/" + roomID + "/logs").get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                Log log = document.toObject(Log.class);




            }

            Room room = getByID(roomID);
            cell.setCellValue(room.getDocID());
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

}
