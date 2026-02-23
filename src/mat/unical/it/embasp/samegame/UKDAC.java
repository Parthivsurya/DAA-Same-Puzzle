package mat.unical.it.embasp.samegame;
import java.util.List;

public class UKDAC implements AIStrategy {

    @Override
    public int[] getMove(char[][] board) {

        List<int[]> moves = GameEngine.getAllMoves(board);

        if (moves == null || moves.isEmpty())
            return null;

        int[][] moveArray = new int[moves.size()][3];
        for (int i = 0; i < moves.size(); i++) {
            moveArray[i] = moves.get(i);
        }

        return findBestMove(moveArray, 0, moveArray.length - 1);
    }

    private int[] findBestMove(int[][] moves, int left, int right) {

        if (left == right) {
            return moves[left];
        }

        int mid = (left + right) / 2;

        int[] bestLeft = findBestMove(moves, left, mid);
        int[] bestRight = findBestMove(moves, mid + 1, right);

        if (bestLeft[2] >= bestRight[2]) {
            return bestLeft;
        } else {
            return bestRight;
        }
    }
}