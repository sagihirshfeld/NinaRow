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

public class LeaveGameServlet extends HttpServlet {

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserStatus userStatus = SessionUtils.getUserStatus(request);
        String userName = userStatus.getLoggedInName();

        GameRoom gameRoom = ServletUtils.getGameRoom(getServletContext(), request.getSession());
        LogicEngine logicEngine = gameRoom.getLogicEngine();

        synchronized (gameRoom){
            // This turn-passing is relevant only for the cases in
            // a player leaves the game which are leaving before the game begins
            if(!gameRoom.isGameInProgress() && logicEngine.getTurnHolder().getPlayerName().equals(userName)){
                logicEngine.passTurnToNextPlayer();
            }

            logicEngine.removePlayer(userName);
        }

        synchronized (getServletContext()){
            gameRoom.decreasePlayerCount();

            if(gameRoom.getCurrentPlayerCount() == 0){
                gameRoom.setGameInProgress(false);
            }
        }

        userStatus.setCurrentGameTitle(null);
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
