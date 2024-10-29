
# How-To Guide for AWS-Based Distributed Image Recognition Pipeline

## 1. Prerequisites
- **AWS Account**: Ensure you have access to AWS services such as EC2, S3, SQS, and Rekognition.
- **SSH Key**: Download and secure the `.pem` file for SSH access to EC2 instances.
- **Java and AWS SDK**: Install Java and the AWS SDK on both EC2 instances.

## 2. EC2 Setup
### Step 1: Launch EC2 Instances
1. Create two Amazon Linux EC2 instances (Instance A and Instance B) with the same security group and `.pem` key.
2. Configure the **Security Group** to allow access only from “MYIP” and open ports for SSH, HTTP, and HTTPS.

### Step 2: SSH Access
1. SSH into each instance:
   ```bash
   ssh -i "your-key.pem" ec2-user@<public-ip-of-instance>
   ```
2. Update each instance:
   ```bash
   sudo yum update -y
   ```

### Step 3: Install Required Software
1. **Install Java**:
   ```bash
   sudo yum install java-11-amazon-corretto -y
   ```
2. **Install AWS CLI**:
   ```bash
   sudo yum install aws-cli -y
   ```
3. **Install Maven** (for Java project management):
   ```bash
   sudo yum install maven -y
   ```

## 3. AWS Configuration
1. **AWS CLI Configuration**:
   ```bash
   aws configure
   ```
   - Enter your AWS `access-key`, `secret-key`, region (e.g., `us-east-1`), and output format (e.g., `json`).
   
2. **AWS Credentials**:
   - For AWS Educate accounts, refresh credentials every 3 hours by updating the `~/.aws/credentials` file with new keys from Vocareum.

## 4. Project Setup
### Step 1: Initialize Maven Project on EC2 A
1. **Create Maven Project**:
   ```bash
   mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=aws-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
   ```
2. **Navigate to Project Directory**:
   ```bash
   cd aws-app
   ```
3. **Add AWS Dependencies**:
   Open `pom.xml` and add dependencies for S3, SQS, and Rekognition.

### Step 2: Write Java Code
1. **Instance A (Car Detection)**:
   - Create a Java file `App.java` in `src/main/java/com/mycompany/app/`.
   - Implement code to:
     - Read images from the S3 bucket.
     - Detect objects using AWS Rekognition.
     - Send image indexes to SQS if a car is detected with 90%+ confidence.

2. **Instance B (Text Recognition)**:
   - Create a Java file `InstanceB.java` with code to:
     - Listen for messages from SQS.
     - Perform text recognition on received image indexes.
     - Write results (image index and detected text) to a file in EBS storage.

### Step 3: Compile and Run
1. **Compile**:
   ```bash
   mvn clean install
   ```
2. **Run Application on Instance A**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.mycompany.app.App"
   ```
3. **Run Application on Instance B**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.mycompany.app.InstanceB"
   ```

## 5. Workflow Overview
1. **Instance A** processes images in the S3 bucket. When a car is detected, it adds the image index to SQS. Upon completion, it sends `-1` to SQS to signal termination.
2. **Instance B** retrieves image indexes from SQS, performs text recognition, and writes results to `output1.txt`. It stops upon receiving `-1`.

## 6. Termination
Terminate both EC2 instances once processing is complete to avoid extra charges.

