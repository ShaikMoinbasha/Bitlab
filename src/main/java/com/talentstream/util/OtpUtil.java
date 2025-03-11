package com.talentstream.util;
import java.util.Random;

import org.springframework.stereotype.Service;
@Service
public class OtpUtil {
	private Random random = new Random();
	public String generateOtp() {
        
        int randomNumber = random.nextInt(9000) + 1000;
        return Integer.toString(randomNumber);
    }

}
