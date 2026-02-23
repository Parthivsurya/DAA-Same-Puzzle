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
        return null;
    }
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

    private MoveResult scanRegion(char[][] board, int colStart, int colEnd) {
        int rows = board.length;
        MoveResult bestMove = null;
        int maxScore = -1;
        boolean[][] visited = new boolean[rows][board[0].length];

        for (int i = 0; i < rows; i++) {
            for (int j = colStart; j < colEnd; j++) {
                if (board[i][j] != '0' && !visited[i][j]) {
                    List<int[]> block = findBlockBounded(board, i, j, board[i][j], colStart, colEnd);
                    
                    for(int[] cell : block) {
                        visited[cell[0]][cell[1]] = true;
                    }
                    if (block.size() >= 2) {
                        int score = (int) Math.pow(block.size() - 1, 2);
                        if (score > maxScore) {
                            maxScore = score;
                            bestMove = new MoveResult(i, j, score);
                        }
                    }
                }}}
        return bestMove;}
        private MoveResult findBestBridge(char[][] board, int mid) {
        int rows = board.length;
        MoveResult bestBridge = null;
        int maxScore = -1;
        boolean[] processedRow = new boolean[rows];

        for (int i = 0; i < rows; i++) {
            if (board[i][mid-1] != '0' && !processedRow[i]) {
                List<int[]> block = GameEngine.findBlock(board, i, mid-1, board[i][mid-1]);
                    for(int[] cell : block) {
                    if (cell[1] == mid-1) {
                        processedRow[cell[0]] = true;
                    }
                }
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
                        bestBridge = new MoveResult(i, mid-1, score);
                    }
                }
            }
        }
        return bestBridge;
    }

    private List<int[]> findBlockBounded(char[][] board, int r, int c, char color, int colStart, int colEnd) {
        List<int[]> block = new java.util.ArrayList<>();
        if (r < 0 || r >= board.length || c < colStart || c >= colEnd) return block;
        if (board[r][c] != color) return block;
        boolean[][] visited = new boolean[board.length][board[0].length];
        findBlockRecursiveBounded(board, r, c, color, colStart, colEnd, visited, block);
        return block;
    }

    private void findBlockRecursiveBounded(char[][] board, int r, int c, char color, int colStart, int colEnd, boolean[][] visited, List<int[]> block) {
        if (r < 0 || r >= board.length || c < colStart || c >= colEnd) return;
        if (visited[r][c]) return;
        if (board[r][c] != color) return;
        
        visited[r][c] = true;
        block.add(new int[] {r, c});
        
        findBlockRecursiveBounded(board, r + 1, c, color, colStart, colEnd, visited, block);
        findBlockRecursiveBounded(board, r - 1, c, color, colStart, colEnd, visited, block);
        findBlockRecursiveBounded(board, r, c + 1, color, colStart, colEnd, visited, block);
        findBlockRecursiveBounded(board, r, c - 1, color, colStart, colEnd, visited, block);
    }
}
