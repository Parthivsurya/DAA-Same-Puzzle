# Final Presentation Script & Code Walkthrough

Use this document as your step-by-step guide during the review. It tells you exactly **What to Open**, **What to Say**, and **How to Answer** the invigilator's questions to score full marks.

---

## Phase 1: The Demo (Proof of Concept)
**Action:** Run the game (`java -cp bin mat.unical.it.embasp.samegame.MainClass`).

**Say:**
"This is SameGame, a puzzle game built using Java Swing. The objective is to clear the board by selecting connected groups of same-colored tiles."
1.  **Click a block:** "As you can see, when I click this group, it disappears."
2.  **Watch them fall:** "The remaining blocks fall down, and empty columns shift left. This is the gravity physics I implemented."
3.  **Switch to Greedy:** "I can switch the AI mode to 'Greedy'. It creates an automated agent that plays the game for me."

---

## Phase 2: The Code Walkthrough

### Step 1: The Model & Graph Representation
**Action:** Open `GameEngine.java`.
**Scroll to:** `findBlock` method (Line ~11).

**Say:**
"To solving this puzzle, I modeled the grid as an **Implicit Graph**."
"Here in the `findBlock` function, I use **Depth First Search (DFS)**."
"Every cell is a 'node'. If a neighbor has the same color, it's connected by an 'edge'. This recursive function traverses those edges to find the entire 'Connected Component' (the block)."

**Invigilator Question:** "Why DFS?"
**Answer:** "It's efficient for finding connected components in a grid (Flood Fill algorithm). BFS would work too, but recursive DFS is cleaner to implement here."

### Step 2: The Grading Requirement (Greedy Logic)
**Action:** Open `GreedyStrategy.java`.

**Say:**
"For the grading requirement of 'Greedy Logic', I created this class."
"The Greedy algorithm implies making the **locally optimal choice** at each stage."
"Here, `getAllMoves` gets every possible move. Then, I sort them by score (`b[2] - a[2]`) and simply pick the top one (`moves.get(0)`). It doesn't look ahead, it just grabs the biggest points *now*."

### Step 3: Your Architecture (The "Wow" Factor)
**Action:** Open `AIPlayer.java` and `AIStrategy.java`.

**Say:**
"To ensure the code is modular and professional, I used the **Strategy Design Pattern**."
"Instead of writing one huge `if-else` statement for every difficulty level, I created common interface `AIStrategy`."
"The `AIPlayer` class doesn't know *how* the move is calculated; it just asks the current strategy optimization to 'get the move'. This allows me to easily plug in new AI algorithms like Backtracking or Dynamic Programming without breaking existing code."

---

## Phase 3: Detailed Code Explanation (For Q&A)

### "Explain `GameEngine.applyGravity`"
**Open:** `GameEngine.java` (Line ~35).
**Answer:**
"It works in two passes:
1.  **Vertical Compact:** Iterates through every column. Copies non-zero blocks into a temporary array and puts them back at the bottom. This makes blocks 'fall'.
2.  **Horizontal Shift:** Checks for completely empty columns. If found, it shifts all subsequent columns to the left to close the gap."

### "Explain the Recursion in `findBlockRecursive`"
**Answer:**
"It has base cases to stop: if we go off-board, if we visited the cell already, or if the color doesn't match.
If those pass, it marks the cell as visited and recursively calls itself for `r+1` (Down), `r-1` (Up), `c+1` (Right), and `c-1` (Left)."



---

## Checklist for Full Marks
- [ ] **Game Model:** Mention "2D Array" grid.
- [ ] **Graph Theory:** Mention "Implicit Graph" and "DFS/Flood Fill".
- [ ] **Greedy:** Show the sorting logic.
- [ ] **Modularity:** Show the `AIStrategy` interface.
- [ ] **Contribution:** Emphasize that *you* designed the architecture to be extensible.
