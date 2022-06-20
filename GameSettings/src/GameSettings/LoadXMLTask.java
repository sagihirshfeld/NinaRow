package GameSettings;

import XMLScheme.GameDescriptor;
import XMLScheme.Players;
import javafx.application.Platform;
import javafx.concurrent.Task;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;

public class LoadXMLTask extends Task<GameSettings> {

    private File selectedFile;
    private Runnable onFinish;

    public LoadXMLTask(File selectedFile,  Runnable onFinish){
        this.selectedFile = selectedFile;
        this.onFinish = onFinish;
    }

    @Override
    protected GameSettings call() throws Exception {
        GameSettings gameSettings = null;

        try {
            updateProgress(0, 3);
            updateMessage("Initializing JAXB...");
            Thread.sleep(1000);

            JAXBContext jaxbContext = JAXBContext.newInstance(GameDescriptor.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            updateProgress(1, 3);
            updateMessage("Unmarshalling XML...");
            Thread.sleep(1500);
            GameDescriptor gameDescriptor = (GameDescriptor) jaxbUnmarshaller.unmarshal(selectedFile);

            updateProgress(2, 3);
            updateMessage("Validating XML...");
            Thread.sleep(1500);

            ArrayList<InvalidSettingsException.ErrorType> errorsInSettings = new ArrayList<>();
            int rowNum = gameDescriptor.getGame().getBoard().getRows();
            int colNum = gameDescriptor.getGame().getBoard().getColumns().intValue();
            int target = gameDescriptor.getGame().getTarget().intValue();
            Players players = gameDescriptor.getPlayers();

            if (rowNum < 5 || 50 < rowNum) {
                errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidRows);

            }
            if (colNum < 6 || 30 < colNum) {
                errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidCols);

            }
            if (rowNum <= target || colNum <= target || target < GameSettings.minTargetNum()) {
                errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidTarget);

            } if(!GameSettings.knownGameVariants.contains(gameDescriptor.getGame().getVariant())){
                errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidVariant);

            }
            if (players.getPlayer().size() < 2 || 6 < players.getPlayer().size()) {
                errorsInSettings.add(InvalidSettingsException.ErrorType.InvalidPlayersNum);

            }
            if (GameSettings.hasIdenticalIDs(players)) {
                errorsInSettings.add(InvalidSettingsException.ErrorType.IdenticalPlayersIDs);

            }
            if (errorsInSettings.size() > 0) {
                throw new InvalidSettingsException(errorsInSettings);
            }

            gameSettings = new GameSettings();
            gameSettings.getDataFromGameDescriptor(gameDescriptor);

            updateProgress(3, 3);
            updateMessage("Done!");
            Thread.sleep(500);

        } catch(InterruptedException ignored) {
        } finally  {
            Platform.runLater(onFinish);
        }

        return gameSettings;
    }
}
