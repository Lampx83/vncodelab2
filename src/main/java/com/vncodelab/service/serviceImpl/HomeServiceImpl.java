//
package com.vncodelab.service.serviceImpl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.vncodelab.constants.FirebaseConstants;
import com.vncodelab.constants.PathConstants;
import com.vncodelab.entity.Home;
import com.vncodelab.service.IHomeService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
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
public class HomeServiceImpl implements IHomeService {

    @Override
    public Map<String, Object> getObjectFirebase() throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference docRef = dbFirestore.document("home/infor");
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = docRef.get();
        return documentSnapshotApiFuture.get().getData();
    }

    @Override
    public Home getInforFirebase(Map<String, Object> objectFirebase) {
        String footers = (String) objectFirebase.get("footers");
        String title = (String) objectFirebase.get("title");
        String logoUrl = (String) objectFirebase.get("logoUrl");
        String description = (String) objectFirebase.get("description");
        Home newInfor = new Home(logoUrl, title, footers, description);



        return newInfor;
    }

    @Override
    public void saveObjectFirebase(Home home) throws IOException {
        home.setLogoUrl("/images/" + home.getImage().getOriginalFilename());

        if (Files.notExists(Paths.get(PathConstants.PATH_LOGO_FOLDER + home.getImage().getOriginalFilename()))) {
            byte[] bytes = home.getImage().getBytes();
            Path path = Paths.get(PathConstants.PATH_LOGO_FOLDER + home.getImage().getOriginalFilename());
            Files.write(path, bytes);
        }

        home.setImage(null);
        Firestore dbFirestore = FirestoreClient.getFirestore();
        dbFirestore.collection(FirebaseConstants.COLLECTION_NAME).document(FirebaseConstants.DOCUMENT_NAME).set(home);
    }



}
