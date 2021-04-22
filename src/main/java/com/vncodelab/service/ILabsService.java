//
package com.vncodelab.service;

import com.vncodelab.entity.Lab;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ILabsService {

    List<Lab> getFeatureLabsByCate(String cateID);
    void saveObjectFirebase(Lab lab);
    Lab getLab(String docID);

}
