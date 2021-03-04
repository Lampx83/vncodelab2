//
package com.vncodelab.service;

import com.vncodelab.entity.Home;
import com.vncodelab.entity.LabF;

import java.io.IOException;
import java.util.List;
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
public interface ILabsService {

    List<LabF> getObjectFirebase() throws InterruptedException, ExecutionException;

    void saveObjectFirebase(LabF lab) throws IOException;

    LabF getLab(String docID);

}
