package Moves;

import Board.Board;

import java.io.Serializable;

final public class RegularMove extends SingleDiscMove implements Serializable {
    private static final int UNASSIGNED = -1;
    private int restingRow = UNASSIGNED;

    public RegularMove(int colToInsert,int discNum){
        super(colToInsert, discNum);
    }

    @Override
    public void undoFrom(Board gameBoard) {
        if(restingRow != UNASSIGNED &&
                restingRow < gameBoard.getBoardHeight() &&
                selectedCol < gameBoard.getBoardWidth()){

            gameBoard.getMatrix()[restingRow][selectedCol] = 0;
        }
    }

    public void setRestingRow(int restingRow){
        this.restingRow = restingRow;
    }

    public int getRestingRow(){
        return restingRow;
    }
}