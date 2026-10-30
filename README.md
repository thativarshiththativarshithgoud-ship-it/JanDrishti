🇮🇳 JanDrishti – Civic Infrastructure Monitoring System

JanDrishti is a JavaFX-based desktop application prototype designed to monitor and manage civic infrastructure issues across India. It provides government officers with a centralized dashboard to view reported issues, identify hotspots, track issue status, and monitor citizen verification.

The system aims to improve transparency, coordination, and visibility in civic issue management.

---

🌟 Key Features

1. Centralized Monitoring Dashboard
   
   - Provides a single view of civic infrastructure issues.
   - Displays issue volume and overall workflow status.
   - Helps officers monitor reported problems.

2. Geographical Issue Tracking
   
   - Organizes civic issues based on geographical hierarchy.
   - Supports tracking from Area to City, District, State, and India.
   - Helps identify areas with reported civic issues.

3. Issue Hotspot Monitoring
   
   - Provides an overview of locations with civic issue activity.
   - Helps officers identify areas requiring attention.

4. Issue Workflow Management
   
   - Tracks the progress of reported civic issues.
   - Supports the following workflow:
     - Reported
     - Verified
     - Assigned
     - Resolved

5. Citizen Verification
   
   - Includes citizen verification status in the monitoring dashboard.
   - Helps officers review verification information.

6. JavaFX Desktop Interface
   
   - Provides a desktop-based dashboard.
   - Uses CSS styling for the application's visual presentation.

7. Database Integration Foundation
   
   - Includes a MySQL database schema.
   - Defines the persistence boundary through "IssueRepository".
   - Uses in-memory demo data in the initial version.

---

🛠️ Technology Stack

- Programming Language: Java 21+
- User Interface: JavaFX
- Styling: CSS
- Build Tool: Apache Maven 3.9+
- Database: MySQL 8+
- Architecture: Repository-based data access structure

---

📁 Project Structure

JanDrishti/
│
├── database/
│   └── schema.sql
│
├── src/
│   └── main/
│       ├── java/
│       │   └── in/
│       │       └── jandrishti/
│       │           ├── model/
│       │           ├── data/
│       │           └── ui/
│       │
│       └── resources/
│           └── styles.css
│
├── .gitignore
├── pom.xml
└── README.md

---

🗄️ Database Setup

JanDrishti includes a MySQL database schema for future persistence integration.

Database file:

database/schema.sql

The initial prototype uses realistic in-memory data, so the dashboard can be reviewed without database credentials.

The persistence boundary is:

IssueRepository

MySQL integration can be connected in the next development stage.

---

🚀 How to Run the Project

Requirements

Install the following software:

- JDK 21 or later
- Apache Maven 3.9 or later
- MySQL 8+ (optional for the current demo UI)

Step 1: Clone the Repository

git clone https://github.com/thativarshiththativarshithgoud-ship-it/JanDrishti.git

Step 2: Open the Project

Open the downloaded "JanDrishti" folder in VS Code or another Java IDE.

Step 3: Run the Application

Open the terminal inside the project folder and execute:

mvn javafx:run

The JavaFX desktop dashboard will start.

---

🔄 Product Workflow

The geographical issue monitoring flow:

Citizen Report
      ↓
    Area
      ↓
    City
      ↓
   District
      ↓
    State
      ↓
     India

Issue Status Workflow

Reported
   ↓
Verified
   ↓
Assigned
   ↓
Resolved

---

🎯 Project Objectives

- Improve monitoring of civic infrastructure issues.
- Provide a centralized dashboard for government officers.
- Support geographical organization of reported issues.
- Improve visibility of issue workflow status.
- Support citizen verification monitoring.
- Establish a foundation for future database integration.

---

🔮 Future Enhancements

- Complete MySQL persistence integration.
- Citizen-side issue reporting interface.
- Advanced geographical hotspot visualization.
- Real-time issue status updates.
- Detailed analytics and reporting.
- Improved officer and administrator workflows.

---

👨‍💻 Project Information

Project Name: JanDrishti

Project Type: JavaFX Desktop Application

Domain: Smart Governance / Civic Infrastructure Monitoring

Repository: https://github.com/thativarshiththativarshithgoud-ship-it/JanDrishti

---

JanDrishti – Bringing civic issues into focus through technology.
