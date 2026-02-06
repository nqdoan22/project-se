# Deployment Guide

## Backend Deployment

1. **Prerequisites**:
   - Ensure you have Java 17 or later installed.
   - Install Gradle if not already installed.
   - Ensure you have access to the production database and its credentials.

2. **Build the Backend**:
   - Navigate to the backend directory:
     ```bash
     cd /Users/newbie/Documents/GitHub/project-se/02_Source/01_Source Code/backend
     ```
   - Build the project using Gradle:
     ```bash
     ./gradlew build
     ```

3. **Run the Backend**:
   - Start the application:
     ```bash
     ./gradlew bootRun
     ```
   - The backend will be available at `http://localhost:8080` by default.

4. **Deploy to Production**:
   - Package the application as a JAR file:
     ```bash
     ./gradlew bootJar
     ```
   - Locate the generated JAR file in the `build/libs/` directory.
   - Transfer the JAR file to the production server.
   - Run the JAR file on the production server:
     ```bash
     java -jar <your-jar-file-name>.jar
     ```

## Frontend Deployment

1. **Prerequisites**:
   - Ensure you have a Vercel account.
   - Install Vercel CLI if not already installed:
     ```bash
     npm install -g vercel
     ```

2. **Deploy to Vercel**:
   - Navigate to the frontend directory:
     ```bash
     cd /Users/newbie/Documents/GitHub/project-se/02_Source/01_Source Code/frontend
     ```
   - Login to Vercel:
     ```bash
     vercel login
     ```
   - Deploy the project:
     ```bash
     vercel --prod
     ```
   - Follow the prompts to complete the deployment.

3. **Access the Frontend**:
   - Once deployed, Vercel will provide a URL for your frontend application. Share this URL with your users.