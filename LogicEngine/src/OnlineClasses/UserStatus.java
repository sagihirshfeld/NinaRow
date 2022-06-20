package OnlineClasses;

public class UserStatus {
    private String loggedInName;
    private boolean isConnected;
    private boolean isPlaying;
    private boolean isComputer;
    private String currentGameTitle;

    public UserStatus() {
        loggedInName = null;
        this.isConnected = false;
        this.isPlaying = false;
        this.isComputer = false;
        this.currentGameTitle = null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public String getCurrentGameTitle() {
        return currentGameTitle;
    }

    public void setCurrentGameTitle(String currentGameTitle) {
        this.currentGameTitle = currentGameTitle;
        isPlaying = currentGameTitle != null;
    }

    public boolean isComputer() {
        return isComputer;
    }

    public void setComputer(boolean computer) {
        isComputer = computer;
    }

    public String getLoggedInName() {
        return loggedInName;
    }

    public void setLoggedInName(String loggedInName) {
        this.loggedInName = loggedInName;
    }
}
