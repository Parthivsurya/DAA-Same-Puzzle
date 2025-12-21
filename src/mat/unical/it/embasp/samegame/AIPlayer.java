package mat.unical.it.embasp.samegame;

public class AIPlayer {

    public void clearMemo() {
        // No-op here as strategies now handle their own state/memoization if needed.
        // If we want to support clearing memoization for DP globally, we might need a reference or instantiate fresh.
        // With the current design, we instantiate a new strategy each time or the strategy manages its own lifecycle.
    }

    public int[] getMove(int mode, char[][] board) {
        AIStrategy strategy = getStrategy(mode);
        if (strategy != null) {
            return strategy.getMove(board);
        }
        return new RandomStrategy().getMove(board); // Fallback
    }

    private AIStrategy getStrategy(int mode) {
        switch (mode) {
            case 0: return new GreedyStrategy();
            case 1: return new DivideAndConquerStrategy();
            case 2: return new BacktrackingStrategy(2);
            case 3: return new DynamicProgrammingStrategy(2);
            case 4: return new RandomStrategy();
            default: return new RandomStrategy();
        }
    }
}
