package Moves;

import Board.Board;

public interface Move {

    int getDiscNum();
    void undoFrom(Board gameBoard);
}
