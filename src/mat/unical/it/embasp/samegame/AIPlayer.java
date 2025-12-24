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
        // For Review 1, we only need Greedy Logic.
        // We map other modes to Greedy as well to simplify the presentation
        // or effectively "remove" the complex logic while keeping the UI.
        switch (mode) {
            case 0: return new GreedyStrategy();
            case 4: return new RandomStrategy(); // Keeping  Random as 'Beginner'
            default: return new RandomStrategy();
        }
    }
}
