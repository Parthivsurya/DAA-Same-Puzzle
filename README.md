# SameGame AI - Comprehensive Code Explanation

This document is designed to help you explain every single part of the code to your teachers. It breaks down the critical functions line-by-line using the "What & Why" approach.

---

## 1. The Core Logic: `GameEngine.java`

This file contains the rules of the game. It is **static** because these rules don't change regardless of who is playing (Human or AI).

### **A. Finding Connected Blocks (`findBlock`)**
**Graph Theory Algorithm Used:** **Depth First Search (DFS)** (Flood Fill).
- **The Graph:** Think of the game board as a "Graph".
    - **Nodes:** Each cell (tile) is a "Node".
    - **Edges:** Two nodes are connected if they are next to each other (up, down, left, right) AND have the same color.
- **The Algorithm:** computing the "Connected Components" of this graph.
    - We start at a block, and recursively visit all its neighbors that share the same color.

```java
// We use a helper function to start the recursion
public static List<int[]> findBlock(char[][] board, int r, int c, char color) {
    List<int[]> block = new ArrayList<>(); // 1. Create a list to store the matching cells
    
    // 2. Safety Check: If coordinates are off-board, or color doesn't match, stop immediately.
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return block;
    if (board[r][c] != color) return block;
    
    // 3. 'visited' array keeps track of where we have been so we don't go in circles.
    boolean[][] visited = new boolean[board.length][board[0].length];
    
    // 4. Start the Recursive Magic
    findBlockRecursive(board, r, c, color, visited, block);
    return block;
}

private static void findBlockRecursive(...) {
    // BASE CASES: Stop the recursion if...
    if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return; // ...off board
    if (visited[r][c]) return; // ...already visited this cell
    if (board[r][c] != color) return; // ...color is different
    
    // DO WORK:
    visited[r][c] = true; // Mark as visited
    block.add(new int[] {r, c}); // Add to our list of blocks
    
    // RECURSE: Look in all 4 directions (Down, Up, Right, Left)
    findBlockRecursive(board, r + 1, c, color, visited, block);
    findBlockRecursive(board, r - 1, c, color, visited, block);
    findBlockRecursive(board, r, c + 1, color, visited, block);
    findBlockRecursive(board, r, c - 1, color, visited, block);
}
```

### **B. Gravity & Physics (`applyGravity`)**
This makes the game "feel" real. When blocks allow, everything falls down and shifts left.

```java
public static void applyGravity(char[][] board) {
    // PART 1: Vertical Gravity (Falling Down)
    for (int j = 0; j < C; ++j) { // Loop through every column
       compactColumn(board, j); // "Squash" the column so non-zero blocks go to the bottom
    }
    
    // PART 2: Horizontal Shift (Moving Left)
    for(int j=0; j<C-1; ++j) {
       if(isColumnEmpty(board, j)) { // If we find an empty column...
           // ...find the next NON-empty column...
           int next = j+1;
           while(next < C && isColumnEmpty(board, next)) next++;
           
           if(next < C) {
               copyColumn(board, j, next); // ...and move that entire column here.
           }
       }
    }
}
```

---

## 2. Strategies (The AI Brains)

We used the **Strategy Pattern**. This means we have one interface (`AIStrategy`) and many classes that implement it.

### **A. Greedy Algorithm (`GreedyStrategy.java`)**
**Logic:** Short-sighted. Picks the move offering the most points *right now*.

```java
public int[] getMove(char[][] board) {
    // 1. Get ALL possible legal moves
    List<int[]> moves = GameEngine.getAllMoves(board);
    
    // 2. Sort them by Score (biggest first)
    // 'b[2] - a[2]' means "Descening Order" (High to Low)
    moves.sort((a, b) -> b[2] - a[2]); 
    
    // 3. Pick the first one (the biggest one)
    if (moves.isEmpty()) return null;
    return moves.get(0); 
}
```

### **B. Random Strategy (`RandomStrategy.java`)**
**Logic:** Baseline for comparison. Simply picks a valid legal move at random. Used for "Beginner" difficulty.

---

## 3. Explaining to Teachers (Common Questions)

**Q: Why use an Interface (`AIStrategy`)?**
> **A:** "It follows the **Open/Closed Principle** (SOLID). We can add a new AI algorithm (like 'Genetic Algorithm') by creating a new class without changing any existing code in `AIPlayer`."

**Q: What is the purpose of `visited` array in `findBlock`?**
> **A:** "To prevent infinite loops. Without it, the algorithm would check cell A, go to B, then see A is connected to B and go loops back to A forever causing a StackOverflowError."

**Q: Explain the `GameEngine.copyBoard` method.**
> **A:** "Java passes objects by reference. If we didn't copy the board before simulating a move, the AI would accidentally destroy the real game board while thinking!"

