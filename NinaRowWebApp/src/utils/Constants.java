package utils;

import Engine.LogicEngine;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String USER_STATUS_ATTRIBUTE = "userStatus";
    public static final String LOGGED_IN_USERS_ATTRIBUTE = "loggedInUsers";
    public static final String GAME_ROOMS_MAP_ATTRIBUTE ="gameRoomsMap";

    public static final int OK_CODE = 200;
    public static final int UNSUPPORTED_MEDIA_ERROR_CODE = 415;
    public static final int INTERNAL_SERVER_ERROR_CODE = 500;

    public static Map<String, LogicEngine.GameVariant> variantToGameTypeMap = new HashMap<String, LogicEngine.GameVariant>()
    {{
        put("Regular", LogicEngine.GameVariant.Regular);
        put("Popout", LogicEngine.GameVariant.Popout);
        put("Circular", LogicEngine.GameVariant.Circular);
    }};
}

