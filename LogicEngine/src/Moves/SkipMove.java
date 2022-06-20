package Moves;

import Board.Board;

public class SkipMove implements Move {

    private final int discNum;

    public SkipMove(int discNum){
        this.discNum = discNum;
    }

    @Override
    public int getDiscNum() {
        return discNum;
    }

    @Override
    public void undoFrom(Board gameBoard) {
        // do nothing
    }
}
