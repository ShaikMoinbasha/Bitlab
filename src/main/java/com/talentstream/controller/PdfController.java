package com.talentstream.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.talentstream.AwsSecretsManagerUtil;
import com.talentstream.entity.Applicant;
import com.talentstream.exception.CustomException;
import com.talentstream.repository.ApplicantRepository;
import com.talentstream.repository.RegisterRepository;

@RestController
@RequestMapping("/resume")
public class PdfController {

    @Autowired
    private RegisterRepository registerRepo;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private AwsSecretsManagerUtil secretsManagerUtil;

    
    private String bucketName;

    private String getSecret() {
        return AwsSecretsManagerUtil.getSecret();
    }

    private AmazonS3 initializeS3Client() {
       
        String secret = getSecret();

        JSONObject jsonObject = new JSONObject(secret);
        String accessKey = jsonObject.getString("AWS_ACCESS_KEY_ID");
        String secretKey = jsonObject.getString("AWS_SECRET_ACCESS_KEY");
        bucketName = jsonObject.getString("AWS_S3_BUCKET_NAME");
        String region = jsonObject.getString("AWS_REGION");
        
        
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(region))
                .build();
    }
    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable long id) {
        Applicant applicant = registerRepo.findById(id);
       
        if (applicant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        String resumeId = applicant.getResumeId();
        String fileKey = resumeId + ".pdf";
       
        try {
            AmazonS3 s3Client = initializeS3Client();
            
            if (!s3Client.doesObjectExist(bucketName, fileKey)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            S3Object s3Object = s3Client.getObject(bucketName, fileKey);
            InputStream inputStream = s3Object.getObjectContent();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", fileKey);
            byte[] content = inputStream.readAllBytes();
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<String> uploadFile(@RequestParam("resume") MultipartFile file, @PathVariable long id) {
        if (file.getSize() > 1 * 1024 * 1024) {
            throw new CustomException("File size should be less than 1MB.", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            throw new CustomException("Only PDF file types are allowed.", HttpStatus.BAD_REQUEST);
        }

        Applicant applicant = registerRepo.findById(id);
        
        
        if (applicant == null) {
            throw new CustomException("Applicant not found for ID: " + id, HttpStatus.NOT_FOUND);
        }

        String resumeId = applicant.getResumeId();
        String fileKey = resumeId + ".pdf";
        System.out.println(fileKey);

        try {
            AmazonS3 s3Client = initializeS3Client();

            s3Client.putObject(bucketName, fileKey, file.getInputStream(), new ObjectMetadata());

            String fileUrl = s3Client.getUrl(bucketName, fileKey).toString();
            Optional<Applicant> applicantOpt = applicantRepository.findById(id);

            if (applicantOpt.isPresent()) {
                Applicant applicant1 = applicantOpt.get();
                applicant1.setLocalResume(true);
                applicantRepository.save(applicant1);
            }

            return ResponseEntity.ok("File uploaded successfully. S3 URL: " + fileUrl);
        } catch (AmazonServiceException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }
}

