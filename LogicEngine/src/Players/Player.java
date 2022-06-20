package Players;

import Moves.*;

import javafx.beans.property.SimpleIntegerProperty;

import java.awt.*;
import java.io.Serializable;
import java.util.LinkedList;
import Board.*;

public class Player implements Serializable {

    protected int discNum;
    protected String playerName;

    // "transient" prevents serialization
    protected transient Board gameBoard;
    public transient SimpleIntegerProperty movesCountProperty;

    public Player(String playerName ,int discNum, Board gameBoard){
        this.playerName = playerName;
        this.discNum = discNum;
        this.gameBoard = gameBoard;
        movesCountProperty = new SimpleIntegerProperty(0);
    }

    public int getDiscNum(){
        return discNum;
    }

    public void setDiscNum(int value) {discNum = value;}

    public int getMovesNum() { return movesCountProperty.getValue(); }

    public void resetMovesNum() { movesCountProperty.setValue(0); }

    public void decreaseMovesNumByOne(){
        movesCountProperty.setValue(movesCountProperty.getValue() - 1);
    }

    public void makeMove(Move move) {
        if(move instanceof RegularMove){
            RegularMove regularMove = (RegularMove)move;
            int landedRow = insertDiscAndGetLandedRow(discNum, regularMove.getSelectedColumn());
            regularMove.setRestingRow(landedRow);

        } else if(move instanceof PopoutMove) {
            popDiscFromBottomOfColumn(((PopoutMove)move).getSelectedColumn());

        } else if(move instanceof QuitMove){
            QuitMove quitMove = (QuitMove) move;
            LinkedList<Point> deletedDiscsPositions = getDiscPositionsAndRemoveThem();
            quitMove.setRemovedDiscsPositions(deletedDiscsPositions);
        }

        if(!(move instanceof QuitMove || move instanceof SkipMove)){
            movesCountProperty.setValue(movesCountProperty.getValue() + 1);
        }
    }

    private LinkedList<Point> getDiscPositionsAndRemoveThem() {
        LinkedList<Point> deletedDiscsPositions = new LinkedList<>();

        for(int row = 0; row < gameBoard.getBoardHeight(); row++){
            for(int col = 0; col < gameBoard.getBoardWidth(); col++){
                if(gameBoard.getMatrix()[row][col] == discNum){
                    deletedDiscsPositions.add(new Point(col, row));
                    removeDiscFromCell(row, col);
                }
            }
        }

        return deletedDiscsPositions;
    }

    private void removeDiscFromCell(int selectedRow, int selectedColumn) {
        for(int row = selectedRow; row > 0; row--){
            gameBoard.getMatrix()[row][selectedColumn] = gameBoard.getMatrix()[row - 1][selectedColumn];
        }

        gameBoard.getMatrix()[0][selectedColumn] = 0;
    }

    private void popDiscFromBottomOfColumn(int selectedColumn) {
        removeDiscFromCell(gameBoard.getBoardHeight() - 1, selectedColumn);
    }

    private int insertDiscAndGetLandedRow(int discNum, int colToInsert) {
        int landedRow = -1;

        for (int row = 0; row < gameBoard.getBoardHeight(); row++) {
            if ((row == (gameBoard.getBoardHeight() - 1) || gameBoard.getMatrix()[row + 1][colToInsert] != 0 )
            && gameBoard.getMatrix()[row][colToInsert] == 0){

                gameBoard.getMatrix()[row][colToInsert] = discNum;
                landedRow = row;
                break;
            }
        }

        return landedRow;
    }


    public String getPlayerName() {
        return playerName;
    }
}
