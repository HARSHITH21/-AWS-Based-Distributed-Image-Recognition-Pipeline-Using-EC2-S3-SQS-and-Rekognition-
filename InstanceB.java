package com.mycompany.app;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class InstanceB {

    public static void main(String[] args) {
        // Initialize AWS clients
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
                .withRegion("us-east-1").build();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1").build();

        AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion("us-east-1").build();

        String bucketName = "njit-cs-643";
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/065870645303/CarImageQueue"; 

        // Use a Set to track processed images and avoid duplicates
        HashSet<String> processedImages = new HashSet<>();

        try (FileWriter writer = new FileWriter("/home/ec2-user/output7.txt", true)) {
            boolean done = false;

            while (!done) {
                ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest(queueUrl)
                        .withMaxNumberOfMessages(10) // Get up to 10 messages at once
                        .withWaitTimeSeconds(10); // Long polling

                List<Message> messages = sqsClient.receiveMessage(receiveRequest).getMessages();

                if (messages.isEmpty()) {
                    done = true;  // Exit if there are no more messages
                } else {
                    for (Message message : messages) {
                        String imageKey = message.getBody();

                        if (imageKey.equals("-1")) {
                            done = true;  // Exit the loop if no more messages
                            break;
                        }

                        // Check if the image has already been processed
                        if (processedImages.add(imageKey)) {
                            String detectedText = detectText(rekognitionClient, bucketName, imageKey, writer);
                            writer.write(imageKey + ": " + detectedText + "\n");
                            System.out.println("Processed text for: " + imageKey);
                        }

                        // Delete the message from SQS after processing
                        sqsClient.deleteMessage(queueUrl, message.getReceiptHandle());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Properly shut down clients to prevent lingering threads
            sqsClient.shutdown();
            s3Client.shutdown();
            rekognitionClient.shutdown();
        }
    }

    public static String detectText(AmazonRekognition rekognitionClient, String bucketName, String imageKey, FileWriter writer) throws IOException {
        // Create an S3Object for the image
        S3Object s3Object = new S3Object().withBucket(bucketName).withName(imageKey);

        DetectTextRequest request = new DetectTextRequest()
                .withImage(new Image().withS3Object(s3Object));

        DetectTextResult result = rekognitionClient.detectText(request);
        StringBuilder detectedText = new StringBuilder();

        // Check if any text detections were found
        if (result.getTextDetections() != null && !result.getTextDetections().isEmpty()) {
            for (TextDetection textDetection : result.getTextDetections()) {
                detectedText.append(textDetection.getDetectedText()).append(" ");
                String outputLine = "Detected text: " + textDetection.getDetectedText() + 
                                    " with confidence: " + textDetection.getConfidence().toString() + "%";
                System.out.println(outputLine);
                writer.write(outputLine + "\n"); // Write to the output file
            }
        } else {
            String noTextDetected = "No text detected in " + imageKey;
            System.out.println(noTextDetected);
            writer.write(noTextDetected + "\n"); // Write to the output file
        }

        return detectedText.toString().trim();
    }
}

