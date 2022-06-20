package servlets;

import Engine.LogicEngine;
import Players.AIPlayer;
import Players.Player;
import OnlineClasses.GameRoom;
import utils.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PlayersListServlet extends HttpServlet {

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        GameRoom gameRoom = ServletUtils.getGameRoom(getServletContext(),request.getSession());
        LogicEngine logicEngine = gameRoom.getLogicEngine();
        List<Player> rawPlayersList = logicEngine.getPlayers();
        List<PlayerData> playersListForSending = new LinkedList<>();

        rawPlayersList.forEach((player) -> {
            boolean isTurnHolder = player.getPlayerName().equals(logicEngine.getTurnHolder().getPlayerName());
            PlayerData playerData = new PlayerData (
                    player.getPlayerName(),
                    player instanceof AIPlayer,
                    player.getDiscNum(),
                    player.movesCountProperty.get(),
                    isTurnHolder);

            playersListForSending.add(playerData);
        });

        ServletUtils.sendAsJson(playersListForSending, response);
    }

    // Projection to send as json
    private class PlayerData {
        String name;
        boolean isComputer;
        int discNum;
        int turnsPlayed;
        boolean isTurnHolder;

        public PlayerData(String name, boolean isComputer, int discNum, int turnsPlayed,boolean isTurnHolder) {
            this.name = name;
            this.isComputer = isComputer;
            this.discNum = discNum;
            this.turnsPlayed = turnsPlayed;
            this.isTurnHolder = isTurnHolder;
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
