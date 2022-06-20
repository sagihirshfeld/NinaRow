package utils;

import OnlineClasses.UserStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtils {
    public static UserStatus getUserStatus(HttpServletRequest request) {
        HttpSession currentSession =  request.getSession();

        UserStatus userStatus;
        Object userStatusObj = currentSession.getAttribute(Constants.USER_STATUS_ATTRIBUTE);

        // Create and add on the first time
        if(userStatusObj == null){
            userStatus = new UserStatus();
            currentSession.setAttribute(Constants.USER_STATUS_ATTRIBUTE, userStatus);

        } else {
            userStatus = (UserStatus)userStatusObj;
        }

        return userStatus;
    }
}

