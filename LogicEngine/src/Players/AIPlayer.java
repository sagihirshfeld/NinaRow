package Players;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Board.Board;
import Moves.*;

public class AIPlayer extends Player implements Serializable {

    public AIPlayer(String playerName ,int discNum, Board gameBoard){
        super(playerName, discNum, gameBoard);
    }

    public Move calculateMove(boolean popoutsIncluded){
        Move calculatedMove;

        if(popoutsIncluded && canPopout()){
            if(gameBoard.isFull()){

                // In case the board is full and the AI can popout
                calculatedMove = getRandomPopoutMove();

            } else {
                calculatedMove = getRandomRegularOrPopoutMove();
            }

        } else {
            calculatedMove = getRandomRegularMove();
        }

        return calculatedMove;
    }

    private Move getRandomRegularOrPopoutMove() {
        // Choose popout over regular for only 1/6 of the times
        int randomNum = new Random().nextInt(6);
        if(randomNum >= 1){
            return getRandomRegularMove();

        } else {
            return getRandomPopoutMove();
        }
    }

    private Move getRandomRegularMove(){
        int columnToInsert = getRandomOpenColumn();
        return new RegularMove(columnToInsert, discNum);
    }

    private int getRandomOpenColumn(){
        List<Integer> openColumns = getOpenColumns();
        int chosenColumnIndex = new Random().nextInt(openColumns.size());
        return openColumns.get(chosenColumnIndex);
    }

    private List<Integer> getOpenColumns(){
        List<Integer> openColumns = new ArrayList<>();
        for(int col = 0; col < gameBoard.getBoardWidth(); col++) {
            if (gameBoard.getMatrix()[0][col] == 0) {
                openColumns.add(col);
            }
        }

        return openColumns;
    }

    private boolean canPopout() {
        return gameBoard.floorContainsDiscNum(discNum);
    }

    private Move getRandomPopoutMove(){
            int columnToInsert = getRandomPopoutableColumn();
            return new PopoutMove(columnToInsert, discNum);
    }

    private int getRandomPopoutableColumn() {
        List<Integer> popoutableColumns = getRandomPopoutableColumns();
        int chosenColumnIndex = new Random().nextInt(popoutableColumns.size());
        return popoutableColumns.get(chosenColumnIndex);
    }

    private List<Integer> getRandomPopoutableColumns() {
        List<Integer> openColumns = new ArrayList<>();
        for(int col = 0; col < gameBoard.getBoardWidth(); col++) {
            if (gameBoard.getMatrix()[gameBoard.getBoardHeight() - 1][col] == discNum) {
                openColumns.add(col);
            }
        }

        return openColumns;
    }
}
