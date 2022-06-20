package Engine;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import Players.*;
import History.*;
import Moves.*;
import Board.*;

// Singleton implementation
public class LogicEngine implements Serializable {
    private static int UNASSIGNED = -1;
    private static boolean AFTER_UNDO = true;
    private static int MAX_PLAYERS = 6;

    public enum GameVariant {
            Regular,
            Popout,
            Circular
    }

    private GameVariant gameVariant;

    private int gameTarget;
    private Board gameBoard;
    private List<Player> players;
    private PriorityQueue<Integer> availableDiscNumbers;
    private MovesHistory movesHistory;

    private int lastDetectedWinnerDiscNum;
    private SimpleObjectProperty<Player> turnHolderProperty;
    public SimpleIntegerProperty turnHolderDiscNumProperty;
    private boolean[] canPopinArr;
    private boolean[] canPopoutArr;

    public LogicEngine(int boardWidth, int boardHeight, int gameTarget, GameVariant gameVariant) {
        gameBoard = new Board(boardWidth, boardHeight);
        movesHistory = new MovesHistory(this);
        this.gameTarget = gameTarget;
        this.gameVariant = gameVariant;

        players = new ArrayList<>();
        availableDiscNumbers = new PriorityQueue<>();
        IntStream.rangeClosed(1,MAX_PLAYERS).forEach(availableDiscNumbers::add);

        turnHolderDiscNumProperty = new SimpleIntegerProperty();
        turnHolderProperty = new SimpleObjectProperty<>();
        turnHolderProperty.addListener((observable, oldValue, newValue) ->
                turnHolderDiscNumProperty.setValue(newValue.getDiscNum()));

        canPopinArr = new boolean[boardWidth];
        canPopoutArr = new boolean[boardWidth];

        for(int col = 0; col < boardWidth; col++){
            canPopinArr[col] = true;
            canPopoutArr[col] = false;
        }

        lastDetectedWinnerDiscNum = UNASSIGNED;
    }

    public boolean isPopoutGame (){
        return gameVariant == GameVariant.Popout;
    }

    public int getGameTarget() {
        return gameTarget;
    }

    public String getGameVariantStr() {
        return gameVariant.toString();
    }

    public int getRecordedMovesCount() {
        return movesHistory.getRecordedMovesCount();
    }

    public List<Move> getLastMoves(int numberOfMovesToGet) {
        return movesHistory.getLastMoves(numberOfMovesToGet);

    }

    public Player getTurnHolder() { return turnHolderProperty.getValue(); }

    public List<Player> getPlayers() { return players; }

    public void removePlayer(String playerName) {
        Optional<Player> playerToRemoveOptional = players.stream()
                .filter((player) -> player.getPlayerName().equals(playerName))
                .findFirst();

        if(playerToRemoveOptional.isPresent()){
            Player playerToRemove = playerToRemoveOptional.get();
            availableDiscNumbers.add(playerToRemove.getDiscNum());
            players.remove(playerToRemove);
        }
    }

    public int[][] getBoardMatrix(){
        return gameBoard.getMatrix();
    }

    public void addPlayer(String playerName, boolean isPlayerComputer){
        Integer playerDiscNum = getSmallestAvailableDiscNum();
        Player newPlayer;

        if (isPlayerComputer) {
            newPlayer =  new AIPlayer(playerName, playerDiscNum, gameBoard);

        } else {
            newPlayer = new Player(playerName ,playerDiscNum, gameBoard);
        }

        players.add(newPlayer);

        if(players.size() == 1){
            turnHolderProperty.setValue(newPlayer);
        }
    }

    public void passTurnToNextPlayer(){
        if(players.size() >= 2) {
            int nextTurnHolderIndex = (players.indexOf(turnHolderProperty.get()) + 1) % players.size();
            turnHolderProperty.setValue(players.get(nextTurnHolderIndex));
        }
    }

    public void passTurnToPrevPlayer(){
        if(players.size() >= 2) {
            int prevTurnHolderIndex = (players.indexOf(turnHolderProperty.get()) - 1) % players.size();;
            turnHolderProperty.setValue(players.get(prevTurnHolderIndex));
        }
    }

    public int getBoardWidth(){
        return gameBoard.getBoardWidth();
    }

    public int getBoardHeight(){
        return gameBoard.getBoardHeight();
    }

    public void makeRegularMove(int selectedColumn){
        Move newMove = new RegularMove(selectedColumn, turnHolderDiscNumProperty.getValue());
        turnHolderProperty.getValue().makeMove(newMove);
        movesHistory.addMove(newMove);
        updateColumnProperties(!AFTER_UNDO);
    }

    public void makePopoutMove(int selectedColumn){
        Move newMove = new PopoutMove(selectedColumn, turnHolderDiscNumProperty.get());
        turnHolderProperty.getValue().makeMove(newMove);
        movesHistory.addMove(newMove);
        updateColumnProperties(!AFTER_UNDO);
    }

    public void makeQuitMove(){
        Move newMove = new QuitMove(turnHolderDiscNumProperty.getValue());
        turnHolderProperty.getValue().makeMove(newMove);
        movesHistory.addMove(newMove);
        updateColumnProperties(!AFTER_UNDO);
    }

    public void makeAIMove(){
        Player turnHolder = turnHolderProperty.getValue();
        if(turnHolder instanceof AIPlayer){
            boolean includePopouts = gameVariant == GameVariant.Popout;
            Move newMove = ((AIPlayer)turnHolder).calculateMove(includePopouts);
            turnHolder.makeMove(newMove);
            movesHistory.addMove(newMove);
            updateColumnProperties(!AFTER_UNDO);
        }
    }

    public void makeSkipMove() {
        movesHistory.addMove(new SkipMove(turnHolderDiscNumProperty.getValue()));
        updateColumnProperties(!AFTER_UNDO);
    }

    public boolean isAvailableColumn(int selectedColumn){
        return 0 <= selectedColumn &&
                selectedColumn < getBoardWidth() &&
                getBoardMatrix()[0][selectedColumn] == 0;
    }

    public boolean turnHolderCantPopout() {
        return !gameBoard.floorContainsDiscNum(turnHolderDiscNumProperty.getValue());
    }

    public boolean turnHolderIsAI(){
        return turnHolderProperty.getValue() instanceof AIPlayer;
    }

    public boolean checkForWin(){
        boolean isWin = false;
        Move lastMove = movesHistory.getLastMove();

        if(lastMove instanceof RegularMove){
            RegularMove regularMove = (RegularMove)lastMove;
            isWin = checkForStreakAround(regularMove.getRestingRow(), regularMove.getSelectedColumn());

        } else if(lastMove instanceof PopoutMove) {
            PopoutMove popoutMove = (PopoutMove)lastMove;

            // A Vertical streak cannot be achieved from using a popout move,
            // thus this will only detect horizontal or diagonal streaks.
            isWin = checkForSingleWinnerFromColumn(popoutMove.getSelectedColumn());

        } else if(lastMove instanceof QuitMove) {
            isWin = checkForWinAfterQuitMove((QuitMove) lastMove);
        }

        return isWin;
    }

    public boolean checkForStalemate() {
        Move lastMove = movesHistory.getLastMove();
        boolean isStalemate = false;

        if(lastMove instanceof RegularMove && gameVariant != GameVariant.Popout){
            isStalemate = isBoardFull();

        } else if(lastMove instanceof PopoutMove){
            PopoutMove popoutMove = (PopoutMove)lastMove;

            // A Vertical streak cannot be achieved from using a popout move,
            // thus this will only detect multiple horizontal or diagonal streaks.
            isStalemate = checkForMultipleWinnersFromColumn(popoutMove.getSelectedColumn());

        } else if(lastMove instanceof QuitMove){
            isStalemate = checkForStalemateAfterQuitMove((QuitMove) lastMove);
        }

        return isStalemate;
    }

    public int getDiscNumOfTheWinner() {
        return lastDetectedWinnerDiscNum;
    }

    public String getNameOfTheWinner(){
        String nameOfTheWinner;
        if(!checkForWin()){
            nameOfTheWinner = null;

        } else {
            Optional<Player> winner = players
                    .stream()
                    .filter((player) -> player.getDiscNum() == lastDetectedWinnerDiscNum)
                    .findFirst();

            nameOfTheWinner = winner.isPresent() ? winner.get().getPlayerName() : null;
        }

       return nameOfTheWinner;
    }

    public void reset(){
        gameBoard.reset();

        for(Player player : players){
            player.resetMovesNum();
        }

        lastDetectedWinnerDiscNum = UNASSIGNED;
        updateColumnProperties(!AFTER_UNDO);
        turnHolderProperty.setValue(players.get(0));
        movesHistory.clear();
    }

    public Move getLastMove(){
        return movesHistory.getLastMove();
    }

    public ListIterator<int[][]> getBoardHistoryIterator(){
        return movesHistory.getListIterator();
    }

    public boolean[] getCanPopinArr() {
        return canPopinArr;
    }

    public boolean[] getCanPopoutArr() {
        return canPopoutArr;
    }

    public List<Point> getDiscsPosOfStreaks() {
        int[][] boardMatrix = getBoardMatrix();
        LinkedList<Point> discPosOfStreaks = new LinkedList<>();

        for(int row = 0; row < boardMatrix.length; row++){
            for(int col = 0; col < boardMatrix[0].length; col++){
                if(checkForStreakAround(row, col)){
                    discPosOfStreaks.add(new Point(col, row));
                }
            }
        }

        return discPosOfStreaks;
    }

    public boolean undoLastMove() {
        boolean undoSuccessful = false;

        if(!movesHistory.isEmpty()) {
            Move lastMove = movesHistory.getAndRemoveLastMove();
            lastMove.undoFrom(gameBoard);

            players.stream()
                    .filter(player -> player.getDiscNum() == lastMove.getDiscNum())
                    .findAny()
                    .ifPresent((previousTurnOwner -> {
                        turnHolderProperty.setValue(previousTurnOwner);
                        turnHolderProperty.getValue().decreaseMovesNumByOne();
                    }));

            undoSuccessful = true;
            updateColumnProperties(AFTER_UNDO);
        }

        return undoSuccessful;
    }

    public void clearPlayers(){
        players.clear();
    }

    public class MakeAIMoveTask extends Task<Boolean> {

        Runnable onFinish;
        public MakeAIMoveTask(Runnable onFinish){
            this.onFinish = onFinish;
        }

        @Override
        protected Boolean call() {
            try {
                AIPlayer turnHolder = (AIPlayer)turnHolderProperty.getValue();

                updateProgress(0,1);
                updateMessage("Calculating move...");
                Thread.sleep(1000);

                boolean includePopouts = gameVariant == GameVariant.Popout;
                Move newMove = turnHolder.calculateMove(includePopouts);

                Platform.runLater(()-> turnHolder.makeMove(newMove));
                Platform.runLater(()->updateColumnProperties(!AFTER_UNDO));
                movesHistory.addMove(newMove);

                updateProgress(1,1);
                updateMessage("Done!");
                Thread.sleep(500);

                Platform.runLater(onFinish);

            } catch (InterruptedException ignored) {}

            return true;
        }
    }







    private Integer getSmallestAvailableDiscNum() {
        return availableDiscNumbers.poll();
    }

    private boolean checkForStalemateAfterQuitMove(QuitMove quitMove) {
        boolean isStalemate = allRemainingPlayersAreAI();

        if(!isStalemate){
            isStalemate = checkForMultipleWinnersAfterQuitMove(quitMove);
        }

        return isStalemate;
    }

    private boolean checkForMultipleWinnersAfterQuitMove(QuitMove quitMove) {
        boolean multipleWinnersDetected = false;

        int[] affectedColumns = quitMove.getRemovedDiscsPositions().
                stream()
                .mapToInt(position -> position.x)
                .toArray();

        int firstDetectedWinnerDiscNum = UNASSIGNED;

        for(int affectedColumn : affectedColumns){
            multipleWinnersDetected = checkForMultipleWinnersFromColumn(affectedColumn);

            if(!multipleWinnersDetected){
                boolean singleWinnerWasDetectedFromColumn = checkForSingleWinnerFromColumn(affectedColumn);

                if(singleWinnerWasDetectedFromColumn && firstDetectedWinnerDiscNum == UNASSIGNED){
                    firstDetectedWinnerDiscNum = lastDetectedWinnerDiscNum;

                } else if(singleWinnerWasDetectedFromColumn && firstDetectedWinnerDiscNum != lastDetectedWinnerDiscNum){
                    multipleWinnersDetected = true;
                }
            }

            if(multipleWinnersDetected){
                break;
            }
        }

        return multipleWinnersDetected;
    }

    private boolean checkForMultipleWinnersFromColumn(int selectedColumn) {
        final int[][] boardMatrix = getBoardMatrix();
        boolean firstStreakFound = false;
        int discNumOfFirstStreakFound = 0;

        for(int row = 0; row < getBoardHeight(); row++){
            boolean streakFoundFromCurrentCell = checkForStreakAround(row, selectedColumn);
            int discNumOfCurrentCell = boardMatrix[row][selectedColumn];

            if(streakFoundFromCurrentCell){
                if(!firstStreakFound){
                    firstStreakFound = true;
                    discNumOfFirstStreakFound = boardMatrix[row][selectedColumn];

                } else if (discNumOfFirstStreakFound != discNumOfCurrentCell){

                    // Multiple winners found
                    return true;
                }
            }
        }

        // Reaching here means that no more than 1 winner was found.
        return false;
    }

    private boolean checkForWinAfterQuitMove(QuitMove quitMove) {
        boolean isWin = false;

        if(players.size() == 1){
            // Last player standing should obviously be the winner
            isWin = true;
            lastDetectedWinnerDiscNum = players.get(0).getDiscNum();

        } else if(!checkForStalemateAfterQuitMove(quitMove)){

            int[] affectedColumns = quitMove.getRemovedDiscsPositions()
                    .stream()
                    .mapToInt(point -> point.x)
                    .toArray();

            for(int affectedColumn : affectedColumns){
                isWin = checkForSingleWinnerFromColumn(affectedColumn);
                if(isWin){
                    break;
                }
            }
        }

        return isWin;
    }

    private boolean allRemainingPlayersAreAI() {
        boolean humanPlayerDetected =  players.stream()
                .anyMatch(player -> !(player instanceof AIPlayer));

        return !humanPlayerDetected;
    }

    private boolean checkForSingleWinnerFromColumn(int selectedColumn) {
        final int[][] boardMatrix = getBoardMatrix();
        boolean firstStreakFound = false;
        int discNumOfFirstStreakFound = 0;

        for(int row = 0; row < getBoardHeight(); row++){
            boolean streakFoundFromCurrentCell = checkForStreakAround(row, selectedColumn);
            int discNumOfCurrentCell = boardMatrix[row][selectedColumn];

            if(streakFoundFromCurrentCell){
                if(!firstStreakFound){
                    firstStreakFound = true;
                    discNumOfFirstStreakFound = boardMatrix[row][selectedColumn];

                } else if (discNumOfFirstStreakFound != discNumOfCurrentCell){
                    // Multiple "winners found"
                    return false;
                }
            }
        }

        if(firstStreakFound){
            lastDetectedWinnerDiscNum = discNumOfFirstStreakFound;
        }

        return firstStreakFound;
    }

    private boolean isBoardFull(){
        return gameBoard.isFull();
    }

    private void removeCurrentTurnHolderFromGame(){
        availableDiscNumbers.add(turnHolderDiscNumProperty.getValue());
        Player currentTurnHolder = turnHolderProperty.getValue();
        players.remove(currentTurnHolder);
    }

    private boolean checkForStreakAround(int row, int col) {
        boolean wasStreakFound =
                getBoardMatrix()[row][col] != 0 &&
                (checkForHorizontalStreak(row, col) ||
                checkForVerticalStreak(row, col) ||
                checkForUpperLeftDiagonalStreak(row, col) ||
                checkForUpperRightDiagonalStreak(row, col));

        if(wasStreakFound){
            lastDetectedWinnerDiscNum = getBoardMatrix()[row][col];
        }

        return wasStreakFound;
    }

    @SuppressWarnings("Duplicates")
    private boolean checkForHorizontalStreak(int row, int col) {
        final int[][] boardMatrix = getBoardMatrix();
        final int boardWidth = getBoardWidth();
        final int discNum = boardMatrix[row][col];

        int sameNumberDiscsToTheLeft = 0;
        int sameNumberDiscsToTheRight = 0;
        int colToTheLeft;
        int colToTheRight;

        if(gameVariant != GameVariant.Circular){

            colToTheLeft = col - 1;
            while (0 <= colToTheLeft &&
                    boardMatrix[row][colToTheLeft] == discNum) {

                sameNumberDiscsToTheLeft++;
                colToTheLeft--;
            }

            colToTheRight = col + 1;
            while (colToTheRight < boardWidth &&
                    boardMatrix[row][colToTheRight] == discNum) {
                sameNumberDiscsToTheRight++;
                colToTheRight++;
            }

        } else {

            colToTheLeft = col > 0 ? col - 1 : boardWidth - 1;
            while (colToTheLeft != col &&
                    boardMatrix[row][colToTheLeft] == discNum) {
                colToTheLeft = colToTheLeft > 0 ? colToTheLeft - 1 : boardWidth - 1;
                sameNumberDiscsToTheLeft++;
            }

            colToTheRight = (col + 1) % boardWidth;
            while (colToTheRight != col &&
                    boardMatrix[row][colToTheRight] == discNum) {
                colToTheRight = (colToTheRight + 1) % boardWidth;
                sameNumberDiscsToTheRight++;
            }
        }

        return sameNumberDiscsToTheLeft + sameNumberDiscsToTheRight + 1 >= gameTarget;
    }

    @SuppressWarnings("Duplicates")
    private boolean checkForVerticalStreak(int row, int col) {
        final int[][] boardMatrix = getBoardMatrix();
        final int boardHeight = getBoardHeight();
        final int discNum = boardMatrix[row][col];

        int sameNumberDiscsBelow = 0;
        int sameNumberDiscsAbove = 0;
        int rowBelow;
        int rowAbove;

        if(gameVariant != GameVariant.Circular) {
            rowAbove = row - 1;
            while (rowAbove >= 0 && boardMatrix[rowAbove][col] == discNum) {
                sameNumberDiscsAbove++;
                rowAbove--;
            }

            rowBelow = row + 1;
            while (rowBelow < boardHeight &&
                    boardMatrix[rowBelow][col] == discNum) {
                sameNumberDiscsBelow++;
                rowBelow++;
            }

        } else {
            rowAbove = row > 0 ? row - 1 : boardHeight - 1;
            while (rowAbove != row &&
                    boardMatrix[rowAbove][col] == discNum) {
                rowAbove = rowAbove > 0 ? rowAbove - 1 : boardHeight - 1;
                sameNumberDiscsAbove++;
            }

            rowBelow = (row + 1) % boardHeight;
            while (rowBelow != row &&
                    boardMatrix[rowBelow][col] == discNum) {
                rowBelow = (rowBelow + 1) % boardHeight;
                sameNumberDiscsBelow++;
            }
        }

        return sameNumberDiscsAbove + sameNumberDiscsBelow + 1 >= gameTarget;
    }

    @SuppressWarnings("Duplicates")
    private boolean checkForUpperLeftDiagonalStreak(int row, int col) {
        final int[][] boardMatrix = getBoardMatrix();
        final int discNum = boardMatrix[row][col];

        int discsBehind = 0, currentRow = row - 1, currentCol = col - 1;

        while (currentRow >= 0 && currentCol >= 0 &&
                boardMatrix[currentRow][currentCol] == discNum) {
            currentRow--;
            currentCol--;
            discsBehind++;
        }

        int discsAfter = 0;
        currentRow = row + 1;
        currentCol = col + 1;

        while (currentRow < getBoardHeight() &&
                currentCol < getBoardWidth() &&
                boardMatrix[currentRow][currentCol] == discNum) {
            currentRow++;
            currentCol++;
            discsAfter++;
        }

        return discsAfter + discsBehind + 1 >= gameTarget;
    }

    @SuppressWarnings("Duplicates")
    private boolean checkForUpperRightDiagonalStreak(int row, int col){
        final int[][] boardMatrix = getBoardMatrix();
        final int discNum = boardMatrix[row][col];

        int discsBehind = 0, currentRow = row + 1, currentCol = col - 1;

        while (currentRow < getBoardHeight() && currentCol >= 0 &&
                boardMatrix[currentRow][currentCol] == discNum) {
            currentRow++;
            currentCol--;
            discsBehind++;
        }

        int discsAfter = 0;
        currentRow = row - 1;
        currentCol = col + 1;

        while (currentRow >= 0 && currentCol < getBoardWidth() &&
                boardMatrix[currentRow][currentCol] == discNum) {
            currentRow--;
            currentCol++;
            discsAfter++;
        }

        return discsAfter + discsBehind + 1 >= gameTarget;
    }

    private void updateColumnProperties(boolean afterUndo) {
        final int[][] boardMatrix = getBoardMatrix();
        for(int col = 0; col < getBoardWidth(); col++){
            canPopinArr[col] = boardMatrix[0][col] == 0;

            if(afterUndo){
                canPopoutArr[col] =
                        boardMatrix[getBoardHeight() - 1][col] == turnHolderDiscNumProperty.getValue();

            } else {
                canPopoutArr[col] =
                        boardMatrix[getBoardHeight() - 1][col] == getNextTurnHolderDiscNum();
            }
        }
    }

    private int getNextTurnHolderDiscNum(){
        int indexOfNextTurnHolder = (players.indexOf(turnHolderProperty.getValue()) + 1) % players.size();
        Player nextTurnHolder = players.get(indexOfNextTurnHolder);
        return nextTurnHolder.getDiscNum();
    }
}
