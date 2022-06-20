package servlets;

import Engine.LogicEngine;
import utils.Constants;
import GameSettings.*;
import OnlineClasses.GameRoom;
import utils.ServletUtils;
import OnlineClasses.UserStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class UploadXmlServlet extends HttpServlet {

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        InputStream xmlFileInputStream = getUploadedXmlInputStream(request);

        try {
            GameSettings gameSettings = GameSettings.loadFromInputStream(xmlFileInputStream);
            LogicEngine logicEngine = new LogicEngine(
                    gameSettings.getBoardWidth(),
                    gameSettings.getBoardHeight(),
                    gameSettings.getGameTarget(),
                    Constants.variantToGameTypeMap.get(gameSettings.getVariant()));

            String gameTitle = gameSettings.getGameTitle();
            Map<String, GameRoom> gameTitleToGameRoomMap = ServletUtils.getGameRoomsMap(getServletContext());

            synchronized (getServletContext()){
                if(gameTitleToGameRoomMap.keySet().contains(gameTitle)) {
                    sendErrorMessage(
                            "A game by this title has been uploaded already!",
                            Constants.UNSUPPORTED_MEDIA_ERROR_CODE,
                            response);

                } else {
                    UserStatus userStatus = (UserStatus) request.getSession().getAttribute(Constants.USER_STATUS_ATTRIBUTE);
                    GameRoom newGameRoom = new GameRoom(logicEngine, gameTitle, userStatus.getLoggedInName(), gameSettings.getTotalPlayers());
                    gameTitleToGameRoomMap.put(gameTitle, newGameRoom);
                }
            }


        } catch(InvalidSettingsException e) {
            String errorMessage = buildInvalidSettingsErrorMessage(e);
            sendErrorMessage(errorMessage, Constants.UNSUPPORTED_MEDIA_ERROR_CODE, response);

        } catch(JAXBException e) {
            sendErrorMessage(
                    "Internal Server error: JAXB Exception occurred.",
                    Constants.INTERNAL_SERVER_ERROR_CODE,
                    response);

        } catch(NullPointerException e) {
            sendErrorMessage(
                    "The selected XML file doesn't match the expected schema!",
                    Constants.UNSUPPORTED_MEDIA_ERROR_CODE,
                    response);

        } catch(Exception e){
            sendErrorMessage(
                    "Internal Server error: Unknown Exception occurred.",
                    Constants.INTERNAL_SERVER_ERROR_CODE,
                    response);

        } finally {
            xmlFileInputStream.close();
        }
    }

    private InputStream getUploadedXmlInputStream(HttpServletRequest request)
            throws ServletException, IOException {

        Part filePart = request.getPart("xmlFile");
        return filePart.getInputStream();
    }

    private String buildInvalidSettingsErrorMessage(InvalidSettingsException e) {
        StringBuilder errorMessage = new StringBuilder();

            errorMessage.append("The selected game settings XML file has the following logical issues:");
            errorMessage.append(System.lineSeparator());

            for(InvalidSettingsException.ErrorType error : e.getErrors()){
                switch(error) {
                    case InvalidRows:
                        errorMessage.append("- The number of rows is not within the requested 5 to 50 range.");
                        break;
                    case InvalidCols:
                        errorMessage.append("- The number of columns is not within the requested 6 to 30 range.");

                        break;
                    case InvalidTarget:
                        errorMessage.append("- The game's streak target is out of the accepted range: ");
                        errorMessage.append(System.lineSeparator());
                        errorMessage.append(GameSettings.minTargetNum());
                        errorMessage.append(" <= target < number of rows, number of columns");
                        break;
                    case InvalidVariant:
                        errorMessage.append("- Unknown game variant. ");
                        break;
                    case InvalidTotalPlayersNum:
                        errorMessage.append("- Total number of players is not within the requested 2 to 6 range.");
                        break;

                    case EmptyOnlineGameTitle:
                        errorMessage.append("- This game has an empty game title: a valid game must have a title.");
                }
                errorMessage.append(System.lineSeparator());
            }

        return errorMessage.toString();
    }

    private void sendErrorMessage(String msg, int errorCode ,HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("text/html");
        response.setStatus(Constants.UNSUPPORTED_MEDIA_ERROR_CODE);
        try(PrintWriter out = response.getWriter()){
            out.print(msg);
            out.flush();
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
