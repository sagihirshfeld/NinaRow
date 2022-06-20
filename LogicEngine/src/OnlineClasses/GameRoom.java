package OnlineClasses;

import Engine.LogicEngine;

public class GameRoom {
    // "transient" prevents serialization
    private transient LogicEngine logicEngine;

    private String title;
    private String variant;
    private int target;
    private int boardWidth;
    private int boardHeight;
    private String nameOfTheCreator;
    private boolean gameInProgress;
    private int currentPlayerCount;
    private int requiredPlayerCount;

    public GameRoom(LogicEngine logicEngine,String title ,String nameOfTheCreator, int requiredPlayerCount){
        this.logicEngine = logicEngine;
        this.variant = logicEngine.getGameVariantStr();
        this.target = logicEngine.getGameTarget();
        this.title = title;
        this.boardWidth = logicEngine.getBoardWidth();
        this.boardHeight = logicEngine.getBoardHeight();
        this.nameOfTheCreator = nameOfTheCreator;
        this.requiredPlayerCount = requiredPlayerCount;
        currentPlayerCount = 0;
        gameInProgress = false;
    }

    public LogicEngine getLogicEngine() {
        return logicEngine;
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public int getCurrentPlayerCount() {
        return currentPlayerCount;
    }

    public void increasePlayerCount(){
        currentPlayerCount++;
    }

    public void decreasePlayerCount(){
        currentPlayerCount--;
    }

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    public int getRequiredPlayerCount() {
        return requiredPlayerCount;
    }
}
