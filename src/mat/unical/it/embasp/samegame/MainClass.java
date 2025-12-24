package mat.unical.it.embasp.samegame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
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
    
    private static AIPlayer aiPlayer = new AIPlayer();
    
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
        
        int[] hintMove = aiPlayer.getMove(aiMode, samegame); 
        
        if (hintMove != null) {
            char blockColorChar = samegame[hintMove[0]][hintMove[1]];
            List<int[]> block = GameEngine.findBlock(samegame, hintMove[0], hintMove[1], blockColorChar);
            
            Color contrastColor = getContrastColor(blockColorChar);
            
            for (int[] cell : block) {
                if (cell[0] < ROWS && cell[1] < COLS) {
                    JTextField tf = gridUI[cell[0]][cell[1]];
                    if (tf != null) {
                        tf.setBorder(new LineBorder(contrastColor, 4)); 
                    }
                }
            }
            frame.repaint();
            
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
        // Reset state
        playerScore = 0;
        aiScore = 0;
        isPlayerTurn = true;
        finished = false;
        if(aiPlayer != null) aiPlayer.clearMemo();
                
        samegame = GameEngine.createNewBoard(ROWS, COLS, NUM_COLORS);
        
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
        
        List<int[]> block = GameEngine.findBlock(samegame, r, c, samegame[r][c]);
        if (block.size() < 2) return;
        
        // Remove block
        for (int[] cell : block) {
            samegame[cell[0]][cell[1]] = '0';
        }
        
        playerScore += (int)Math.pow(block.size() - 1, 2);
        GameEngine.applyGravity(samegame);
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
        
        int[] bestMove = aiPlayer.getMove(aiMode, samegame);
        
        if (bestMove != null) {
            // Highlight
            List<int[]> block = GameEngine.findBlock(samegame, bestMove[0], bestMove[1], samegame[bestMove[0]][bestMove[1]]);
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
            if (!finished) { 
                isPlayerTurn = true;
                updateTitle();
            }
        }
    }
    
    // Separated execution logic
    private static void executeAIMove(int[] bestMove) {
        List<int[]> block = GameEngine.findBlock(samegame, bestMove[0], bestMove[1], samegame[bestMove[0]][bestMove[1]]);
        int score = (int)Math.pow(block.size() - 1, 2);
        for (int[] cell : block) {
            samegame[cell[0]][cell[1]] = '0';
        }
        aiScore += score;
        
        GameEngine.applyGravity(samegame);
        checkGameOver();
        
        if (!finished) {
            isPlayerTurn = true;
            updateTitle();
        }
        refreshBoardUI();
    }
    
    private static void checkGameOver() {
        if (GameEngine.isGameOver(samegame)) {
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
}
