package com.vncodelab;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class test {
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            Process p = Runtime.getRuntime().exec("/home/phamxuanlam/go/bin/claat export 1rz-UJcd5wQ-giAdIm81bEQoT94xuUJwTj5eik_8LDA4");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = input.readLine();
            p.waitFor();
            System.out.println("Done87");
            String folderName = line.split("\t")[1];
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(folderName + "/codelab.json")));
            String totalLine = "";
            while ((line = br.readLine()) != null)
                totalLine = totalLine + line;
            System.out.println("Done93");
        } catch (Exception ex) {
            System.out.println("Exception");
            ex.printStackTrace();
        }
    }
}