package com.om.blog.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}")
    private String cloudName;

    @Value("${cloudinary.api-key:${CLOUDINARY_API_KEY:}}")
    private String apiKey;

    @Value("${cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Resolve credentials prioritizing environment variables
        String cName = System.getenv("CLOUDINARY_CLOUD_NAME");
        if (cName == null || cName.isBlank()) {
            cName = cloudName;
        }

        String aKey = System.getenv("CLOUDINARY_API_KEY");
        if (aKey == null || aKey.isBlank()) {
            aKey = apiKey;
        }

        String aSecret = System.getenv("CLOUDINARY_API_SECRET");
        if (aSecret == null || aSecret.isBlank()) {
            aSecret = apiSecret;
        }

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cName != null ? cName.trim() : "");
        config.put("api_key", aKey != null ? aKey.trim() : "");
        config.put("api_secret", aSecret != null ? aSecret.trim() : "");
        config.put("secure", true);

        return new Cloudinary(config);
    }
}
