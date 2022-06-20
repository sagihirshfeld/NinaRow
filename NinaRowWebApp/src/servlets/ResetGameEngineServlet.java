package servlets;

import OnlineClasses.GameRoom;
import utils.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResetGameEngineServlet extends HttpServlet {

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        GameRoom gameRoom = ServletUtils.getGameRoom(getServletContext(), request.getSession());
        gameRoom.getLogicEngine().reset();

        // A game engine reset is done by the one of the players right before he/she
        // returns to the lobby - which is done by the ajax success-callback from the
        // ajax call to this servlet.
        //
        // If a servlet doesn't respond - the ajax success-callback is invoked right
        // after the sending - which might (and have) create a race problem in which
        // the gameRoom cannot be found at this servlet in the player's session since
        // it was removed when he left to the lobby.
        ServletUtils.sendAsJson("OK to leave now!", response);
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
