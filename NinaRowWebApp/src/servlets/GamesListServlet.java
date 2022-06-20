package servlets;

import OnlineClasses.GameRoom;
import utils.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class GamesListServlet extends HttpServlet {

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        Map<String, GameRoom> gamesMap = ServletUtils.getGameRoomsMap(getServletContext());
        List<GameRoom> gameRoomsList;

        // The fetching of this info is synchronized with the code parts that change the data of the game rooms.
        synchronized (getServletContext()){
            gameRoomsList = new LinkedList<>(gamesMap.values());
        }

        ServletUtils.sendAsJson(gameRoomsList, response);
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
