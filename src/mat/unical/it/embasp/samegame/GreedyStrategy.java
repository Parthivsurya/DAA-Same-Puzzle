package mat.unical.it.embasp.samegame;

import java.util.List;

public class GreedyStrategy implements AIStrategy {

    @Override
    public int[] getMove(char[][] board) {
        List<int[]> moves = GameEngine.getAllMoves(board);
        // Custom Merge Sort implementation (O(N log N))
        mergeSort(moves);
        
        if (moves.isEmpty()) return null;
        return moves.get(0); 
    }

    private void mergeSort(List<int[]> list) {
        if (list.size() <= 1) return;

        int mid = list.size() / 2;
        
        // Create Left and Right sublists
        List<int[]> left = new java.util.ArrayList<>();
        List<int[]> right = new java.util.ArrayList<>();
        
        for(int i = 0; i < mid; i++) left.add(list.get(i));
        for(int i = mid; i < list.size(); i++) right.add(list.get(i));

        // Recursively sort them
        mergeSort(left);
        mergeSort(right);

        // Merge them back together
        merge(list, left, right);
    }

    private void merge(List<int[]> result, List<int[]> left, List<int[]> right) {
        int i = 0, j = 0, k = 0;
        
        while (i < left.size() && j < right.size()) {
            // Sort descending by score (index 2)
            if (left.get(i)[2] >= right.get(j)[2]) { 
                result.set(k++, left.get(i++));
            } else {
                result.set(k++, right.get(j++));
            }
        }
        
        while (i < left.size()) {
            result.set(k++, left.get(i++));
        }
        
        while (j < right.size()) {
            result.set(k++, right.get(j++));
        }
    }
}
