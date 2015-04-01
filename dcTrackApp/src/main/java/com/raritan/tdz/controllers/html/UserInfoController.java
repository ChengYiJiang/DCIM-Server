package com.raritan.tdz.controllers.html;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.session.DCTrackSessionManager;
import com.raritan.tdz.session.DCTrackSessionManagerInterface;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

@Controller
@RequestMapping("/user")
//@RequestMapping("/v2/session/user/") //this is purposed path
public class UserInfoController extends BaseController  {
	
	private final Logger log = Logger.getLogger( UserInfoController.class );
	
	@Autowired
	private DCTrackSessionManagerInterface dcTrackSessionMgr;
	
	/**
	 *   Get unit from user setting
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.22/api/user/unit
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/user/unit
	 *  1 : US, 2 : SI
	 */
	@RequestMapping(value="/unit", method=RequestMethod.GET)
	public @ResponseBody Map getUnit(
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		
		Map hm = new HashMap();
		try {
			
			UserInfo user = RESTAPIUserSessionContext.getUser();
			hm.put( "unit", user.getUnits() );
		} catch( Exception e ){
			hm.put( "unit", "1"  );
			log.error("",e);
		}	
		return hm;
	}
	
	/**
	 *  Get default location from user setting
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/user/default_location
	 *  or local env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" --cookie DCTRACK_INIT_SESSION=IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS "http://admin:raritan@localhost:8080/dcTrackApp/api/user/default_location"
	 */
	@RequestMapping(value="/default_location", method=RequestMethod.GET)
	public @ResponseBody Map getDefaultLocation (
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		
		Map hm = new HashMap();
		try {
			
			UserInfo user = RESTAPIUserSessionContext.getUser();
			hm.put( "locationId", user.getDefaultLocationId() );
			hm.put( "locationCode", user.getDefaultLocationCode() );
						
		} catch( Exception e ){
			log.error("",e);
		}	
		return hm;
	}	
	
	/**
	 *  Get access level
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/user/accessLevel
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/user/accessLevel
	 *  1 : US, 2 : SI
	 */
	@RequestMapping(value="/accessLevel", method=RequestMethod.GET)
	 @ResponseBody
	public Map getAccessLevel(
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		
		Map hm = new HashMap();
		hm.put( "isAdmin", false );
		hm.put( "isManager", false );
		hm.put( "isMember", false );
		hm.put( "isViewer", false );
		hm.put( "isLogin", false );

		try {
			UserInfo user = RESTAPIUserSessionContext.getUser();
			log.info("user="+user);

			if (user != null) {

                // UserInfo != null means that the user is logged in.
                // hm.put( "isLogin", true);
                // Session is alive means that the user is logged in.
                hm.put("isLogin", ! RESTAPIUserSessionContext.sessionTimedOut());

                hm.put("userId", user.getUserId());
				hm.put( "userName", user.getUserName() );
			
				if ( user.isAdmin() ) {
					hm.put( "isAdmin", true );
					return hm;
				}
			
				if ( user.isManager() ) {
					hm.put( "isManager", true );
					return hm;
				}
			
				if ( user.isMember() ) {
					hm.put( "isMember", true );
					return hm;
				}
			
				if ( user.isViewer() ) {
					hm.put( "isViewer", true );
					return hm;
				}
			}
			
		} catch( Exception e ) {
			log.error("",e);
		}	
		
		return hm;
	}

    /**
     * Check session timeout.
     */
    @RequestMapping(value = "/checkSession", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> checkSession(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("timeout", true);

        try {
            // Check cookie and session id.
            boolean noSessionId = true;
            Cookie cookies[] = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("DCTRACK_INIT_SESSION")) {
                        noSessionId = false;
                        break;
                    }
                }

                if (noSessionId) {
                    log.info("Can not find DCTRACK_INIT_SESSION in cookie.");
                    jsonMap.put("error", "Can not find DCTRACK_INIT_SESSION in cookie.");

                } else {
                    UserInfo user = RESTAPIUserSessionContext.getUser();

                    if (user != null) {
                        // Check session timeout.
                        jsonMap.put("timeout", RESTAPIUserSessionContext.sessionTimedOut());

                        // debug info
                        long currentTime = System.currentTimeMillis();
                       // long lastAccessedTime = RESTAPIUserSessionContext.getLastAccessedTime();
                        long lastAccessedTime = 0;
                        if (dcTrackSessionMgr.getAttribute(user.getSessionId(), DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED) != null){
                         Timestamp lastAccessedTimestamp = (Timestamp) dcTrackSessionMgr.getAttribute(user.getSessionId(), DCTrackSessionManager.DCTRACK_USER_LAST_TIME_ACCESSED);
                         lastAccessedTime = (Long) lastAccessedTimestamp.getTime();
                        }
                        jsonMap.put("sessionId", user.getSessionId());
                        jsonMap.put("sessionTimeout", user.getSessionTimeout());
                        jsonMap.put("sessionTimeoutMinute", (long) (user.getSessionTimeout() / 60000));
                        jsonMap.put("currentTime", currentTime);
                        jsonMap.put("lastAccessedTime", lastAccessedTime);
                        jsonMap.put("idleTime", currentTime - lastAccessedTime);
                        jsonMap.put("idleTimeMinute", (long) ((currentTime - lastAccessedTime) / 60000));

                        jsonMap.put("flexLastAccessedTime", lastAccessedTime);
                    } else {
                        log.info("Can not find UserInfo.");
                        jsonMap.put("error", "Can not find UserInfo.");
                    }
                }

            } else {
                log.info("No cookie.");
                jsonMap.put("error", "No cookie.");
            }

        } catch (Exception e) {
            log.error("",e);
            jsonMap.put("error", e.toString());
        }

        return jsonMap;
    }

    /**
     * Clear session attributes.
     */
    @RequestMapping(value = "/clearSession", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> clearSession(HttpServletRequest request, HttpServletResponse response) throws Throwable {

        Map<String, Object> jsonMap = new HashMap<String, Object>();

        try {
            RESTAPIUserSessionContext.clearSession();

        } catch (Exception e) {
            log.error("",e);
            jsonMap.put("error", e.toString());
        }

        return jsonMap;
    }
}