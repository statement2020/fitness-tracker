# 🏋️‍♂️ Fitness Tracker Application

A Spring Boot + Thymeleaf web application for tracking and comparing calories burnt, calories consumed, and weight across multiple users.  
It includes insightful dashboards (powered by Plotly) to visualize absolute calories and %BMR metrics.

---

## 🚀 Features

- Create users with a name and **Basal Metabolic Rate (BMR)**
- Record daily entries for:
    - Calories consumed
    - Calories burnt
    - Weight
- Combined dashboard with:
    - **Bar charts** for calories burnt/consumed
    - **Line charts** for %BMR burnt/consumed
    - Dual Y-axis comparison
- Data persisted in **PostgreSQL**

---

## 🛠️ Requirements

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)

---

## 📦 Build & Run

Clone the repository and navigate into the project directory:

```bash
git clone https://github.com/yourusername/fitness-tracker.git
cd fitness-tracker
```


### 1. Build and start the application using Docker Compose:

```bash
mvn clean install
docker compose up
```
This will:
* Build the Fitness Tracker jar
* Start a PostgreSQL container (fitness_postgres)
* Start the Fitness Tracker app container (fitness_app)

### 2. Access the application
Once the container are running, open your browser and navigate to:
http://localhost:8080

### 3. Database Configuration
The application is preconfigured with these basic defaults:
- **Database**: `fitness_db`
- **Username**: `fitness_user`
- **Password**: `fitness_password`
- **Host**: `postgres`

### 4. Using the application
**1. Add a User** 

Navigate to the "Users" section and click "Add User". Provide a name and BMR value.

**2. Add Measurements**

Navigate to the "Measurements" section and click "Add Measurement". Select a user, date, and enter calories consumed, calories burnt, and weight.

**3. View Dashboard**

Go to the "Dashboard" section to visualize the data with interactive charts.

## 🧹 Stopping the Application

To stop the application and remove the containers, run:

```bash
docker compose down
```

To stop the application and remove everything including the database volume, run:

```bash
docker compose down -v
```