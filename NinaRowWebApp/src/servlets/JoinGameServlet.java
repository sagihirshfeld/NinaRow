package servlets;

import Engine.LogicEngine;
import OnlineClasses.GameRoom;
import utils.ServletUtils;
import utils.SessionUtils;
import OnlineClasses.UserStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JoinGameServlet extends HttpServlet {

    public static final String GAME_TITLE_PARAMETER = "gameTitleParameter";

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String gameTitle = request.getParameter(GAME_TITLE_PARAMETER);

        UserStatus userStatus = SessionUtils.getUserStatus(request);
        userStatus.setCurrentGameTitle(gameTitle);

        GameRoom gameRoom =  ServletUtils.getGameRoom(getServletContext(), request.getSession());
        LogicEngine logicEngine = gameRoom.getLogicEngine();

        synchronized (gameRoom) {
            logicEngine.addPlayer(userStatus.getLoggedInName(), userStatus.isComputer());
        }

        synchronized (getServletContext()){
            gameRoom.increasePlayerCount();
            gameRoom.setGameInProgress(gameRoom.getCurrentPlayerCount() == gameRoom.getRequiredPlayerCount());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
