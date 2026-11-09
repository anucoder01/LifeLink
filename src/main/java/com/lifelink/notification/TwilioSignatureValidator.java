package com.lifelink.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class TwilioSignatureValidator {
    @Value("${twilio.auth-token:}") private String authToken;
    @Value("${twilio.public-webhook-url:}") private String publicWebhookUrl;

    public boolean isValid(String signature, MultiValueMap<String, String> params) {
        if (authToken.isBlank() || publicWebhookUrl.isBlank() || signature == null) return false;
        try {
            SortedMap<String, String> sorted = new TreeMap<>();
            params.forEach((key, values) -> sorted.put(key, values == null || values.isEmpty() ? "" : values.get(0)));
            StringBuilder data = new StringBuilder(publicWebhookUrl);
            sorted.forEach((key, value) -> data.append(key).append(value));
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(authToken.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] expected = Base64.getEncoder().encode(mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8)));
            byte[] actual = signature.getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }
}
