package mat.unical.it.embasp.samegame;

import java.util.List;

public class GreedyStrategy implements AIStrategy {

    @Override
    public int[] getMove(char[][] board) {
        List<int[]> moves = GameEngine.getAllMoves(board);
        moves.sort((a, b) -> b[2] - a[2]); 
        if (moves.isEmpty()) return null;
        return moves.get(0); 
    }
}
