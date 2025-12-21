package mat.unical.it.embasp.samegame;

import java.util.List;

public class BacktrackingStrategy implements AIStrategy {
    
    private int depth;

    public BacktrackingStrategy(int depth) {
        this.depth = depth;
    }

    @Override
    public int[] getMove(char[][] board) {
        return getBacktrackingMove(board, depth);
    }

    private int[] getBacktrackingMove(char[][] board, int depth) {
        if (depth == 0) return new GreedyStrategy().getMove(board); // Fallback to Greedy
        
        List<int[]> moves = GameEngine.getAllMoves(board);
        moves.sort((a, b) -> b[2] - a[2]);
        
        int branchLimit = Math.min(moves.size(), 5);
        int maxVal = -1;
        int[] bestMove = null;
        
        for(int i=0; i<branchLimit; ++i) {
            int[] move = moves.get(i);
            int currentScore = move[2];
            char[][] nextBoard = GameEngine.copyBoard(board);
            GameEngine.simulateMove(nextBoard, move[0], move[1]);
            int futureVal = backtrackingVal(nextBoard, depth - 1);
            int totalVal = currentScore + futureVal;
            if (totalVal > maxVal) {
                maxVal = totalVal;
                bestMove = move;
            }
        }
        if (bestMove == null && !moves.isEmpty()) return moves.get(0); 
        return bestMove;
    }
    
    private int backtrackingVal(char[][] board, int depth) {
        if (depth == 0) return 0;
        List<int[]> moves = GameEngine.getAllMoves(board);
        if (moves.isEmpty()) return 0;
        moves.sort((a, b) -> b[2] - a[2]);
        return moves.get(0)[2];
    }
}
