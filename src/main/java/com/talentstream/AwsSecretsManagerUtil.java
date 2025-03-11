package com.talentstream;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;

@Configuration
public class AwsSecretsManagerUtil {
	
	
	private static final Logger LOGGER=LoggerFactory.getLogger(AwsSecretsManagerUtil.class);

	public static String getSecret() {
		  try {
		  String secrets = System.getenv("AWS_ACCESS_KEY_ID");
		  LOGGER.info( secrets);
		 
		  JSONObject jsonObject = new JSONObject(secrets);
	        String accessKey = jsonObject.getString("AWS_ACCESS_KEY_ID");
	        String secretKey = jsonObject.getString("AWS_SECRET_ACCESS_KEY");
	        String region1 = jsonObject.getString("AWS_REGION");
	        Region region = Region.of(region1);
	        
	        
	        if (accessKey == null || secretKey == null) {
	           
	            
	            LOGGER.error("AWS credentials are not set in environment variables.");
	            return null;
	        }
	
	            return secrets;
	        } catch (Exception e) {
	           
	        	LOGGER.error("An error occurred: " + e.getMessage());
	            return null;
	        }
    }

    
}
