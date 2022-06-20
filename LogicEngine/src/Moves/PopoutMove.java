package Moves;

import Board.Board;

import java.io.Serializable;

final public class PopoutMove extends SingleDiscMove implements Serializable {

    public PopoutMove(int selectedCol, int discNum){
        super(selectedCol,discNum);
    }

    @Override
    public void undoFrom(Board gameBoard) {
        for(int row = 0; row + 1< gameBoard.getBoardHeight(); row++){
            gameBoard.getMatrix()[row][selectedCol] = gameBoard.getMatrix()[row + 1][selectedCol];
        }

        gameBoard.getMatrix()[gameBoard.getBoardHeight() - 1][selectedCol] = discNum;
    }
}
