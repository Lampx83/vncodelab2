//
package com.vncodelab.service;

import com.vncodelab.entity.Lab;

import java.io.IOException;
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
public interface ILabsService {

    List<Lab> getObjectFirebase() throws InterruptedException, ExecutionException;

    void saveObjectFirebase(Lab lab) throws IOException;

    Lab getLab(String docID);

}
