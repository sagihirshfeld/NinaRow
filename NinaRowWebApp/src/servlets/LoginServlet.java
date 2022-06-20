package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import utils.ServletUtils;
import utils.SessionUtils;
import OnlineClasses.UserStatus;

public class LoginServlet extends HttpServlet {

    private static final String USERNAME_PARAMETER = "username";
    private static final String IS_COMPUTER_PARAMETER ="isComputer";

    private static final Object addUserLock = new Object();

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String userName = request.getParameter(USERNAME_PARAMETER);
        boolean isComputer = getIsComputerParameter(request);
        Set<String> loggedInUsers = ServletUtils.getLoggedInUsers(getServletContext());

        boolean nameAlreadyExists = true;

        if(!loggedInUsers.contains(userName)) {

            synchronized (addUserLock){
                loggedInUsers.add(userName);
            }

            nameAlreadyExists = false;
        }

        if(!nameAlreadyExists) {
            loggedInUsers.add(userName);
            UserStatus userStatus = SessionUtils.getUserStatus(request);
            userStatus.setLoggedInName(userName);
            userStatus.setConnected(true);
            userStatus.setComputer(isComputer);
        }

        ServletUtils.sendAsJson(nameAlreadyExists, response);
    }

    private boolean getIsComputerParameter(HttpServletRequest request) {
        // A checkbox's value is "on" by default if checked, or null if unchecked
        String isComputerCheckBoxValue = request.getParameter(IS_COMPUTER_PARAMETER);
        return isComputerCheckBoxValue != null;
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
