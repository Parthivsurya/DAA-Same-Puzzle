package mat.unical.it.embasp.samegame;

import java.util.List;

public class DivideAndConquerStrategy implements AIStrategy {
    @Override
    public int[] getMove(char[][] board) {
        if (board == null || board.length == 0) return null;
        int cols = board[0].length;
        MoveResult best = solve(board, 0, cols);
        
        if (best != null) {
            return new int[] { best.r, best.c };
        }
        // Fallback: If D&C finds nothing (e.g. empty board), try global search
        List<int[]> allMoves = GameEngine.getAllMoves(board);
        if (!allMoves.isEmpty()) {
             // Return move with highest score
             int max = -1;
             int[] bestFallback = null;
             for(int[] m : allMoves) {
                 if(m[2] > max) {
                     max = m[2];
                     bestFallback = m;
                 }
             }
             return bestFallback;
        }
        
        return null;
    }
    // Helper class to store move info and its score
    private static class MoveResult {
        int r, c;
        int score;

        public MoveResult(int r, int c, int score) {
            this.r = r;
            this.c = c;
            this.score = score;
        }
    }
    // Recursive function to find the best move in column range [colStart, colEnd)
    private MoveResult solve(char[][] board, int colStart, int colEnd) {
        int width = colEnd - colStart;

        // Base Case: If the region is small (1 or 2 columns), solve directly by scanning.
        if (width <= 2) {
            return scanRegion(board, colStart, colEnd);
        }
        // Divide
        int mid = colStart + (width / 2);
        // Conquer (Recursive Steps)
        MoveResult leftBest = solve(board, colStart, mid);
        MoveResult rightBest = solve(board, mid, colEnd);
        // Combine
        // Calculate scores. If null, score is -1.
        int leftScore = (leftBest != null) ? leftBest.score : -1;
        int rightScore = (rightBest != null) ? rightBest.score : -1;
        
        // Find best "Bridge" move that crosses the cut (mid-1 to mid)
        MoveResult bridgeBest = findBestBridge(board, mid);
        int bridgeScore = (bridgeBest != null) ? bridgeBest.score : -1;

        // Pick the Winner
        if (bridgeScore >= leftScore && bridgeScore >= rightScore) return bridgeBest;
        if (leftScore >= rightScore) return leftBest;
        return rightBest;
    }

    // Scans a specific region for the best move (Base Case)
    private MoveResult scanRegion(char[][] board, int colStart, int colEnd) {
        int rows = board.length;
        MoveResult bestMove = null;
        int maxScore = -1;
        // Use a visited array just for this scan to avoid re-counting the same block
        boolean[][] visited = new boolean[rows][board[0].length];

        for (int i = 0; i < rows; i++) {
            for (int j = colStart; j < colEnd; j++) {
                if (board[i][j] != '0' && !visited[i][j]) {
                    
                    // Use GameEngine to find the block size
                    List<int[]> block = GameEngine.findBlock(board, i, j, board[i][j]);
                    
                    // Mark visited
                    for(int[] cell : block) {
                        // Only mark if it's within our region (optimization), 
                        // but marking globally is safer to prevent double processing 
                        // if the block meanders in/out.
                        if (cell[0] < rows && cell[1] < board[0].length) {
                             visited[cell[0]][cell[1]] = true;
                        }
                    }

                    // Check if block size is valid
                    if (block.size() >= 2) {
                        // Calculate Score
                        int score = (int) Math.pow(block.size() - 1, 2);
                        
                        // Use only if it originates in our region? 
                        // Yes, (i, j) is the seed, which IS in [colStart, colEnd).
                        
                        if (score > maxScore) {
                            maxScore = score;
                            bestMove = new MoveResult(i, j, score);
                        }
                    }
                }
            }
        }
        return bestMove;
    }
    // Finds the best move that spans across the cut (mid-1 and mid) for ANY row
    private MoveResult findBestBridge(char[][] board, int mid) {
        int rows = board.length;
        MoveResult bestBridge = null;
        int maxScore = -1;
        
        // Visited array to prevent checking the SAME bridge block multiple times 
        // (since we scan every row of the cut).
        boolean[] processedRow = new boolean[rows];

        for (int i = 0; i < rows; i++) {
            // Check if there is a piece at the cut boundary
            if (board[i][mid-1] != '0' && !processedRow[i]) {
                
                // Get the block
                List<int[]> block = GameEngine.findBlock(board, i, mid-1, board[i][mid-1]);
                
                // Mark rows at the cut for this block so we don't re-process for i+1, i+2...
                for(int[] cell : block) {
                    if (cell[1] == mid-1) {
                        processedRow[cell[0]] = true;
                    }
                }
                // BRIDGE CONDITION: Does this block exist on BOTH sides?
                // It must have at least one cell with col < mid AND one cell with col >= mid.
                // Since we started seed at mid-1, we know it has col < mid.
                // So we just need to check if ANY cell has col >= mid.
                boolean isBridge = false;
                for(int[] cell : block) {
                    if (cell[1] >= mid) {
                        isBridge = true;
                        break;
                    }
                }
                if (isBridge && block.size() >= 2) {
                    int score = (int) Math.pow(block.size() - 1, 2);
                    if (score > maxScore) {
                        maxScore = score;
                        // Return the seed we found (i, mid-1)
                        bestBridge = new MoveResult(i, mid-1, score);
                    }
                }
            }
        }
        return bestBridge;
    }
}
