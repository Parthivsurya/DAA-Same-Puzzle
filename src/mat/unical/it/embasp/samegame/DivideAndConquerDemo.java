package mat.unical.it.embasp.samegame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DivideAndConquerDemo extends JFrame {

    private DemoPanel boardPanel;
    private JTextArea logArea;
    private char[][] board;
    
    // Controls
    private JButton startBtn, pauseBtn, stepBtn, resetBtn;
    private JSlider speedSlider;
    
    // Visualization State
    private int vColStart = -1, vColEnd = -1; // Current Range
    private int vMid = -1;                    // Current Split Line
    private List<int[]> vHighlightBlock = null; // Block being inspected
    private String vHighlightType = null;     // Type of highlight (Scan, Bridge, Winner)
    
    // Animation Control
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final Object stepLock = new Object();
    private volatile boolean stepRequested = false;
    private volatile int delay = 800; 

    public DivideAndConquerDemo() {
        super("Divide & Conquer Demo (5x5) - Advanced Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Setup Board (Custom Scenario)
        resetBoard();

        // 2. Control Panel (Top)
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        startBtn = new JButton("Start");
        pauseBtn = new JButton("Pause");
        stepBtn = new JButton("Step");
        resetBtn = new JButton("Reset");
        
        pauseBtn.setEnabled(false);
        stepBtn.setEnabled(false);
        
        speedSlider = new JSlider(0, 1500, 800);
        speedSlider.setInverted(true); // Left = Fast (0ms), Right = Slow (1500ms)
        speedSlider.setToolTipText("Animation Speed");
        
        startBtn.addActionListener(e -> startAnimation());
        pauseBtn.addActionListener(e -> togglePause());
        stepBtn.addActionListener(e -> requestStep());
        resetBtn.addActionListener(e -> resetDemo());
        speedSlider.addChangeListener(e -> delay = speedSlider.getValue());
        
        controlPanel.add(startBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stepBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(new JLabel("Speed:"));
        controlPanel.add(speedSlider);
        
        add(controlPanel, BorderLayout.NORTH);

        // 3. Main Content (Center + Legend)
        JPanel centerContainer = new JPanel(new BorderLayout());
        
        boardPanel = new DemoPanel();
        centerContainer.add(boardPanel, BorderLayout.CENTER);
        
        // Legend Panel (Right)
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(new TitledBorder("Legend"));
        
        addLegendItem(legendPanel, Color.RED, "Split Line (Mid)");
        addLegendItem(legendPanel, new Color(100, 149, 237, 100), "Active Scope"); // Cornflower Blue
        addLegendItem(legendPanel, Color.ORANGE, "Scanning Base Case");
        addLegendItem(legendPanel, Color.MAGENTA, "Bridge Check");
        addLegendItem(legendPanel, Color.GREEN, "Winner Move");
        
        centerContainer.add(legendPanel, BorderLayout.EAST);
        add(centerContainer, BorderLayout.CENTER);

        // 4. Log Area (Bottom)
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
        
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void addLegendItem(JPanel p, Color c, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(c);
        if(c.getAlpha() < 255) colorBox.setOpaque(true); // simple fix for viewing
        colorBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        item.add(colorBox);
        item.add(new JLabel(text));
        p.add(item);
    }

    private void resetBoard() {
        board = new char[][]{
            {'r', 'r', 'b', 'b', 'g'},
            {'r', 'b', 'b', 'b', 'g'},
            {'g', 'g', 'r', 'r', 'r'},
            {'b', 'b', 'r', 'g', 'g'},
            {'b', 'r', 'r', 'g', 'b'}
        };
        vColStart = -1; vColEnd = -1; vMid = -1;
        vHighlightBlock = null; vHighlightType = null;
    }
    
    private void resetDemo() {
        // Stop any running thread? (Simple approach: just reset UI, logic might continue for a bit but it's a demo)
        // ideally we interrupt.
        resetBoard();
        logArea.setText("");
        boardPanel.repaint();
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        stepBtn.setEnabled(false);
    }

    private void startAnimation() {
        startBtn.setEnabled(false);
        pauseBtn.setEnabled(true);
        resetBtn.setEnabled(false);
        logArea.setText("Starting Algorithm...\n");
        
        new Thread(() -> {
            solveWithVis(board, 0, board[0].length, 0);
            updateVis(-1, -1, -1, null, null, "Done! Algorithm Finished.");
            SwingUtilities.invokeLater(() -> {
                resetBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
                stepBtn.setEnabled(false);
            });
        }).start();
    }
    
    private void togglePause() {
        boolean p = isPaused.get();
        isPaused.set(!p);
        pauseBtn.setText(!p ? "Resume" : "Pause");
        stepBtn.setEnabled(!p);
        
        if (p) { // If we were paused and now resuming, notify
            synchronized(stepLock) {
                stepLock.notifyAll();
            }
        }
    }
    
    private void requestStep() {
        stepRequested = true;
        synchronized(stepLock) {
            stepLock.notifyAll();
        }
    }

    private void updateVis(int c1, int c2, int mid, List<int[]> block, String type, String msg) {
        this.vColStart = c1;
        this.vColEnd = c2;
        this.vMid = mid;
        this.vHighlightBlock = block;
        this.vHighlightType = type;
        
        SwingUtilities.invokeLater(() -> {
            boardPanel.repaint();
            if(msg != null) {
                logArea.append(msg + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
        
        // WaitLogic
        try {
            if (isPaused.get()) {
                synchronized(stepLock) {
                    while (isPaused.get() && !stepRequested) {
                        stepLock.wait();
                    }
                    stepRequested = false; // reset step
                }
            } else {
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {}
    }

    // --- Algorithm with Visualization ---

    private MoveResult solveWithVis(char[][] board, int colStart, int colEnd, int depth) {
        String indent = "  ".repeat(depth);
        int width = colEnd - colStart;
        
        updateVis(colStart, colEnd, -1, null, null, indent + "-> Range [" + colStart + ", " + colEnd + ")");
        
        // Base Case
        if (width <= 2) {
            MoveResult res = scanRegion(board, colStart, colEnd);
            updateVis(colStart, colEnd, -1, res != null ? getBlock(res.r, res.c, colStart, colEnd) : null, "Scan", indent + "   Base Case Best: " + (res != null ? res.score : 0));
            return res;
        }

        // Divide
        int mid = colStart + (width / 2);
        updateVis(colStart, colEnd, mid, null, "Split", indent + "   Splitting at " + mid);

        // Conquer
        MoveResult leftBest = solveWithVis(board, colStart, mid, depth + 1);
        MoveResult rightBest = solveWithVis(board, mid, colEnd, depth + 1);

        int leftScore = (leftBest != null) ? leftBest.score : -1;
        int rightScore = (rightBest != null) ? rightBest.score : -1;

        // Show Combine Step
        updateVis(colStart, colEnd, mid, null, "Combine", indent + "   Combining: Left Best = " + Math.max(0, leftScore) + ", Right Best = " + Math.max(0, rightScore));

        // Combine (Find Bridge)
        MoveResult bridgeBest = findBestBridgeWithVis(board, mid, indent);
        
        // Pick Winner
        int bridgeScore = (bridgeBest != null) ? bridgeBest.score : -1;

        MoveResult winner;
        String winType;
        if (bridgeScore >= leftScore && bridgeScore >= rightScore) {
            winner = bridgeBest;
            winType = "Bridge";
        } else if (leftScore >= rightScore) {
            winner = leftBest;
            winType = "Left (Existing)";
        } else {
            winner = rightBest;
            winType = "Right (Existing)";
        }
        
        // When showing the winner, if it was Left/Right, we still have to respect bounds for the score!
        List<int[]> highlightBlock = null;
        if (winner != null) {
            if ("Bridge".equals(winType)) {
                highlightBlock = getBlock(winner.r, winner.c, -1, -1); // Bridge uses global scope
            } else {
                highlightBlock = getBlock(winner.r, winner.c, colStart, colEnd);
            }
        }
        
        updateVis(colStart, colEnd, -1, highlightBlock, "Winner", indent + "<- Winner: " + winType + " (" + (winner!=null?winner.score:0) + ")");
        return winner;
    }

    private MoveResult scanRegion(char[][] board, int colStart, int colEnd) {
        int rows = board.length;
        MoveResult best = null;
        int max = -1;
        boolean[][] visited = new boolean[rows][board[0].length];

        for (int i = 0; i < rows; i++) {
            for (int j = colStart; j < colEnd; j++) {
                if (board[i][j] != '0' && !visited[i][j]) {
                    List<int[]> block = findBlockBounded(board, i, j, board[i][j], colStart, colEnd);
                    for(int[] c : block) visited[c[0]][c[1]] = true; // Mark visited

                    if (block.size() >= 2) {
                        // Briefly highlight each potential block being scanned
                        updateVis(colStart, colEnd, -1, block, "Scanning", null); 

                        int score = (int) Math.pow(block.size() - 1, 2);
                        if (score > max) {
                            max = score;
                            best = new MoveResult(i, j, score);
                        }
                    }
                }
            }
        }
        return best;
    }

    private MoveResult findBestBridgeWithVis(char[][] board, int mid, String indent) {
        int rows = board.length;
        MoveResult best = null;
        int max = -1;
        boolean[] processedRow = new boolean[rows];

        for (int i = 0; i < rows; i++) {
            if (board[i][mid-1] != '0' && !processedRow[i]) {
                List<int[]> block = GameEngine.findBlock(board, i, mid-1, board[i][mid-1]);
                for(int[] c : block) if (c[1]==mid-1) processedRow[c[0]] = true;

                boolean isBridge = false;
                for(int[] c : block) if (c[1] >= mid) isBridge = true;

                if (isBridge && block.size() >= 2) {
                    updateVis(vColStart, vColEnd, mid, block, "BridgeCheck", indent + "     Checking Bridge...");
                    int score = (int) Math.pow(block.size() - 1, 2);
                    if (score > max) {
                        max = score;
                        best = new MoveResult(i, mid-1, score);
                    }
                }
            }
        }
        return best;
    }

    private List<int[]> getBlock(int r, int c, int colStart, int colEnd) {
        if (colStart == -1 || colEnd == -1) {
            return GameEngine.findBlock(board, r, c, board[r][c]); // Global
        }
        return findBlockBounded(board, r, c, board[r][c], colStart, colEnd);
    }
    
    // Helper: Bounded Flood Fill
    private List<int[]> findBlockBounded(char[][] board, int r, int c, char color, int colStart, int colEnd) {
        List<int[]> block = new java.util.ArrayList<>();
        if (r < 0 || r >= board.length || c < colStart || c >= colEnd) return block;
        if (board[r][c] != color) return block;
        boolean[][] visited = new boolean[board.length][board[0].length];
        findBlockRecursiveBounded(board, r, c, color, colStart, colEnd, visited, block);
        return block;
    }

    private void findBlockRecursiveBounded(char[][] board, int r, int c, char color, int colStart, int colEnd, boolean[][] visited, List<int[]> block) {
        if (r < 0 || r >= board.length || c < colStart || c >= colEnd) return;
        if (visited[r][c]) return;
        if (board[r][c] != color) return;
        
        visited[r][c] = true;
        block.add(new int[] {r, c});
        
        findBlockRecursiveBounded(board, r + 1, c, color, colStart, colEnd, visited, block);
        findBlockRecursiveBounded(board, r - 1, c, color, colStart, colEnd, visited, block);
        findBlockRecursiveBounded(board, r, c + 1, color, colStart, colEnd, visited, block);
        findBlockRecursiveBounded(board, r, c - 1, color, colStart, colEnd, visited, block);
    }

    // --- Inner Classes ---

    private static class MoveResult {
        int r, c, score;
        MoveResult(int r, int c, int score) { this.r=r; this.c=c; this.score=score; }
    }

    private class DemoPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int rows = 5;
            int cols = 5;
            
            // Adjust to keep aspect ratio or fill? Let's simply fill but keep square cells if possible
            int cellW = getWidth() / cols;
            int cellH = getHeight() / rows;
            // Make cells square
            int dim = Math.min(cellW, cellH);
            int marginX = (getWidth() - (dim * cols)) / 2;
            int marginY = (getHeight() - (dim * rows)) / 2;

            if (board == null) return;
            
            // Draw Range Background Scope
            if (vColStart != -1) {
                g.setColor(new Color(100, 149, 237, 50)); // Transparent Blue
                int x = marginX + vColStart * dim;
                int w = (vColEnd - vColStart) * dim;
                g.fillRect(x, marginY, w, dim * rows);
                
                g.setColor(new Color(100, 149, 237));
                g.drawRect(x, marginY, w, dim * rows);
            }

            // Draw Board Cells
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int x = marginX + j * dim;
                    int y = marginY + i * dim;
                    
                    // Grid
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y, dim, dim);

                    // Piece
                    char c = board[i][j];
                    if (c != '0') {
                        drawPiece((Graphics2D)g, x, y, dim, c);
                    }
                }
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Split Line
            if (vMid != -1) {
                int lineX = marginX + vMid * dim;
                g2.setColor(Color.RED);
                g2.drawLine(lineX, marginY, lineX, marginY + dim * rows);
            }

            // Draw Block Highlights
            if (vHighlightBlock != null) {
                if ("BridgeCheck".equals(vHighlightType) || "Bridge".equals(vHighlightType)) g2.setColor(Color.MAGENTA);
                else if ("Winner".equals(vHighlightType)) g2.setColor(Color.GREEN);
                else g2.setColor(Color.ORANGE);

                for (int[] p : vHighlightBlock) {
                    int r = p[0];
                    int c = p[1];
                    int x = marginX + c * dim;
                    int y = marginY + r * dim;
                    g2.drawRoundRect(x + 2, y + 2, dim - 4, dim - 4, 10, 10);
                }
            }
        }
        
        private void drawPiece(Graphics2D g2, int x, int y, int dim, char c) {
            Color color = getColor(c);
            
            // Gradient fill
            GradientPaint gp = new GradientPaint(x, y, color.brighter(), x + dim, y + dim, color.darker());
            g2.setPaint(gp);
            g2.fillRoundRect(x + 4, y + 4, dim - 8, dim - 8, 15, 15);
            
            // Border
            g2.setColor(color.darker().darker());
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x + 4, y + 4, dim - 8, dim - 8, 15, 15);
        }
    }

    private Color getColor(char c) {
        // Matches GameEngine/MainClass colors logic
        switch (c) {
            case 'g': return Color.YELLOW;    // g in logic -> Yellow UI
            case 'r': return Color.RED;
            case 'b': return Color.BLUE;
            case 'v': return Color.GREEN;     // v -> Green
            case 'o': return Color.ORANGE;
            case 'm': return Color.MAGENTA;
            case 'c': return Color.CYAN;
            default: return Color.GRAY;
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e){}
        SwingUtilities.invokeLater(DivideAndConquerDemo::new);
    }
}

