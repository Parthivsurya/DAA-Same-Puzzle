# Review 1: Design & Implementation - Defense Document

This document maps your project directly to the requirements of **Review 1**. Use this as your script or cheat sheet during the presentation.

---

## 1. Choice of Game & Model (2 Marks)
**What to say:**
"I chose **SameGame**, a tile-matching puzzle game. The model is a **2D Grid** where each cell represents a node in a graph. The objective is to clear the board by selecting connected components of the same color."

**Where is it in the code?**
- `MainClass.java`: Sets up the visual grid (`ROWS` x `COLS`).
- `GameEngine.java`: Manages the 2D array model `char[][] board`.

---

## 2. POC (Proof of Concept) & Working Game (2 Marks)
**What to say:**
"I have a fully functional Proof of Concept. The game has a GUI built with Java Swing, supports mouse interaction, tracks scores, and implements physics (gravity)."

**Key Code to Show:**
- **Game Loop:** `MainClass.handleMouseClick()` - Detects clicks, updates score, applies gravity, re-renders.
- **Physics:** `GameEngine.applyGravity()` - The logic that makes blocks fall down and columns shift left.

---

## 3. Graph Representation (2 Marks)
**What to say:**
"Although it looks like a grid, I treat the board as an **Implicit Graph**."
- **Nodes:** Every cell in the grid `(row, col)`.
- **Edges:** Exist between adjacent cells (Up, Down, Left, Right) **IF AND ONLY IF** they share the same color.
- **Algorithm:** I use **Depth First Search (DFS)** to traverse this graph and find "Connected Components" (the blocks to remove).

**Key Code to Show:**
*File:* `GameEngine.java`
```java
// DFS Algorithm to find connected nodes
public static List<int[]> findBlock(char[][] board, int r, int c, char color) {
    // ... setup ...
    findBlockRecursive(board, r, c, color, visited, block); 
    return block;
}

// Recursive step (Traversing the edges)
private static void findBlockRecursive(...) {
    // ... base cases ...
    // Visit neighbors: Down, Up, Right, Left
    findBlockRecursive(board, r + 1, c, color, visited, block);
    findBlockRecursive(board, r - 1, c, color, visited, block);
    // ...
}
```

---

## 4. Greedy Logic Implementation (2 Marks)
**What to say:**
"I implemented a **Greedy Strategy** AI. It analyzes the current state of the graph and makes the locally optimal choice — picking the move that gives the highest immediate score."

**Key Code to Show:**
*File:* `GreedyStrategy.java`
```java
public int[] getMove(char[][] board) {
    // 1. Get ALL Connected Components (Graph Nodes)
    List<int[]> moves = GameEngine.getAllMoves(board);
    
    // 2. Sort by Score (Descending)
    moves.sort((a, b) -> b[2] - a[2]); 
    
    // 3. Pick the Top 1
    return moves.get(0); 
}
```

---

## 5. Individual Contribution (2 Marks)
**What to say:**
"My major contribution is the **Architectural Design**. I refactored the project to use the **Strategy Design Pattern**. Instead of hardcoding logic, I created a modular system where different AI algorithms (Greedy, Random, etc.) can be plugged in dynamically."

**Key Code to Show:**
*File:* `AIStrategy.java` (The Interface) & `AIPlayer.java` (The Context)
```java
// The Interface I designed
public interface AIStrategy {
    int[] getMove(char[][] board);
}

// How AIPlayer uses my design to switch brains dynamically
public int[] getMove(int mode, char[][] board) {
    AIStrategy strategy = getStrategy(mode);
    return strategy.getMove(board);
}
```




Yes, absolutely! The Graph Implementation is the core of the GameEngine.java file and it is fully intact.

Where is it? Open GameEngine.java and look at the findBlock function (lines 11-32).

How to explain it:

The Grid is the Graph: "Even though it looks like a 2D array, we treat each cell as a 'Node' in a graph."
Connections are Edges: "If two neighbors have the same color, they have an 'Edge' connecting them."
The Algorithm: "The findBlock function uses DFS (Depth First Search) to traverse these edges and find the 'Connected Components'. That is exactly what a Graph Traversal is."
So you definitely have the "Graph Representation" part covered for your review!


Q: What data structure are you using?

A: "I am using a 2D array (char[][]) to represent the grid because it provides O(1) constant time access to any cell using its coordinates."

Q: How do you find the matching blocks?

A: "I use a recursive Depth First Search (DFS) algorithm. I maintain a visited boolean matrix to prevent infinite loops."

Q: How does gravity work?

A: "It is a two-step process. First, compactColumn shifts non-zero elements to the start of the array. Second, copyColumn shifts entire columns left if an empty column is found."

Q: What is the complexity?

A: "The findBlock function visits each node once, so it is linear time relative to the board size, O(Rows * Cols)."