package mat.unical.it.embasp.samegame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicProgrammingStrategy implements AIStrategy {
    
    private Map<String, Integer> memoMap = new HashMap<>();
    private int depth;

    public DynamicProgrammingStrategy(int depth) {
        this.depth = depth;
    }
    
    public void clearMemo() {
        memoMap.clear();
    }

    @Override
    public int[] getMove(char[][] board) {
        clearMemo(); // Start fresh for each move decision to avoid stale state? Or keep?
                     // The original code cleared it before calling getDPMove via case 3.
        return getDPMove(board, depth);
    }
    
    public int[] getDPMove(char[][] board, int depth) {
        List<int[]> moves = GameEngine.getAllMoves(board);
        moves.sort((a, b) -> b[2] - a[2]);
        
        int branchLimit = Math.min(moves.size(), 5);
        int maxVal = -1;
        int[] bestMove = null;
        
        for(int i=0; i<branchLimit; ++i) {
            int[] move = moves.get(i);
            char[][] nextBoard = GameEngine.copyBoard(board);
            GameEngine.simulateMove(nextBoard, move[0], move[1]);
            int val = move[2] + getDPValue(nextBoard, depth - 1);
            if (val > maxVal) {
                maxVal = val;
                bestMove = move;
            }
        }
        return bestMove;
    }
    
    private int getDPValue(char[][] board, int depth) {
        if (depth == 0) return 0;
        
        String key = GameEngine.boardToString(board) + ":" + depth;
        if (memoMap.containsKey(key)) return memoMap.get(key);
        
        List<int[]> moves = GameEngine.getAllMoves(board);
        if (moves.isEmpty()) return 0;
        moves.sort((a, b) -> b[2] - a[2]);
        
        int best = 0;
        int branchLimit = Math.min(moves.size(), 3); 
        
        for(int i=0; i<branchLimit; ++i) {
            char[][] next = GameEngine.copyBoard(board);
            GameEngine.simulateMove(next, moves.get(i)[0], moves.get(i)[1]);
            int val = moves.get(i)[2] + getDPValue(next, depth - 1);
            if (val > best) best = val;
        }
        
        memoMap.put(key, best);
        return best;
    }
}
