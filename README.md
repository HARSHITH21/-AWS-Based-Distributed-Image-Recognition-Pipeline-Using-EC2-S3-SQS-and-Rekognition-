
# AWS-Based Distributed Image Recognition Pipeline Using EC2, S3, SQS, and Rekognition

## Overview
This project demonstrates how to develop a distributed image recognition pipeline using the Amazon AWS cloud platform. The application utilizes existing AWS services including EC2, S3, SQS, and Rekognition to perform object detection and text recognition on images stored in an S3 bucket. This assignment showcases the power of cloud-based distributed computing by using two Amazon Linux EC2 instances that work in parallel to process images, leveraging AWS Rekognition for image analysis.

## Project Structure
This project comprises two Amazon EC2 instances:
1. **Instance A**: Responsible for object detection.
2. **Instance B**: Performs text recognition on images detected to contain certain objects (e.g., cars) based on criteria set in instance A.

## Objectives
Through this project, you will learn:
1. How to create and manage VMs (EC2 instances) in AWS.
2. How to utilize S3 as cloud storage for application data.
3. How to communicate between VMs using AWS SQS as a message queue.
4. How to program distributed Java applications on Amazon Linux VMs.
5. How to utilize AWS Rekognition for object detection and text recognition in images.

## Workflow
1. **Setup**:
   - Create two Amazon Linux EC2 instances (Instance A and Instance B).
   - Ensure both instances use the same `.pem` key for SSH access.
   - Configure the Security Group to allow access only from “MYIP” and open ports for SSH, HTTP, and HTTPS.
   - Use free-tier instances to avoid unnecessary charges. Terminate instances after completing the assignment to prevent additional costs.

2. **Credentials**:
   - Use AWS access keys for authentication to connect and communicate with S3, SQS, and Rekognition.
   - For AWS Educate users, credentials need to be refreshed every 3 hours due to session expiration.

3. **Image Processing Pipeline**:
   - **Instance A** reads 10 images from a designated S3 bucket (`https://njit-cs-643.s3.us-east-1.amazonaws.com`).
   - Instance A processes each image to detect objects using AWS Rekognition.
   - If a "car" is detected with over 90% confidence, the index of the image (e.g., `2.jpg`) is stored in an SQS queue.
   - **Instance B** retrieves image indexes from the SQS queue as soon as they are available and performs text recognition on the images.
   - When Instance A completes processing, it adds a termination signal (`-1`) to the queue to signal Instance B to stop processing.

4. **Output**:
   - Instance B saves the results to a file in its associated EBS storage. The file contains:
     - Image indexes with both cars and recognized text.
     - The actual text detected in each image alongside its index.

## Setup and Installation
### Prerequisites
- **AWS Account**: Access to AWS services like EC2, S3, SQS, and Rekognition.
- **Java**: Ensure Java is installed on both EC2 instances.
- **AWS SDK**: Install AWS SDK for Java on both EC2 instances to enable interaction with AWS services.
- **Amazon Linux**: Use Amazon Linux AMI for both EC2 instances.

### Step-by-Step Setup
1. **EC2 Instances**:
   - Create two EC2 instances with Amazon Linux AMI.
   - Set up SSH access using the same `.pem` key.
   - Configure security settings to allow access only from “MYIP” and open necessary ports.

2. **AWS Credentials**:
   - Set up AWS credentials by copying your AWS access keys into the `~/.aws/credentials` file on both instances.
   - For AWS Educate users, refresh credentials every 3 hours.

3. **Install Java and AWS SDK**:
   - Install Java on both EC2 instances if not already installed.
   - Download and configure the AWS SDK for Java to allow the program to interact with AWS services.

4. **S3 and SQS Configuration**:
   - **S3**: Ensure images are available in the designated S3 bucket (`https://njit-cs-643.s3.us-east-1.amazonaws.com`).
   - **SQS**: Set up an SQS queue to communicate between Instance A and Instance B.

### Running the Application
1. **Instance A**:
   - Run the Java application on Instance A to read images from the S3 bucket.
   - Use AWS Rekognition to detect objects. If a car is detected with 90%+ confidence, add the image index to the SQS queue.
   - When all images are processed, add the termination signal (`-1`) to the queue.

2. **Instance B**:
   - Run the Java application on Instance B to listen to the SQS queue for image indexes.
   - For each image index received, download the image from S3 and use AWS Rekognition for text recognition.
   - Save the results (image index and recognized text) to a file in Instance B's EBS storage.

3. **Termination**:
   - When Instance B reads the termination signal (`-1`), it stops processing and completes the output file.

## Security Considerations
- Restrict access to instances by setting the Security Group’s Source to “MYIP.”
- Open only the necessary ports (SSH, HTTP, HTTPS).
- Ensure `.pem` keys and AWS credentials are securely stored and handled.

## License
This project is for educational purposes. Ensure compliance with AWS usage policies, particularly for free-tier resources to avoid unintended charges.

