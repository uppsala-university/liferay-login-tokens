package se.uu.liferay.portlet;

import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.util.bridges.mvc.MVCPortlet;
import se.uu.liferay.util.LoginToken;

import javax.portlet.*;
import java.io.IOException;
import java.util.List;

public class LoginTokenPortlet extends MVCPortlet {
    private String viewJSP;

    @Override
    public void init() throws PortletException {
        super.init();
        viewJSP = this.getPortletConfig().getInitParameter("view-jsp");
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        if (themeDisplay.isSignedIn()) {
            request.setAttribute("tokens", LoginToken.getLoginTokens(themeDisplay.getUser()));
            this.include(viewJSP, request, response);
        } else {
            this.include("/html/loginportlet/empty.jsp", request, response);
        }
    }

    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
        String resourceId = resourceRequest.getResourceID();

        if (Validator.isNull(resourceId)) {
            //log.warn( "serveResource() called with empty resourceId!" );
            return;
        }
        ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);

        if ("GENERATE_NEW".equals(resourceId)) {
            String desc = resourceRequest.getParameter("description");
            LoginToken token = LoginToken.generateToken(desc, themeDisplay.getUser());
            String msg = "{\"token\":\"" + token.getTokenPlaintext() + "\", \"description\":\"" + token.getDescription() + "\"}";
            resourceResponse.getWriter().write(msg);
            resourceResponse.flushBuffer();
        } else if ("REVOKE".equals(resourceId)) {
            String created = resourceRequest.getParameter("created");
            List<LoginToken> allTokens = LoginToken.getLoginTokens(themeDisplay.getUser());
            for (LoginToken t: allTokens) {
                if (t.getCreated() != null && t.getCreated().equals(created)) {
                    LoginToken.revokeToken(t.getEncryptedToken(), themeDisplay.getUser());
                }
            }
        }
    }
}
