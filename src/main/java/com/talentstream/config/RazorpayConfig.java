package com.talentstream.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Configuration
public class RazorpayConfig {
 
    private final String razorPayKey;
    private final String razorPaySecret;
 
    @Autowired  // Constructor-based dependency injection
    public RazorpayConfig(@Value("${razorpay.key}") String razorPayKey, 
                          @Value("${razorpay.secret}") String razorPaySecret) {
        this.razorPayKey = razorPayKey;
        this.razorPaySecret = razorPaySecret;
    }

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(razorPayKey, razorPaySecret);
    }

    
    public String getRazorPayKey() {
        return razorPayKey;
    }

    public String getRazorPaySecret() {
        return razorPaySecret;
    }
}
