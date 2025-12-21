package mat.unical.it.embasp.samegame;

import java.util.List;

public class DivideAndConquerStrategy implements AIStrategy {

    @Override
    public int[] getMove(char[][] board) {
        // Dynamic midpoint
        int mid = board[0].length / 2;
        int[] bestLeft = getBestLocalMove(board, 0, mid);
        int[] bestRight = getBestLocalMove(board, mid, board[0].length);
        
        if (bestLeft == null) return bestRight;
        if (bestRight == null) return bestLeft;
        
        if (bestLeft[2] >= bestRight[2]) return bestLeft;
        else return bestRight;
    }
    
    private int[] getBestLocalMove(char[][] board, int starCol, int endCol) {
        int maxScore = -1;
        int[] best = null;
        boolean[][] visited = new boolean[board.length][board[0].length];
        
        for(int i=0; i<board.length; ++i) {
            for(int j=starCol; j<endCol; ++j) {
                if(board[i][j] != '0' && !visited[i][j]) {
                    List<int[]> block = GameEngine.findBlock(board, i, j, board[i][j]);
                    
                    for(int[] b : block) visited[b[0]][b[1]] = true; 
                    
                    if (block.size() >= 2) {
                        int score = (int)Math.pow(block.size() - 1, 2);
                        if (score > maxScore) {
                            maxScore = score;
                            best = new int[] {i, j, score};
                        }
                    }
                }
            }
        }
        return best;
    }

}
