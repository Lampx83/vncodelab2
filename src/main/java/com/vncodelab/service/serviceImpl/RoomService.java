//
package com.vncodelab.service.serviceImpl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.entity.Room;
import org.springframework.stereotype.Service;

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


}
