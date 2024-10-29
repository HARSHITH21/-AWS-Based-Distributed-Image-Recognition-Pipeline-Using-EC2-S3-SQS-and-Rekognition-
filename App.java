package com.mycompany.app;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.List;

public class App {
    private static final String S3_BUCKET = "njit-cs-643"; 
    private static final String SQS_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/065870645303/CarImageQueue"; 
    public static void main(String[] args) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("us-east-1").build();
        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();

        try {
            //  List of images to process
            String[] images = {"1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg", "6.jpg", "7.jpg", "8.jpg", "9.jpg", "10.jpg"};
            for (String image : images) {
                detectCarAndSendMessage(rekognitionClient, sqsClient, S3_BUCKET, image);
            }
        } finally {
            // Shutdown clients
            s3Client.shutdown();
            rekognitionClient.shutdown();
            sqsClient.shutdown();
        }
    }

    private static void detectCarAndSendMessage(AmazonRekognition rekognition, AmazonSQS sqsClient, String bucket, String key) {
        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withS3Object(new S3Object().withBucket(bucket).withName(key)))
                .withMaxLabels(10)
                .withMinConfidence(90F);

        DetectLabelsResult result = rekognition.detectLabels(request);
        List<Label> labels = result.getLabels();

        boolean carDetected = false;

        for (Label label : labels) {
            if (label.getName().equalsIgnoreCase("Car")) {
                System.out.printf("Car detected in: %s with confidence: %.2f%%\n", key, label.getConfidence());
                sendMessage(sqsClient, key); // Send message if car is detected
                carDetected = true;
                break; // Exit loop after finding a car
            }
        }

        if (!carDetected) {
            System.out.printf("No car detected in: %s\n", key);
        }
    }

    private static void sendMessage(AmazonSQS sqsClient, String imageName) {
        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                .withQueueUrl(SQS_QUEUE_URL)
                .withMessageBody(imageName);
        sqsClient.sendMessage(sendMsgRequest);
    }
}

