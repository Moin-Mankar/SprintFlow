package com.sprintflow.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() {

        if(!FirebaseApp.getApps().isEmpty()){
            return;
        }

        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.getApplicationDefault()
                    )
                    .build();

            FirebaseApp.initializeApp(options);

            System.out.println("Firebase initialized successfully");
        }catch (Exception e){
            throw new IllegalStateException(
                    "Failed to initialize Firebase",e
            );
        }
    }
}