package mat.unical.it.embasp.samegame;

import java.util.List;

public class GreedyStrategy implements AIStrategy {

    @Override
    public int[] getMove(char[][] board) {
        List<int[]> moves = GameEngine.getAllMoves(board);
        // Custom Bubble Sort implementation (Descending order by score at index 2)
        for (int i = 0; i < moves.size() - 1; i++) {
            for (int j = 0; j < moves.size() - i - 1; j++) {
                // If current move has less points than the next move, swap them
                if (moves.get(j)[2] < moves.get(j + 1)[2]) {
                    int[] temp = moves.get(j);
                    moves.set(j, moves.get(j + 1));
                    moves.set(j + 1, temp);
                }
            }
        } 
        if (moves.isEmpty()) return null;
        return moves.get(0); 
    }
}
