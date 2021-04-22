//
package com.vncodelab.service.serviceImpl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.entity.Lab;
import com.vncodelab.service.ILabsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class LabsServiceImpl implements ILabsService {

    @Override
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


    @Override
    public void saveObjectFirebase(Lab lab) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection("labs").document(lab.getDocID()).set(lab);


    }

    @Override
    public Lab getLab(String docID) {
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
