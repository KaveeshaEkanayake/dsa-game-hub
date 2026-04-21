# DSA Game Hub 🎮

A collection of five algorithm-based interactive games built as a group coursework project for the **PDSA (Programming and Data Structures & Algorithms)** module at the **National Institute of Business Management (NIBM)** — BSc (Hons) Computing, Batch 25.2.

🌐 **Live Frontend:** [dsa-game-hub.vercel.app](https://dsa-game-hub.vercel.app)  
⚙️ **Live Backend:** [dsa-game-hub-staging.up.railway.app](https://dsa-game-hub-staging.up.railway.app)

---

## 🎯 Games

### 1. Minimum Cost Problem
Assigns N tasks (50–100) to N employees to minimise total cost using two algorithms:
- Hungarian Algorithm
- Greedy Assignment

Costs randomise between $20–$200 each round. Algorithm execution times are recorded in the database.

### 2. Snake and Ladder Game
A classic Snake and Ladder game on an N×N board (6–12) with N-2 snakes and N-2 ladders placed randomly each round. Uses two algorithms to find the minimum dice throws:
- Breadth-First Search (BFS)
- Dynamic Programming (DP)

Players are given 3 MCQ choices and must identify the minimum number of dice throws to win.

### 3. Traffic Simulation Problem
Models a traffic network with nodes A (source) → B, C, D, E, F, G, H → T (sink). Road capacities are randomised between 5–15 vehicles/min each round. Players must identify the maximum vehicle flow from A to T using:
- Ford-Fulkerson Algorithm
- Edmonds-Karp Algorithm

**Scoring:** WIN (exact), DRAW (within ±1), LOSE (more than ±1 away)

### 4. Knight's Tour Problem
Places a knight on an 8×8 or 16×16 chessboard at a random starting position. Players must identify the correct sequence of moves that visits every square exactly once using:
- Warnsdorff's Heuristic
- Backtracking

### 5. Sixteen Queens Puzzle
Places 16 queens on a 16×16 chessboard such that no two queens threaten each other. Solutions are found using:
- Sequential Backtracking
- Threaded Backtracking

Players submit solutions and the system tracks which solutions have already been found. When all solutions are identified the system resets.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React + Vite |
| Backend | Java Spring Boot |
| Database | MySQL |
| ORM | Hibernate (JPA) |
| Deployment (Frontend) | Vercel |
| Deployment (Backend) | Railway |
| CI/CD | GitHub Actions |
| Testing | JUnit 5 + Mockito |

---

## 🏗 Project Structure

```
dsa-game-hub/
├── backend/
│   └── dsagamehub/
│       ├── src/main/java/com/dsagamehub/
│       │   ├── controller/       # REST API controllers
│       │   ├── service/          # Algorithm + game logic
│       │   ├── model/            # JPA entity classes
│       │   ├── repository/       # Spring Data JPA repositories
│       │   ├── dto/              # Data transfer objects
│       │   └── exception/        # Custom exception classes
│       └── src/test/java/com/dsagamehub/
│           └── service/          # JUnit unit tests
├── frontend/
│   └── src/
│       ├── components/           # Shared UI components
│       ├── pages/                # Game pages
│       │   ├── MinimumCost.jsx
│       │   ├── SnakeLadder.jsx
│       │   ├── TrafficSimulation.jsx
│       │   ├── KnightsTour.jsx
│       │   └── SixteenQueens.jsx
│       └── App.jsx
└── .github/
    └── workflows/                # CI/CD pipeline configs
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8+
- Maven

### Backend Setup

```bash
# Clone the repo
git clone https://github.com/KaveeshaEkanayake/dsa-game-hub.git
cd dsa-game-hub/backend/dsagamehub

# Create application.properties from the example
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` with your local MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dsa_game_hub
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

Create the database:

```sql
CREATE DATABASE dsa_game_hub;
```

Run the backend:

```bash
./mvnw spring-boot:run
```

### Frontend Setup

```bash
cd dsa-game-hub/frontend

# Install dependencies
npm install

# Create environment file
echo "VITE_API_URL=http://localhost:8080" > .env.local

# Start dev server
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 🧪 Running Tests

```bash
cd backend/dsagamehub

# Run all tests
./mvnw test

# Run specific game tests
./mvnw test -Dtest=TrafficFordFulkersonServiceTest
./mvnw test -Dtest=TrafficEdmondsKarpServiceTest
./mvnw test -Dtest=TrafficGameServiceTest
./mvnw test -Dtest=KnightTourServiceTest
```

---

## 🌿 Branch Strategy

```
main                          # stable production code
development                   # staging — auto deploys to Railway + Vercel
feature/game-mincost          # Minimum Cost game
feature/snake-ladder          # Snake and Ladder game
feature/traffic-simulation-final  # Traffic Simulation game
feature/knights-tour          # Knight's Tour game
feature/sixteen-queens        # Sixteen Queens game
```

All feature branches merge into `development` via Pull Requests. CI must pass before merging.

---

## ⚙️ CI/CD Pipeline

Two GitHub Actions pipelines run on every push to `development`:

**Backend CI** — compiles the Spring Boot project and runs all JUnit tests.  
**Frontend CI** — builds the React/Vite project and checks for errors.

On success, Railway auto-deploys the backend and Vercel auto-deploys the frontend.

---

## 🗄 Database

The application uses MySQL with Hibernate auto-creating tables on startup (`ddl-auto=update`). Key tables:

| Table | Description |
|---|---|
| `game_rounds` | Knight's Tour game rounds |
| `knight_tour_results` | Knight's Tour player results |
| `minimum_cost_rounds` | Minimum Cost game rounds |
| `player_answers` | Player answers across games |
| `snake_ladder_game_results` | Snake and Ladder results |
| `traffic_game_rounds` | Traffic Simulation rounds |
| `traffic_game_results` | Traffic Simulation player results |
| `traffic_edges` | Traffic network edge data |
| `players` | Player records |

---

## 📡 API Endpoints

### Traffic Simulation
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/traffic/new-round` | Generate a new game round with random capacities |
| POST | `/api/traffic/submit-answer` | Submit player answer and get WIN/DRAW/LOSE result |
| GET | `/api/traffic/results` | Get all correct answers |
| GET | `/api/traffic/rounds` | Get all game rounds |
| GET | `/api/traffic/results/{roundId}` | Get results for a specific round |

### Snake and Ladder
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/snake-ladder/start` | Start a new game round |
| POST | `/api/snake-ladder/submit` | Submit player answer |

### Knight's Tour
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/knights-tour/new-game` | Start a new Knight's Tour game |
| POST | `/api/knights-tour/submit` | Submit player's move sequence |

---

## 👥 Team

Built by a team of **10 students** from NIBM BSc (Hons) Computing, Batch 25.2 as part of the PDSA module coursework.

---

## 📄 License

This project is for academic purposes only.
