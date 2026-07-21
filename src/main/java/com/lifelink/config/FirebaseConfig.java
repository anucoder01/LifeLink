package com.lifelink.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount;
            if (firebaseConfigPath.startsWith("classpath:")) {
                serviceAccount = getClass().getClassLoader().getResourceAsStream(firebaseConfigPath.replace("classpath:", ""));
            } else {
                serviceAccount = new FileInputStream(ResourceUtils.getFile(firebaseConfigPath));
            }
            
            if (serviceAccount == null) {
                System.out.println("Firebase config file not found. Firebase will not be initialized.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
        }
    }
}
