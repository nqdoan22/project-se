# Setting Up Development Environment on Local Machine

Follow these steps to set up the development environment for this project on your local machine:

## Prerequisites

1. **Install Java Development Kit (JDK):**
   - Ensure you have JDK 17 or later installed. You can download it from [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or use [OpenJDK](https://openjdk.org/).
   - Verify installation by running `java -version` in your terminal.

2. **Install Node.js and npm:**
   - Download and install the latest LTS version of Node.js from [Node.js official website](https://nodejs.org/).
   - Verify installation by running `node -v` and `npm -v` in your terminal.

3. **Install Gradle:**
   - Install Gradle by following the instructions on the [Gradle website](https://gradle.org/install/).
   - Verify installation by running `gradle -v` in your terminal.

4. **Install Git:**
   - Download and install Git from [Git official website](https://git-scm.com/).
   - Verify installation by running `git --version` in your terminal.

## Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd 02_Source/01_Source\ Code/backend
   ```

2. Run the following command to build the project:
   ```bash
   ./gradlew build
   ```

3. Start the backend server:
   ```bash
   ./gradlew bootRun
   ```

## Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd 02_Source/01_Source\ Code/frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

## Database Setup

1. Ensure you have a database server (e.g., MySQL) installed and running.
2. Use the provided `dbscript.sql` and `test_data.sql` files in the `02_Source/01_Source Code/` directory to set up the database schema and seed data.
3. Update the database connection details in the `application.properties` file located in `backend/src/main/resources/`.

## Additional Notes

- Refer to the `04_Compilation Guide.md` in the `02_Source/` directory for more detailed build instructions.
- For deployment instructions, see the `02_Deployment Guide.md` in the `03_Deployment/` directory.