package mat.unical.it.embasp.samegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    // --- Board Manipulation / Mechanics ---

    public static List<int[]> findBlock(char[][] board, int r, int c, char color) {
        List<int[]> block = new ArrayList<>();
        if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return block;
        if (board[r][c] != color) return block;
        boolean[][] visited = new boolean[board.length][board[0].length];
        findBlockRecursive(board, r, c, color, visited, block);
        return block;
    }
    private static void findBlockRecursive(char[][] board, int r, int c, char color, boolean[][] visited, List<int[]> block) {
        if (r < 0 || r >= board.length || c < 0 || c >= board[0].length) return;
        if (visited[r][c]) return;
        if (board[r][c] != color) return;
        
        visited[r][c] = true;
        block.add(new int[] {r, c});
        
        findBlockRecursive(board, r + 1, c, color, visited, block);
        findBlockRecursive(board, r - 1, c, color, visited, block);
        findBlockRecursive(board, r, c + 1, color, visited, block);
        findBlockRecursive(board, r, c - 1, color, visited, block);
    }

    public static void applyGravity(char[][] board) {
        int R = board.length;
        int C = board[0].length;
        
        // 1. Compact columns (gravity downwards)
        for (int j = 0; j < C; ++j) {
           compactColumn(board, j);
        }
        
        // 2. Compact rows (shift left if column is empty)
        for(int j=0; j<C-1; ++j) {
           if(isColumnEmpty(board, j)) {
               // find next non-empty
               int next = j+1;
               while(next < C && isColumnEmpty(board, next)) next++;
               if(next < C) {
                   copyColumn(board, j, next);
               }
           }
        }
    }

    private static boolean isColumnEmpty(char[][] board, int j) {
        for (int i = 0; i < board.length; ++i)
            if (board[i][j] != '0') return false;
        return true;
    }

    private static void copyColumn(char[][] board, int dest, int src) {
        for (int i = 0; i < board.length; ++i) { 
            board[i][dest] = board[i][src]; 
            board[i][src] = '0'; 
        }
    }

    private static void compactColumn(char[][] board, int j) {
        int R = board.length;
        char[] tmp = new char[R];
        int c = 0;
        // Collect non-empty
        for (int i = 0; i < R; ++i)
            if (board[i][j] != '0') tmp[c++] = board[i][j];
        // Fill rest with '0'
        for (; c < tmp.length; ++c) tmp[c] = '0';
        // Copy back
        for (int i = 0; i < R; ++i) board[i][j] = tmp[i];
    }
    
    public static boolean isGameOver(char[][] board) {
        int ROWS = board.length;
        int COLS = board[0].length;
        
        for(int i=0; i<ROWS; ++i) {
            for(int j=0; j<COLS; ++j) {
                if(board[i][j] != '0') {
                    if (i+1 < ROWS && board[i+1][j] == board[i][j]) return false;
                    if (j+1 < COLS && board[i][j+1] == board[i][j]) return false;
                }
            }
        }
        return true;
    }
    public static char[][] copyBoard(char[][] src) {
        char[][] dest = new char[src.length][src[0].length];
        for(int i=0; i<src.length; ++i)
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        return dest;
    }
    public static String boardToString(char[][] board) {
        StringBuilder sb = new StringBuilder();
        for(char[] row : board) sb.append(row);
        return sb.toString();
    }
    
    public static char[][] createNewBoard(int rows, int cols, int numColors) {
        Random r = new Random();
        char[][] board = new char[rows][cols];
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j) {
                int colore =  r.nextInt(numColors); 
                switch (colore) {
                case 0: board[i][j] = 'b'; break; // Blue
                case 1: board[i][j] = 'g'; break; // Yellow
                case 2: board[i][j] = 'r'; break; // Red
                case 3: board[i][j] = 'v'; break; // Green
                case 4: board[i][j] = 'o'; break; // Orange
                case 5: board[i][j] = 'm'; break; // Magenta
                case 6: board[i][j] = 'c'; break; // Cyan
                default: board[i][j] = 'b'; break;
                }
            }
        return board;
    }
    public static java.util.List<int[]> getAllMoves(char[][] board) {
        List<int[]> moves = new ArrayList<>();
        boolean[][] visited = new boolean[board.length][board[0].length];
        for(int i=0; i<board.length; ++i) {
            for(int j=0; j<board[0].length; ++j) {
                if(board[i][j] != '0' && !visited[i][j]) {
                    List<int[]> block = findBlock(board, i, j, board[i][j]);
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
    public static void simulateMove(char[][] board, int r, int c) {
        List<int[]> block = findBlock(board, r, c, board[r][c]);
        for(int[] cell : block) board[cell[0]][cell[1]] = '0';
        applyGravity(board);
    }
}
