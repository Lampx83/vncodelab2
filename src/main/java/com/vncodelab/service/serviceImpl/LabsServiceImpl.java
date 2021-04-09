//
package com.vncodelab.service.serviceImpl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.entity.Lab;
import com.vncodelab.service.ILabsService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class is .
 *
 * @Description: .
 * @author: NVAnh
 * @create_date: Feb 19, 2021
 * @version: 1.0
 * @modifer: NVAnh
 * @modifer_date: Feb 19, 2021
 */
@Service
public class LabsServiceImpl implements ILabsService {

    @Override
    public List<Lab> getObjectFirebase() throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<Lab> list = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("labs").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            Lab lab = document.toObject(Lab.class);
            lab.setLabID(document.getId());
            list.add(lab);
        }
        return list;
    }


    @Override
    public void saveObjectFirebase(Lab lab) throws IOException {
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
