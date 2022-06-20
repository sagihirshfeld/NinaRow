package GameSettings;

import XMLScheme.DynamicPlayers;
import XMLScheme.GameDescriptor;
import XMLScheme.Player;
import XMLScheme.Players;
import org.omg.DynamicAny.DynAny;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class GameSettings implements Serializable {

    static List<String> knownGameVariants = Arrays.asList("Regular","Popout","Circular");

    private int boardWidth;
    private int boardHeight;
    private int gameTarget;
    private String variant;
    private Players players;

    private int totalPlayers;
    private String gameTitle;

    public static int minTargetNum() {
        return 2;
    }

    GameSettings() {}

    public static GameSettings loadFromFile(File file) throws InvalidSettingsException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(GameDescriptor.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        GameDescriptor gameDescriptor = (GameDescriptor) jaxbUnmarshaller.unmarshal(file);

        return gameSettingsFromGameDescriptor(gameDescriptor);
    }

    public static GameSettings loadFromInputStream(InputStream inputStream) throws InvalidSettingsException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(GameDescriptor.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        GameDescriptor gameDescriptor = (GameDescriptor) jaxbUnmarshaller.unmarshal(inputStream);

        return gameSettingsFromGameDescriptor(gameDescriptor);
    }

    private static GameSettings gameSettingsFromGameDescriptor(GameDescriptor gameDescriptor) throws InvalidSettingsException, JAXBException{
        ArrayList<InvalidSettingsException.ErrorType> errorsInSettings = new ArrayList<>();
        int rowNum = gameDescriptor.getGame().getBoard().getRows();
        int colNum = gameDescriptor.getGame().getBoard().getColumns().intValue();
        int target = gameDescriptor.getGame().getTarget().intValue();
        Players players = gameDescriptor.getPlayers();

        DynamicPlayers dynamicPlayers = gameDescriptor.getDynamicPlayers();
        boolean isOnlineGame = dynamicPlayers != null;
        int totalPlayers = 0;
        String gameTitle = null;

        if(dynamicPlayers != null){
            totalPlayers = dynamicPlayers.getTotalPlayers();
            gameTitle = dynamicPlayers.getGameTitle();
        }

        if (rowNum < 5 || 50 < rowNum) {
            errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidRows);

        }
        if (colNum < 6 || 30 < colNum) {
            errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidCols);

        }
        if (rowNum <= target || colNum <= target || target < minTargetNum()) {
            errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidTarget);

        } if(!knownGameVariants.contains(gameDescriptor.getGame().getVariant())){
            errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidVariant);
        }

        if (players != null && (players.getPlayer().size() < 2 || 6 < players.getPlayer().size())) {
            errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidPlayersNum);
        }

        if (players != null && hasIdenticalIDs(players)) {
            errorsInSettings.add(InvalidSettingsException.ErrorType.IdenticalPlayersIDs);
        }

        if(isOnlineGame && (totalPlayers < 2 || 6 < totalPlayers)){
            errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidTotalPlayersNum);
        }

        if(isOnlineGame && (gameTitle == null || gameTitle.trim().isEmpty())){
            errorsInSettings.add(InvalidSettingsException.ErrorType.EmptyOnlineGameTitle);
        }

        if (errorsInSettings.size() > 0) {
            throw new InvalidSettingsException(errorsInSettings);

        } else {
            GameSettings gameSettings = new GameSettings();
            gameSettings.getDataFromGameDescriptor(gameDescriptor);

            return gameSettings;
        }
    }

    static boolean hasIdenticalIDs(Players players) {
        Set<Short> seenIDs = new HashSet<>();
        for(Player currentPlayer : players.getPlayer()){
            if(seenIDs.contains(currentPlayer.getId())){
                return true;

            } else{
                seenIDs.add(currentPlayer.getId());
            }
        }

        return false;
    }

    void getDataFromGameDescriptor(GameDescriptor gameDescriptor) {
        boardWidth = gameDescriptor.getGame().getBoard().getColumns().intValue();
        boardHeight = gameDescriptor.getGame().getBoard().getRows();
        gameTarget = gameDescriptor.getGame().getTarget().intValue();
        variant = gameDescriptor.getGame().getVariant();
        players = gameDescriptor.getPlayers();

        DynamicPlayers dynamicPlayers = gameDescriptor.getDynamicPlayers();
        totalPlayers = dynamicPlayers != null ? dynamicPlayers.getTotalPlayers() : 0;
        gameTitle = dynamicPlayers != null ? dynamicPlayers.getGameTitle() : "";
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public int getGameTarget() {
        return gameTarget;
    }

    public String getVariant() {
        return variant;
    }

    public Players getPlayers() {
        return players;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public String getGameTitle() {
        return gameTitle;
    }
}






