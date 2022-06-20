package servlets;

import Engine.LogicEngine;
import OnlineClasses.GameRoom;
import utils.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MakeMoveServlet extends HttpServlet {

    private static final String MOVETYPE_PARAMETER = "moveType";
    private static final String SELECTED_COLUMN_PARAMETER = "selectedCol";
    private static final int UNASSIGNED = -1;


    private void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String moveTypeParameter = request.getParameter(MOVETYPE_PARAMETER);
        int selectedColParameter = UNASSIGNED;

        try {
            selectedColParameter = Integer.parseInt(request.getParameter(SELECTED_COLUMN_PARAMETER));

        } catch(NumberFormatException e){
            // do nothing - this only means moveType == "QuitMove"
        }

        GameRoom gameRoom = ServletUtils.getGameRoom(getServletContext(), request.getSession());
        LogicEngine logicEngine = gameRoom.getLogicEngine();

        synchronized (gameRoom) {
            switch(moveTypeParameter){
                case "Popin":
                    logicEngine.makeRegularMove(selectedColParameter);
                    break;
                case "Popout":
                    logicEngine.makePopoutMove(selectedColParameter);
                    break;
                case "ComputerMove":
                    logicEngine.makeAIMove();
                    break;
                case "Quit":
                    logicEngine.makeQuitMove();
                    break;
                case "Skip":
                    logicEngine.makeSkipMove();
                    break;
            }

            logicEngine.passTurnToNextPlayer();
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
