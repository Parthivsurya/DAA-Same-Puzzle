# SameGame AI Project - Team Presentation Script (5 Members)

This document splits the project into 5 clear roles. Assign one role to each member. This ensures everyone speaks, explains code, and demonstrates "Individual Contribution" for full marks.

---

## 👥 Member 1: The Project Lead & UI Specialist
**Role:** Introduction, GUI, and Demo.
**File to Open:** `MainClass.java`

**Script:**
*   **Intro:** "Good morning. We are here to present our project, **SameGame AI**. It is a tile-matching puzzle game with an integrated AI agent."
*   **The UI:** "I handled the User Interface using **Java Swing**. In `MainClass.java`, I set up the `JFrame` and the `GridLayout` to render the board."
*   **Demo:** *Run the game.* "As you can see, the user can click tiles. I implemented the `MouseListener` to detect clicks and update the score dynamically."
*   **Individual Contribution:** "My focus was on the Event-Driven Programming to connect the user actions with the game logic."

**Key Code to Show:** `createAndShowGUI()` and `refreshBoardUI()`.

---

## 👥 Member 2: The Game Physicist
**Role:** The Game Model & Gravity Logic.
**File to Open:** `GameEngine.java` (Scroll to `applyGravity`)

**Script:**
*   **The Model:** "While the UI looks like a grid, the internal data model is a `char[][]` (2D Array) managed in `GameEngine.java`. This separates the View from the Model."
*   **The Physics:** "My major contribution is the **Gravity Logic**. When blocks are removed, we can't just leave holes."
*   **Code explanation:** "In `applyGravity`, I implemented a two-pass algorithm:
    1.  **Vertical:** Compacting columns so blocks fall down.
    2.  **Horizontal:** Shifting entire columns left if a column becomes empty."
*   **Individual Contribution:** "Implementing the physics engine to ensure valid board states after every move."

**Key Code to Show:** `applyGravity()`, `compactColumn()`.

---

## 👥 Member 3: The Graph Theorist
**Role:** Graph Representation & Search Algorithms.
**File to Open:** `GameEngine.java` (Scroll to `findBlock`)

**Script:**
*   **The Graph:** "I focused on the Data Structure algorithm. This grid is actually an **Implicit Graph**."
    *   **Nodes:** Each cell $(r, c)$.
    *   **Edges:** Connection to neighbors of the *same color*.
*   **The Algorithm:** "To identify which blocks to remove, I implemented **Depth First Search (DFS)**."
*   **Code Walkthrough:** "In the `findBlock` function, we recursively visit neighbors (Up, Down, Left, Right). We use a `visited` array to prevent cycles. This is effectively the **Flood Fill Algorithm**."
*   **Individual Contribution:** "Modeling the problem as a Graph and implementing the DFS traversal."

**Key Code to Show:** `findBlock()` and `findBlockRecursive()`.

---

## 👥 Member 4: The Architect (Design Patterns)
**Role:** System Architecture & Strategy Pattern.
**Files to Open:** `AIPlayer.java`, `AIStrategy.java`

**Script:**
*   **The Architecture:** "To support multiple difficulty levels, I architected the solution using the **Strategy Design Pattern**."
*   **The Problem:** "Hardcoding `if-else` logic for every AI mode would make the code messy and hard to maintain."
*   **The Solution:** "I defined an interface `AIStrategy` (Show the file). Now, `AIPlayer` works as a 'Context'—it delegates the decision-making to the strategy object."
*   **Benefits:** "This follows the Open/Closed Principle. We can add new AI brains without touching the player code."
*   **Individual Contribution:** "Software Architecture and refactoring the codebase for modularity."

**Key Code to Show:** `AIPlayer.getMove()` and the `AIStrategy` interface.

---

## 👥 Member 5: The AI Specialist
**Role:** AI Logic & Greedy Implementation.
**Files to Open:** `GreedyStrategy.java`

**Script:**
*   **The Logic:** "I implemented the specific AI logic required for our project Review. I built the `GreedyStrategy`."
*   **How it works:** "It analyzes the current board state and identifies every possible connected component (group of color)."
*   **Optimization:** "It sorts these components by size to always pick the locally optimal move—the one that gives the highest score immediately."
*   **Why Greedy?** "It is extremely fast and efficient, providing instant feedback to the user without valid delays."
*   **Individual Contribution:** "Implementation of the core decision-making algorithm."

**Key Code to Show:** `GreedyStrategy.getMove()` and the sorting logic.

---

## 🎯 Summary for Grades (Individual Contribution)
- **Member 1:** UI/UX & Main Interaction Loop.
- **Member 2:** Data Model & Physics Engine (Gravity).
- **Member 3:** Graph Theory & DFS Algorithm.
- **Member 4:** Software Architecture (Strategy Pattern).
- **Member 5:** AI Logic & Optimization Algorithms.
