package mat.unical.it.embasp.samegame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

public class MainClass {

    private MainClass() {}

    private static boolean finished = false;
    private static JFrame frame;
    private static JPanel boardPanel;
    private static JTextField[][] gridUI; 
    
    // Game State
    private static int playerScore = 0;
    private static int aiScore = 0;
    private static boolean isPlayerTurn = true; 
    
    // Difficulty / AI Mode
    // 0 = Greedy, 1 = D&C, 2 = Backtracking, 3 = DP, 4 = Random (Beginner)
    private static int aiMode = 0; 
    
    // Board config
    private static int ROWS = 10;
    private static int COLS = 20;
    private static int NUM_COLORS = 4; // Default
    private static char[][] samegame; 
    
    // For DP
    private static java.util.Map<String, Integer> memoMap = new java.util.HashMap<>();
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
            startGame();
        });
    }
    
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        frame = new JFrame("SameGame AI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // --- Menu Bar ---
        JMenuBar menuBar = new JMenuBar();
        
        // Game Menu
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newItem = new JMenuItem("New Game");
        newItem.addActionListener(e -> startGame());
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        
        gameMenu.add(newItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);
        
        // Hint Menu
        JMenu hintMenu = new JMenu("Hint");
        hintMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showHint();
            }
        });
        menuBar.add(hintMenu);
        
        // Mode Menu
        JMenu modeMenu = new JMenu("AI Strategy");
        ButtonGroup group = new ButtonGroup();
        
        addModeItem(modeMenu, group, "Solo (No AI)", -1);
        addModeItem(modeMenu, group, "Greedy (Review 1)", 0);
        addModeItem(modeMenu, group, "Divide & Conquer (Review 2)", 1);
        addModeItem(modeMenu, group, "Backtracking (Review 3)", 2);
        addModeItem(modeMenu, group, "Dynamic Programming (Review 3)", 3);
        addModeItem(modeMenu, group, "Beginner (Random)", 4);
        
        menuBar.add(modeMenu);
        
        // Options Menu
        JMenu optionsMenu = new JMenu("Options");
        
        JMenuItem sizeItem = new JMenuItem("Set Grid Size...");
        sizeItem.addActionListener(e -> showGridSizeDialog());
        optionsMenu.add(sizeItem);
        
        JMenuItem colorItem = new JMenuItem("Set Number of Colors...");
        colorItem.addActionListener(e -> showColorDialog());
        optionsMenu.add(colorItem);
        
        menuBar.add(optionsMenu);
        
        frame.setJMenuBar(menuBar);
        
        // Board Panel
        boardPanel = new JPanel();
        frame.add(boardPanel, BorderLayout.CENTER);
        
        frame.setSize(800, 600); // Initial size
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static void addModeItem(JMenu menu, ButtonGroup group, String name, int mode) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
        if (mode == aiMode) item.setSelected(true);
        item.addActionListener(e -> {
            aiMode = mode;
            startGame(); 
        });
        group.add(item);
        menu.add(item);
    }
    
    private static void showGridSizeDialog() {
        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField rowField = new JTextField(String.valueOf(ROWS));
        JTextField colField = new JTextField(String.valueOf(COLS));
        p.add(new javax.swing.JLabel("Rows:"));
        p.add(rowField);
        p.add(new javax.swing.JLabel("Cols:"));
        p.add(colField);
        
        int result = JOptionPane.showConfirmDialog(frame, p, "Set Grid Dimensions", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int r = Integer.parseInt(rowField.getText().trim());
                int c = Integer.parseInt(colField.getText().trim());
                if (r < 2 || c < 2 || r > 50 || c > 50) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid values (2-50).");
                    return;
                }
                ROWS = r;
                COLS = c;
                startGame();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid number format.");
            }
        }
    }
    
    private static void showColorDialog() {
        String input = JOptionPane.showInputDialog(frame, "Enter number of colors (3-7):", NUM_COLORS);
        if (input != null) {
            try {
                int n = Integer.parseInt(input.trim());
                if (n < 3 || n > 7) {
                    JOptionPane.showMessageDialog(frame, "Please enter a value between 3 and 7.");
                    return;
                }
                NUM_COLORS = n;
                startGame();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid number.");
            }
        }
    }
    
    private static void showHint() {
        if (finished || !isPlayerTurn) return;
        
        // Use Greedy or Backtracking to find a "good" move
        int[] hintMove = getGreedyMove(samegame); 
        
        if (hintMove != null) {
            char blockColorChar = samegame[hintMove[0]][hintMove[1]];
            List<int[]> block = findBlock(hintMove[0], hintMove[1], blockColorChar);
            
            Color contrastColor = getContrastColor(blockColorChar);
            
            // Highlight in Contrast Color
            for (int[] cell : block) {
                if (cell[0] < ROWS && cell[1] < COLS) {
                    JTextField tf = gridUI[cell[0]][cell[1]];
                    if (tf != null) {
                        tf.setBorder(new LineBorder(contrastColor, 4)); 
                    }
                }
            }
            frame.repaint();
            
            // Timer to clear highlight
            Timer t = new Timer(1500, e -> {
                 for (int[] cell : block) {
                    if (cell[0] < ROWS && cell[1] < COLS) {
                        JTextField tf = gridUI[cell[0]][cell[1]];
                        if (tf != null) tf.setBorder(null);
                    }
                }
                frame.repaint();
            });
            t.setRepeats(false);
            t.start();
        } else {
            JOptionPane.showMessageDialog(frame, "No move possible!");
        }
    }
    
    private static Color getContrastColor(char c) {
        switch (c) {
            case 'b': return Color.YELLOW;    // Blue -> Yellow
            case 'g': return Color.BLUE;      // Yellow -> Blue
            case 'r': return Color.CYAN;      // Red -> Cyan
            case 'v': return Color.MAGENTA;   // Green -> Magenta
            case 'o': return Color.BLUE;      // Orange -> Blue
            case 'm': return Color.GREEN;     // Magenta -> Green
            case 'c': return Color.RED;       // Cyan -> Red
            default: return Color.GREEN;      // Default
        }
    }
    
    private static void startGame() {
        Random r = new Random();
        
        // Reset state
        playerScore = 0;
        aiScore = 0;
        isPlayerTurn = true;
        finished = false;
        memoMap.clear();
        
        // Re-init board
        samegame = new char[ROWS][COLS];
        
        for (int i = 0; i < ROWS; ++i)
            for (int j = 0; j < COLS; ++j) {
                int colore =  r.nextInt(0, NUM_COLORS); 
                switch (colore) {
                case 0: samegame[i][j] = 'b'; break; // Blue
                case 1: samegame[i][j] = 'g'; break; // Yellow
                case 2: samegame[i][j] = 'r'; break; // Red
                case 3: samegame[i][j] = 'v'; break; // Green
                case 4: samegame[i][j] = 'o'; break; // Orange
                case 5: samegame[i][j] = 'm'; break; // Magenta
                case 6: samegame[i][j] = 'c'; break; // Cyan
                }
            }
        
        refreshBoardUI();
    }
    
    private static void refreshBoardUI() {
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(ROWS, COLS));
        gridUI = new JTextField[ROWS][COLS];
        
        for (int i = 0; i < ROWS; ++i) {
            for (int j = 0; j < COLS; ++j) {
                JTextField text = new JTextField("");
                text.setHorizontalAlignment(JTextField.CENTER);
                text.setEditable(false);
                gridUI[i][j] = text; // Store reference
                
                char cVal = samegame[i][j];
                if (cVal != '0') {
                    switch(cVal) {
                    case 'g': text.setBackground(Color.YELLOW); break;
                    case 'r': text.setBackground(Color.RED); break;
                    case 'b': text.setBackground(Color.BLUE); break;
                    case 'v': text.setBackground(Color.GREEN); break;
                    case 'o': text.setBackground(Color.ORANGE); break;
                    case 'm': text.setBackground(Color.MAGENTA); break;
                    case 'c': text.setBackground(Color.CYAN); break;
                    }
                    
                    final int r = i;
                    final int c = j;
                    text.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            handleMouseClick(r, c);
                        }
                    });
                } else {
                    text.setBackground(Color.WHITE); 
                    text.setBorder(null); 
                }
                boardPanel.add(text);
            }
        }
        
        updateTitle();
        frame.revalidate();
        frame.repaint();
    }
    
    private static void updateTitle() {
        if (finished) return;
        
        String algoName = switch(aiMode) {
            case -1 -> "Solo Mode";
            case 0 -> "Greedy";
            case 1 -> "D&C";
            case 2 -> "Backtracking";
            case 3 -> "DP";
            case 4 -> "Beginner";
            default -> "Unknown";
        };
        
        if (aiMode == -1) {
            frame.setTitle("SameGame [" + algoName + "] | Size: " + ROWS + "x" + COLS + " | Colors: " + NUM_COLORS + " | Score: " + playerScore);
        } else {
            String turnStr = isPlayerTurn ? "(Your Turn)" : "(AI Thinking...)";
            frame.setTitle("SameGame [" + algoName + "] | Size: " + ROWS + "x" + COLS + " | Colors: " + NUM_COLORS + " | You: " + playerScore + " | AI: " + aiScore + " " + turnStr);
        }
    }
    
    private static void handleMouseClick(int r, int c) {
        if (finished) return;
        if (!isPlayerTurn) return; 
        if (samegame[r][c] == '0') return;
        
        List<int[]> block = findBlock(r, c, samegame[r][c]);
        if (block.size() < 2) return;
        
        // Remove block
        for (int[] cell : block) {
            samegame[cell[0]][cell[1]] = '0';
        }
        
        playerScore += (int)Math.pow(block.size() - 1, 2);
        cambiaMatrice(samegame);
        checkGameOver();
        refreshBoardUI();
        
        if (!finished) {
            if (aiMode == -1) {
                // Solo Mode: Stay on player turn
                isPlayerTurn = true; 
                updateTitle();
            } else {
                isPlayerTurn = false;
                updateTitle();
                // Start AI thinking logic
                SwingUtilities.invokeLater(() -> {
                    prepareAIMove();
                });
            }
        }
    }
    
    private static void prepareAIMove() {
        if (finished) return;
        System.out.println("AI Thinking... Mode: " + aiMode);
        
        int[] bestMove = null;
        switch (aiMode) {
        case 0: bestMove = getGreedyMove(samegame); break;
        case 1: bestMove = getDCMove(samegame); break;
        case 2: bestMove = getBacktrackingMove(samegame, 2); break;
        case 3: 
            memoMap.clear(); 
            bestMove = getDPMove(samegame, 2); 
            break;
        case 4: bestMove = getRandomMove(samegame); break;
        }
        
        if (bestMove != null) {
            // Highlight
            List<int[]> block = findBlock(bestMove[0], bestMove[1], samegame[bestMove[0]][bestMove[1]]);
            for (int[] cell : block) {
                JTextField tf = gridUI[cell[0]][cell[1]];
                tf.setBorder(new LineBorder(Color.BLACK, 3));
            }
            frame.repaint();
            
            final int[] move = bestMove;
            
            // Execute after 1 second
            Timer t = new Timer(1000, e -> executeAIMove(move));
            t.setRepeats(false);
            t.start();
        } else {
            // No moves
            checkGameOver();
            // If AI has no moves, check logic actually ends game in checkGameOver usually.
            // But if we return here, we must ensure state is consistent.
            if (!finished) { 
                isPlayerTurn = true;
                updateTitle();
            }
        }
    }
    
    // Separated execution logic
    private static void executeAIMove(int[] bestMove) {
        List<int[]> block = findBlock(bestMove[0], bestMove[1], samegame[bestMove[0]][bestMove[1]]);
        int score = (int)Math.pow(block.size() - 1, 2);
        for (int[] cell : block) {
            samegame[cell[0]][cell[1]] = '0';
        }
        aiScore += score;
        
        cambiaMatrice(samegame);
        checkGameOver();
        
        if (!finished) {
            isPlayerTurn = true;
            updateTitle();
        }
        refreshBoardUI();
    }
    
    // --- AI Strategies ---
    
    private static int[] getRandomMove(char[][] board) {
        List<int[]> moves = getAllMoves(board);
        if (moves.isEmpty()) return null;
        Random r = new Random();
        return moves.get(r.nextInt(moves.size()));
    }
    
    private static int[] getGreedyMove(char[][] board) {
        List<int[]> moves = getAllMoves(board);
        moves.sort((a, b) -> b[2] - a[2]); 
        if (moves.isEmpty()) return null;
        return moves.get(0); 
    }
    
    private static int[] getDCMove(char[][] board) {
        // Dynamic midpoint
        int mid = board[0].length / 2;
        int[] bestLeft = getBestLocalMove(board, 0, mid);
        int[] bestRight = getBestLocalMove(board, mid, board[0].length);
        
        if (bestLeft == null) return bestRight;
        if (bestRight == null) return bestLeft;
        
        if (bestLeft[2] >= bestRight[2]) return bestLeft;
        else return bestRight;
    }
    
    private static int[] getBestLocalMove(char[][] board, int starCol, int endCol) {
        int maxScore = -1;
        int[] best = null;
        boolean[][] visited = new boolean[board.length][board[0].length];
        
        for(int i=0; i<board.length; ++i) {
            for(int j=starCol; j<endCol; ++j) {
                if(board[i][j] != '0' && !visited[i][j]) {
                    List<int[]> block = findBlockInBoard(board, i, j, board[i][j]);
                    for(int[] b : block) visited[b[0]][b[1]] = true; 
                    
                    if (block.size() >= 2) {
                        int score = (int)Math.pow(block.size() - 1, 2);
                        if (score > maxScore) {
                            maxScore = score;
                            best = new int[] {i, j, score};
                        }
                    }
                }
            }
        }
        return best;
    }
    
    private static int[] getBacktrackingMove(char[][] board, int depth) {
        if (depth == 0) return getGreedyMove(board);
        
        List<int[]> moves = getAllMoves(board);
        moves.sort((a, b) -> b[2] - a[2]);
        
        int branchLimit = Math.min(moves.size(), 5);
        int maxVal = -1;
        int[] bestMove = null;
        
        for(int i=0; i<branchLimit; ++i) {
            int[] move = moves.get(i);
            int currentScore = move[2];
            char[][] nextBoard = copyBoard(board);
            simulateMove(nextBoard, move[0], move[1]);
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
    
    private static int backtrackingVal(char[][] board, int depth) {
        if (depth == 0) return 0;
        List<int[]> moves = getAllMoves(board);
        if (moves.isEmpty()) return 0;
        moves.sort((a, b) -> b[2] - a[2]);
        return moves.get(0)[2];
    }

    private static int[] getDPMove(char[][] board, int depth) {
        List<int[]> moves = getAllMoves(board);
        moves.sort((a, b) -> b[2] - a[2]);
        
        int branchLimit = Math.min(moves.size(), 5);
        int maxVal = -1;
        int[] bestMove = null;
        
        for(int i=0; i<branchLimit; ++i) {
            int[] move = moves.get(i);
            char[][] nextBoard = copyBoard(board);
            simulateMove(nextBoard, move[0], move[1]);
            int val = move[2] + getDPValue(nextBoard, depth - 1);
            if (val > maxVal) {
                maxVal = val;
                bestMove = move;
            }
        }
        return bestMove;
    }
    
    private static int getDPValue(char[][] board, int depth) {
        if (depth == 0) return 0;
        
        String key = boardToString(board) + ":" + depth;
        if (memoMap.containsKey(key)) return memoMap.get(key);
        
        List<int[]> moves = getAllMoves(board);
        if (moves.isEmpty()) return 0;
        moves.sort((a, b) -> b[2] - a[2]);
        
        int best = 0;
        int branchLimit = Math.min(moves.size(), 3); 
        
        for(int i=0; i<branchLimit; ++i) {
            char[][] next = copyBoard(board);
            simulateMove(next, moves.get(i)[0], moves.get(i)[1]);
            int val = moves.get(i)[2] + getDPValue(next, depth - 1);
            if (val > best) best = val;
        }
        
        memoMap.put(key, best);
        return best;
    }
    
    // --- Helpers ---
    
    private static List<int[]> getAllMoves(char[][] board) {
        List<int[]> moves = new ArrayList<>();
        boolean[][] visited = new boolean[board.length][board[0].length];
        for(int i=0; i<board.length; ++i) {
            for(int j=0; j<board[0].length; ++j) {
                if(board[i][j] != '0' && !visited[i][j]) {
                    List<int[]> block = findBlockInBoard(board, i, j, board[i][j]);
                    for(int[] b : block) visited[b[0]][b[1]] = true;
                    if (block.size() >= 2) {
                        int score = (int)Math.pow(block.size() - 1, 2);
                        moves.add(new int[] {i, j, score});
                    }
                }
            }
        }
        return moves;
    }
    
    private static void simulateMove(char[][] board, int r, int c) {
        List<int[]> block = findBlockInBoard(board, r, c, board[r][c]);
        for(int[] cell : block) board[cell[0]][cell[1]] = '0';
        cambiaMatrice(board);
    }
    
    private static char[][] copyBoard(char[][] src) {
        char[][] dest = new char[src.length][src[0].length];
        for(int i=0; i<src.length; ++i)
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        return dest;
    }
    
    private static String boardToString(char[][] board) {
        StringBuilder sb = new StringBuilder();
        for(char[] row : board) sb.append(row);
        return sb.toString();
    }
    
    private static List<int[]> findBlockInBoard(char[][] board, int r, int c, char color) {
        List<int[]> block = new ArrayList<>();
        boolean[][] visited = new boolean[board.length][board[0].length];
        findBlockRecursiveInBoard(board, r, c, color, visited, block);
        return block;
    }
    
    private static void findBlockRecursiveInBoard(char[][] board, int r, int c, char color, boolean[][] visited, List<int[]> block) {
        if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return;
        if (visited[r][c]) return;
        if (board[r][c] != color) return;
        
        visited[r][c] = true;
        block.add(new int[] {r, c});
        
        findBlockRecursiveInBoard(board, r + 1, c, color, visited, block);
        findBlockRecursiveInBoard(board, r - 1, c, color, visited, block);
        findBlockRecursiveInBoard(board, r, c + 1, color, visited, block);
        findBlockRecursiveInBoard(board, r, c - 1, color, visited, block);
    }
    
    private static List<int[]> findBlock(int r, int c, char color) {
        return findBlockInBoard(samegame, r, c, color);
    }

    private static void checkGameOver() {
        boolean movePossible = false;
        // Use current ROWS/COLS
        for(int i=0; i<ROWS; ++i) {
            for(int j=0; j<COLS; ++j) {
                if(samegame[i][j] != '0') {
                    if (i+1 < ROWS && samegame[i+1][j] == samegame[i][j]) movePossible = true;
                    if (j+1 < COLS && samegame[i][j+1] == samegame[i][j]) movePossible = true;
                }
            }
        }
        
        if (!movePossible) {
            finished = true;
            showGameOverDialog();
        }
    }
    
    private static void showGameOverDialog() {
        if (aiMode == -1) {
             JOptionPane.showMessageDialog(frame, "Game Over!\nFinal Score: " + playerScore, "Game Over", JOptionPane.INFORMATION_MESSAGE);
             return;
        }
        
        String winner;
        if (playerScore > aiScore) winner = "You Win!";
        else if (aiScore > playerScore) winner = "AI Wins!";
        else winner = "It's a Draw!";
        
        String algoName = switch(aiMode) {
            case 0 -> "Greedy";
            case 1 -> "D&C";
            case 2 -> "Backtracking";
            case 3 -> "DP";
            case 4 -> "Beginner";
            default -> "Unknown";
        };
        
        String msg = "Algorithm: " + algoName + "\nYou: " + playerScore + " | AI: " + aiScore + "\n" + winner;
        JOptionPane.showMessageDialog(frame, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void cambiaMatrice(char[][] tabella) {
        int R = tabella.length;
        int C = tabella[0].length;
        
        for (int i = 0; i < R; ++i) {
            for (int j = 0; j < C; ++j) {
                if (tabella[i][j] == '0') aggiornaColonna(tabella, j);
            }
        }
        
        for(int j=0; j<C-1; ++j) {
           if(colonnaVuota(tabella, j)) {
               // find next non-empty
               int next = j+1;
               while(next < C && colonnaVuota(tabella, next)) next++;
               if(next < C) {
                   copiaColonna(tabella, j, next);
               }
           }
        }
    } 
    
    private static boolean colonnaVuota(char[][] tabella, int j) {
        for (int i = 0; i < tabella.length; ++i)
            if (tabella[i][j] != '0') return false;
        return true;
    } 
    
    private static void copiaColonna(char[][] tabella, int dest, int src) {
        for (int i = 0; i < tabella.length; ++i) { 
            tabella[i][dest] = tabella[i][src]; 
            tabella[i][src] = '0'; 
        }
    } 

    private static void aggiornaColonna(char[][] tabella, int j) {
        int R = tabella.length;
        char[] tmp = new char[R];
        int c = 0;
        for (int i = 0; i < R; ++i)
            if (tabella[i][j] != '0') tmp[c++] = tabella[i][j];
        for (; c < tmp.length; ++c) tmp[c] = '0';
        for (int i = 0; i < R; ++i) tabella[i][j] = tmp[i];
    }
}
