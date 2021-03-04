package com.vncodelab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class VnCodelabApplication extends SpringBootServletInitializer {
    private static final Logger log = LoggerFactory.getLogger(VnCodelabApplication.class);


    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(VnCodelabApplication.class, args);
    }


}
