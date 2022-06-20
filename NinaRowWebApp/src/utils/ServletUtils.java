package utils;

import OnlineClasses.GameRoom;
import OnlineClasses.UserStatus;
import com.google.gson.Gson;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServletUtils {

    private static final Object gameRoomsMapLock = new Object();
    private static final Object loggedInUsersLock = new Object();

    public static void sendAsJson(Object objectToSend, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("application/json");

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(objectToSend);

        try(PrintWriter out = response.getWriter()){
            out.print(jsonResponse);
            out.flush();
        }
    }

    public static  Set<String> getLoggedInUsers(ServletContext servletContext) {

        synchronized (loggedInUsersLock) {
            if(servletContext.getAttribute(Constants.LOGGED_IN_USERS_ATTRIBUTE) == null) {
                servletContext.setAttribute(Constants.LOGGED_IN_USERS_ATTRIBUTE, new HashSet<String>());
            }
        }

        return  (HashSet<String>)servletContext.getAttribute(Constants.LOGGED_IN_USERS_ATTRIBUTE);
    }

    public static Map<String, GameRoom> getGameRoomsMap(ServletContext servletContext) {

        synchronized (gameRoomsMapLock) {
            if(servletContext.getAttribute(Constants.GAME_ROOMS_MAP_ATTRIBUTE) == null){
                servletContext.setAttribute(Constants.GAME_ROOMS_MAP_ATTRIBUTE, new HashMap<String, GameRoom>());
            }
        }

        return (HashMap<String, GameRoom>) servletContext.getAttribute(Constants.GAME_ROOMS_MAP_ATTRIBUTE);
    }

    public static GameRoom getGameRoom(ServletContext servletContext, HttpSession session) {
        GameRoom gameRoom;

        UserStatus userStatus = (UserStatus) session.getAttribute(Constants.USER_STATUS_ATTRIBUTE);
        String gameTitle = userStatus.getCurrentGameTitle();
        gameRoom = getGameRoomsMap(servletContext).get(gameTitle);

        return gameRoom;
    }
}
