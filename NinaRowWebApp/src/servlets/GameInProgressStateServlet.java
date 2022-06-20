package servlets;

import Engine.LogicEngine;
import Moves.Move;
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

public class GameInProgressStateServlet extends HttpServlet {

    private static final String TURNS_UPDATED_AT_USER_PARAMETER = "turnsUpdatedAtUserParameter";

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        try {
            GameRoom gameRoom = ServletUtils.getGameRoom(getServletContext(), request.getSession());
            LogicEngine logicEngine = gameRoom.getLogicEngine();

            int turnsUpdatedAtUser = Integer.parseInt(request.getParameter(TURNS_UPDATED_AT_USER_PARAMETER));
            GameStateUpdate gameStateUpdate = new GameStateUpdate();

            List<Move> rawMovesToUpdate = null;

            // In sync with the part that registers a new move at the engine at MakeMoveServlet
            synchronized (gameRoom){
                if(turnsUpdatedAtUser >= logicEngine.getRecordedMovesCount()){
                    gameStateUpdate.newTurnsAvailable = false;

                } else {
                    gameStateUpdate.newTurnsAvailable = true;

                    // Get the turn holder, whether the game is over and the name of the winner (which can be null)
                    gameStateUpdate.turnHolder = logicEngine.getTurnHolder();
                    gameStateUpdate.isGameOver = logicEngine.checkForStalemate() || logicEngine.checkForWin();
                    gameStateUpdate.nameOfTheWinner = logicEngine.getNameOfTheWinner();

                    // Get the canPopin/Popout arrays - for button locking/unlocking
                    gameStateUpdate.canPopinArr = logicEngine.getCanPopinArr();
                    gameStateUpdate.canPopoutArr = logicEngine.getCanPopoutArr();

                    // Get the last moves
                    rawMovesToUpdate = logicEngine.getLastMoves(logicEngine.getRecordedMovesCount() - turnsUpdatedAtUser);
                }
            }

            if(gameStateUpdate.newTurnsAvailable && rawMovesToUpdate != null){
                List<MoveWrapper> wrappedMovesToUpdate = new LinkedList<>();
                rawMovesToUpdate.forEach((rawMove) -> wrappedMovesToUpdate.add(new MoveWrapper(rawMove)));
                gameStateUpdate.movesToUpdate = wrappedMovesToUpdate;

                ServletUtils.sendAsJson(gameStateUpdate, response);
            }


        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // Projection to send as json
    private class GameStateUpdate {

        private boolean newTurnsAvailable;
        private List<MoveWrapper> movesToUpdate;
        private Player turnHolder;
        private boolean isGameOver;
        private String nameOfTheWinner;
        private boolean[] canPopinArr;
        private boolean[] canPopoutArr;
    }

    // Projection to send as json
    private class MoveWrapper {
        private String nestedMoveType;
        private Move nestedMove;

        public MoveWrapper(Move moveToWrap){
            this.nestedMove = moveToWrap;
            this.nestedMoveType = moveToWrap.getClass().getSimpleName();
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
