package com.vncodelab.controller;

import com.vncodelab.payload.UploadFileResponse;
import com.vncodelab.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/img/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    @GetMapping("/claat")
    public String claat() {
        try {

//            Scanner s = new Scanner(System.in);
//            System.out.println("Nhap vao mot so");
//            int x = s.nextInt();
//            System.out.println(x);
            Process p = Runtime.getRuntime().exec("/home/phamxuanlam/go/bin/claat export 1rz-UJcd5wQ-giAdIm81bEQoT94xuUJwTj5eik_8LDA4");

       //     Process p = Runtime.getRuntime().exec("./claat export 1rz-UJcd5wQ-giAdIm81bEQoT94xuUJwTj5eik_8LDA4");

            //    Process p = Runtime.getRuntime().exec("java -v");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = input.readLine();
            System.out.println("Dong "+ line);
            p.waitFor();

            return line;
//            System.out.println("Done87");
//            String folderName = line.split("\t")[1];
//            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(folderName + "/codelab.json")));
//            String totalLine = "";
//            while ((line = br.readLine()) != null)
//                totalLine = totalLine + line;
//            System.out.println(totalLine);
//            System.out.println("Done93");

        } catch (Exception ex) {
            System.out.println("Exception");
            ex.printStackTrace();
        }
        return "end";
    }
}