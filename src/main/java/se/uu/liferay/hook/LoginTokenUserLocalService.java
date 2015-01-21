package se.uu.liferay.hook;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceWrapper;
import com.liferay.portal.service.UserLocalService;
import se.uu.liferay.util.LoginToken;

public class LoginTokenUserLocalService extends UserLocalServiceWrapper {

	public LoginTokenUserLocalService(UserLocalService userLocalService) {
		super(userLocalService);
	}

    @Override
    public int authenticateByScreenName(long companyId, String screenName, String password, Map<String,String[]> headerMap, Map<String,String[]> parameterMap, Map<String,Object> resultsMap) throws com.liferay.portal.kernel.exception.PortalException, com.liferay.portal.kernel.exception.SystemException {
        User u = UserLocalServiceUtil.getUserByScreenName(PortalUtil.getDefaultCompanyId(), screenName);
        if (isPassword(u, password)) {
            resultsMap.put("userId", u.getUserId());
            return Authenticator.SUCCESS;
        }
        return super.authenticateByScreenName(companyId, screenName, password, headerMap, parameterMap, resultsMap);
    }

    @Override
    public int authenticateByUserId(long companyId, long userId, String password, Map<String,String[]> headerMap, Map<String,String[]> parameterMap, Map<String,Object> resultsMap) throws com.liferay.portal.kernel.exception.PortalException, com.liferay.portal.kernel.exception.SystemException {
        User u = UserLocalServiceUtil.getUserById(PortalUtil.getDefaultCompanyId(), userId);
        if (isPassword(u, password)) {
            resultsMap.put("userId", u.getUserId());
            return Authenticator.SUCCESS;
        }
        return super.authenticateByUserId(companyId, userId, password,headerMap, parameterMap, resultsMap);
    }

    @Override
    public int authenticateByEmailAddress(long companyId, String emailAddress, String password,Map<String, String[]> headerMap, Map<String, String[]> parameterMap, Map<String, Object> resultsMap) throws PortalException, SystemException {
        User u = UserLocalServiceUtil.getUserByEmailAddress(PortalUtil.getDefaultCompanyId(), emailAddress);
        if (isPassword(u, password)) {
            resultsMap.put("userId", u.getUserId());
            return Authenticator.SUCCESS;
        }
        return super.authenticateByEmailAddress(companyId, emailAddress, password, headerMap, parameterMap, resultsMap);
    }

    private boolean isPassword(User u, String password) {
        String encryptedPassword = DigesterUtil.digest(password);
        List<LoginToken> tokens = LoginToken.getLoginTokens(u);
        for (LoginToken token: tokens) {
            if (token.getEncryptedToken().equals(encryptedPassword)) {
                return true;
            }
        }
        return false;
    }

    /*
    //To handle webdav then use one of the following methods to authenticate the user properly, using tokens?
    public long authenticateForDigest(long companyId, String username, String realm, String nonce, String method, String uri, String response) throws PortalException, SystemException {
        return super.authenticateForDigest(companyId, username, realm, nonce, method, uri, response);
    }
    public long authenticateForBasic(long companyId, String authType, String login, String password) throws PortalException, SystemException {
        return super.authenticateForBasic(companyId, authType, login, password);
    }
    */
}
