package Board;

import java.io.Serializable;

public class Board implements Serializable {

    private int[][] matrix = null;
    private int boardHeight;
    private int boardWidth;

    public Board(int width, int height){
        if(matrix == null){
            this.boardWidth = width;
            this.boardHeight = height;
            matrix = new int[height][width];
        }
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int[][] getMatrix(){
        return matrix;
    }

    public void reset() {
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++)
                matrix[row][col] = 0;
        }
    }

    public boolean isFull(){
        boolean isFull = true;

        for(int col = 0; col < boardWidth; col++){
            if(matrix[0][col] == 0){
                isFull = false;
                break;
            }
        }

        return isFull;
    }

    public boolean floorContainsDiscNum(int discNum) {
        boolean hasDiscNum = false;
        for(int col = 0; col < getBoardWidth(); col++){
            if(matrix[boardHeight - 1][col] == discNum){
                hasDiscNum = true;
                break;
            }
        }

        return hasDiscNum;
    }
}
