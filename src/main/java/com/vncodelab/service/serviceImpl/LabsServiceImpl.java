//
package com.vncodelab.service.serviceImpl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.entity.LabF;
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
    public List<LabF> getObjectFirebase() throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<LabF> list = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("labs").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            LabF labF = document.toObject(LabF.class);
            labF.setLabID(document.getId());
            list.add(labF);
        }
        return list;
    }


    @Override
    public void saveObjectFirebase(LabF lab) throws IOException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection("labs").document(lab.getDocID()).set(lab);


    }

    @Override
    public LabF getLab(String docID) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference docRef = dbFirestore.document("labs/" + docID);
        LabF labF = null;
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            labF = document.toObject(LabF.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return labF;
    }

}
