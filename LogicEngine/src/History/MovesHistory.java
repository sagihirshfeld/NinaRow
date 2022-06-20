package History;

import Board.Board;
import Moves.Move;
import Players.Player;
import javafx.beans.property.SimpleBooleanProperty;
import java.io.Serializable;
import java.util.*;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import Engine.LogicEngine;


public class MovesHistory implements Serializable, HasListIterator<int[][]>{

    private LinkedList<Move> recordedMoves = new LinkedList<>();
    private Board virtualBoard;
    private int virtualBoardStateIndex;
    private SimpleBooleanProperty canUndoProperty;

    public MovesHistory(LogicEngine containingEngine){
        virtualBoard = new Board(containingEngine.getBoardWidth(), containingEngine.getBoardHeight());
        virtualBoardStateIndex = 0;
        canUndoProperty = new SimpleBooleanProperty(false);
    }

    public void addMove(Move move){
        if(recordedMoves.isEmpty()){
            canUndoProperty.setValue(true);
        }

        recordedMoves.add(move);
    }

    public Move getLastMove(){
        return recordedMoves.isEmpty() ? null : recordedMoves.peekLast();
    }

    public Move getAndRemoveLastMove()
    {
        Move lastMove =  recordedMoves.pollLast();
        if(recordedMoves.isEmpty()){
            canUndoProperty.setValue(false);
        }

        return lastMove;
    }

    public int getRecordedMovesCount(){
        return recordedMoves.size();
    }

    public boolean isEmpty(){
        return recordedMoves.isEmpty();
    }

    public void clear()
    {
        recordedMoves.clear();
        canUndoProperty.setValue(false);
        virtualBoard.reset();
    }

    // Flips the virtualBoard to one of the previous states, using the list of recorded moves
    private int[][] getBoardMatrixAtPreviousState(int desiredStateIndex){
        if(desiredStateIndex > recordedMoves.size()) {
            throw new IndexOutOfBoundsException();
        }

        ListIterator<Move> listIterator= recordedMoves.listIterator(virtualBoardStateIndex);

        while(virtualBoardStateIndex > desiredStateIndex && listIterator.hasPrevious()){
            Move moveToUndo = listIterator.previous();
            moveToUndo.undoFrom(virtualBoard);
            virtualBoardStateIndex--;
        }

        Player virtualPlayer = new Player("VirtualPlayer",0, virtualBoard);
        Move moveToMake;
        while(virtualBoardStateIndex < desiredStateIndex && listIterator.hasNext()){
            moveToMake = listIterator.next();
            virtualPlayer.setDiscNum(moveToMake.getDiscNum());
            virtualPlayer.makeMove(moveToMake);
            virtualBoardStateIndex++;
        }

        // At this point virtualBoardStateIndex == desiredStateIndex
        return virtualBoard.getMatrix();
    }

    @Override
    public ListIterator<int[][]> getListIterator() {
        return new BoardHistoryIterator(this);
    }

    public List<Move> getLastMoves(int numberOfMovesToGet) {
        List<Move> lastMoves = new LinkedList<>();
        ListIterator<Move> recordedMovesIterator = recordedMoves.listIterator(recordedMoves.size() - numberOfMovesToGet);
        while(recordedMovesIterator.hasNext()){
            lastMoves.add(recordedMovesIterator.next());
        }

        return lastMoves;
    }

    private class BoardHistoryIterator implements ListIterator<int[][]>{

        private MovesHistory movesHistorySrc;
        private int currentIndex = - 1;

        public BoardHistoryIterator(MovesHistory movesHistorySrc){
            this.movesHistorySrc = movesHistorySrc;
        }

        @Override
        public boolean hasNext() {
            return movesHistorySrc.recordedMoves.size() != 0 && currentIndex + 1 <= movesHistorySrc.recordedMoves.size();
        }

        @Override
        public int[][] next() {
            return movesHistorySrc.getBoardMatrixAtPreviousState(++currentIndex);
        }

        @Override
        public boolean hasPrevious() {
            return movesHistorySrc.recordedMoves.size() !=0 && currentIndex - 1 >= 0;
        }

        @Override
        public int[][] previous() {
            return movesHistorySrc.getBoardMatrixAtPreviousState(--currentIndex);
        }

        @Override
        public int nextIndex() {
            return currentIndex + 1;
        }

        @Override
        public int previousIndex() {
            return currentIndex - 1;
        }

        @Override
        public void remove() {
            throw new NotImplementedException();
        }

        @Override
        public void set(int[][] ints) {
            throw new NotImplementedException();
        }

        @Override
        public void add(int[][] ints) {
            throw new NotImplementedException();
        }
    }
}
