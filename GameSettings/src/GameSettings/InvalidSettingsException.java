package GameSettings;

import java.util.List;

public class InvalidSettingsException extends  Exception {

    public enum ErrorType {
        InvalidRows,
        InvalidCols,
        InvalidTarget,
        InvalidVariant,
        InvalidPlayersNum,
        IdenticalPlayersIDs,
        InvalidTotalPlayersNum,
        EmptyOnlineGameTitle,
    }

    private List<ErrorType> errors;

    public InvalidSettingsException(List<ErrorType> errors) {
        this.errors = errors;
    }

    public List<ErrorType> getErrors() {
        return errors;
    }
}
