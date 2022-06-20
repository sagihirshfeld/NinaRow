package Moves;

import Board.Board;

import java.awt.*;
import java.util.Comparator;
import java.util.LinkedList;

public class QuitMove implements Move {

    private final int discNum;
    private LinkedList<Point> removedDiscsPositions;

    public QuitMove(int discNum) {
        this.discNum = discNum;
    }

    @Override
    public int getDiscNum() {
        return discNum;
    }

    public LinkedList<Point> getRemovedDiscsPositions(){
        return removedDiscsPositions;
    }

    public void setRemovedDiscsPositions(LinkedList<Point> removedDiscsPositions){
        this.removedDiscsPositions = removedDiscsPositions;
    }

    @Override
    public void undoFrom(Board gameBoard) {
        Comparator<Point> comparator = (pointA, pointB) -> pointB.y - pointA.y;
        removedDiscsPositions
                .stream()
                .sorted(comparator)
                .forEach(deletedPosition -> {
                    for(int row = 0; row < deletedPosition.y; row++){
                        gameBoard.getMatrix()[row][deletedPosition.x] = gameBoard.getMatrix()[row + 1][deletedPosition.x];
                    }

                    gameBoard.getMatrix()[deletedPosition.y][deletedPosition.x] = discNum;
                });
    }
}
