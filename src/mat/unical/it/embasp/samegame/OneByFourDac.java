package mat.unical.it.embasp.samegame;

import java.util.List;

public class OneByFourDac implements AIStrategy {

    @Override
    public int[] getMove(char[][] board) {
        if (board == null || board.length == 0) return null;
        
        MoveResult best = solve(board,
                        0, board.length,
                        0, board[0].length);
        
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

   private MoveResult solve(char[][] board,
                         int rowStart, int rowEnd,
                         int colStart, int colEnd) {

    int height = rowEnd - rowStart;
    int width = colEnd - colStart;

    if (height <= 2 && width <= 2) {
        return scanRegion(board, rowStart, rowEnd, colStart, colEnd);
    }

    int midRow = rowStart + height / 2;
    int midCol = colStart + width / 2;

    MoveResult topLeft =
        solve(board, rowStart, midRow, colStart, midCol);

    MoveResult topRight =
        solve(board, rowStart, midRow, midCol, colEnd);

    MoveResult bottomLeft =
        solve(board, midRow, rowEnd, colStart, midCol);

    MoveResult bottomRight =
        solve(board, midRow, rowEnd, midCol, colEnd);

    MoveResult best = getBest(topLeft, topRight,
                              bottomLeft, bottomRight);

    MoveResult verticalBridge =
        findVerticalBridge(board, rowStart, rowEnd, midCol);

    MoveResult horizontalBridge =
        findHorizontalBridge(board, colStart, colEnd, midRow);

    best = getBest(best, verticalBridge, horizontalBridge);

    return best;
}


   private MoveResult scanRegion(char[][] board,
                              int rowStart, int rowEnd,
                              int colStart, int colEnd) {

    MoveResult bestMove = null;
    int maxScore = -1;
    
    for (int i = rowStart; i < rowEnd; i++) {
        for (int j = colStart; j < colEnd; j++) {

            if (board[i][j] != '0') {

                List<int[]> block =
                    GameEngine.findBlock(board, i, j, board[i][j]);

                if (block.size() >= 2) {

                    int score =
                        (int) Math.pow(block.size() - 1, 2);

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


    private MoveResult findVerticalBridge(char[][] board,
                                      int rowStart,
                                      int rowEnd,
                                      int midCol) {

    MoveResult best = null;
    int maxScore = -1;

    for (int i = rowStart; i < rowEnd; i++) {

        if (midCol - 1 < 0 || midCol >= board[0].length)
            continue;

        if (board[i][midCol - 1] != '0') {

            List<int[]> block =
                GameEngine.findBlock(board, i, midCol - 1,
                                     board[i][midCol - 1]);

            boolean isBridge = false;

            for (int[] cell : block) {
                if (cell[1] >= midCol) {
                    isBridge = true;
                    break;
                }
            }

            if (isBridge && block.size() >= 2) {
                int score =
                    (int) Math.pow(block.size() - 1, 2);

                if (score > maxScore) {
                    maxScore = score;
                    best = new MoveResult(i,
                                          midCol - 1,
                                          score);
                }
            }
        }
    }

    return best;
}


private MoveResult findHorizontalBridge(char[][] board,
                                        int colStart,
                                        int colEnd,
                                        int midRow) {

    MoveResult best = null;
    int maxScore = -1;

    for (int j = colStart; j < colEnd; j++) {

        if (midRow - 1 < 0 || midRow >= board.length)
            continue;

        if (board[midRow - 1][j] != '0') {

            List<int[]> block =
                GameEngine.findBlock(board,
                                     midRow - 1,
                                     j,
                                     board[midRow - 1][j]);

            boolean isBridge = false;

            for (int[] cell : block) {
                if (cell[0] >= midRow) {
                    isBridge = true;
                    break;
                }
            }

            if (isBridge && block.size() >= 2) {

                int score =
                    (int) Math.pow(block.size() - 1, 2);

                if (score > maxScore) {
                    maxScore = score;
                    best = new MoveResult(midRow - 1,
                                          j,
                                          score);
                }
            }
        }
    }
    return best;
}

private MoveResult getBest(MoveResult... results) {
    MoveResult best = null;
    int max = -1;

    for (MoveResult r : results) {
        if (r != null && r.score > max) {
            max = r.score;
            best = r;
        }
    }

    return best;
}
}