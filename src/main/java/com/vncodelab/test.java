package com.vncodelab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class test {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(System.getProperty("user.home") + "/go/bin/claat export 1rz-UJcd5wQ-giAdIm81bEQoT94xuUJwTj5eik_8LDA4");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line = input.readLine();
        p.waitFor();
    }
}