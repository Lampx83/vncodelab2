//
package com.vncodelab.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.entity.Lab;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class LabService {


    public List<Lab> getFeatureLabsByCate(String cateID) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Lab> list = new ArrayList<>();
        ApiFuture<QuerySnapshot> future;
        if (cateID == null)
            future = dbFirestore.collection("labs").whereEqualTo("feature", true).get();
        else
            future = dbFirestore.collection("labs").whereEqualTo("feature", true).whereEqualTo("cateID", cateID).get();
        List<QueryDocumentSnapshot> documents = null;
        try {
            documents = future.get().getDocuments();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for (QueryDocumentSnapshot document : documents) {
            Lab lab = document.toObject(Lab.class);
            list.add(lab);
        }
        return list;
    }


    public void save(Lab lab) {
        //Luu ban ghi cho Lab

        FirestoreClient.getFirestore().collection("labs").document(lab.getDocID()).set(lab);
        //Luu ban ghi cho Users
        HashMap<String, Object> map = new HashMap<>();
        map.put("lastUsed", FieldValue.serverTimestamp());
        map.put("description", lab.getDescription());
        map.put("userID", lab.getUserID());
        map.put("order", 999);

        FirestoreClient.getFirestore().collection("labs").document(lab.getDocID()).collection("users").document(lab.getUserID()).set(map);
    }

    public void delete(Lab lab) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        //Kiểm tra xem có bao nhiêu người?
        CollectionReference collection = db.collection("labs").document(lab.getDocID()).collection("users");
        ApiFuture<QuerySnapshot> future = collection.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        int numberOfUser = documents.size();

        //Xoa nguoi dung khoi danh sach su dung labs
        collection.document(lab.getUserID()).delete();
        if (numberOfUser == 1) { //Neu chi con 1 nguoi
            //Xoa trong firestore labs
            db.collection("labs").document(lab.getDocID()).delete();
        }
        //Xoa rooms
        Query query = db.collection("rooms").whereEqualTo("docID", lab.getDocID());
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            document.getReference().delete();
        }
    }

    public Lab getByID(String docID) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference docRef = dbFirestore.document("labs/" + docID);
        Lab lab = null;
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            lab = document.toObject(Lab.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lab;
    }

}
