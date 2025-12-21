package mat.unical.it.embasp.samegame;

import java.util.List;
import java.util.Random;

public class RandomStrategy implements AIStrategy {

    @Override
    public int[] getMove(char[][] board) {
        List<int[]> moves = GameEngine.getAllMoves(board);
        if (moves.isEmpty()) return null;
        Random r = new Random();
        return moves.get(r.nextInt(moves.size()));
    }

}
