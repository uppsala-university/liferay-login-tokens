package se.uu.liferay.util;

import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.util.PwdGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class LoginToken {
    private static final String TOKEN_DELIMITER = "¢";
    private static final String TOKEN_FIELD_DELIMITER = "|";

    private String description = "N/A";
    private String encryptedToken;
    /**
     * Could contain the token in plain text. Generally this will only be populated at generation time. Only.
     */
    private transient String tokenPlaintext;
    private String created;

    private void fromString(String concat) {
        //Should always be 3 fields since we know how toString method constructs its results.
        StringTokenizer tokenizer = new StringTokenizer(concat, TOKEN_FIELD_DELIMITER);
        description = tokenizer.nextToken();
        encryptedToken = tokenizer.nextToken();
        created = tokenizer.nextToken();
    }

    public String toString() {
        return description + TOKEN_FIELD_DELIMITER + encryptedToken + TOKEN_FIELD_DELIMITER + created;
    }

    public String getDescription() {
        return description;
    }
    public String getEncryptedToken() {
        return encryptedToken;
    }
    public String getCreated() {
        return created;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setEncryptedToken(String token) {
        this.encryptedToken = token;
    }
    public void setCreated(String created) {
        this.created = created;
    }
    public String getTokenPlaintext() {
        return tokenPlaintext;
    }
    public void setTokenPlaintext(String tokenPlaintext) {
        this.tokenPlaintext = tokenPlaintext;
    }

    public static List<LoginToken> getLoginTokens(User user) {
        List<LoginToken> results = new ArrayList<>();
        verifyExpandoValues();
        String concatTokens = null;
        try {
            concatTokens = (String) user.getExpandoBridge().getAttribute(ExpandoUtil.EXPANDO_LOGIN_TOKENS, false);
        } catch(Exception e) {
            //Most probably the expando value didn't exist previously, creating it.
            //verifyExpandoValues();
        }
        if (Validator.isNotNull(concatTokens)) {
            StringTokenizer tokenizer = new StringTokenizer(concatTokens, TOKEN_DELIMITER);

            while (tokenizer.hasMoreTokens()) {
                String loginTokenAsString = tokenizer.nextToken();
                LoginToken loginToken = new LoginToken();
                loginToken.fromString(loginTokenAsString);
                results.add(loginToken);
            }
        }

        return results;
    }

    public static LoginToken generateToken(String description, User user) {
        LoginToken loginToken = new LoginToken();
        loginToken.tokenPlaintext = PwdGenerator.getPassword(20);
        loginToken.encryptedToken = DigesterUtil.digest(loginToken.tokenPlaintext);
        loginToken.description = Validator.isNull(description) ? "N/A" : description;
        loginToken.created = new Date().toString();

        String allTokens = (String) user.getExpandoBridge().getAttribute(ExpandoUtil.EXPANDO_LOGIN_TOKENS, false);
        if (Validator.isNull(allTokens)) {
            allTokens = "";
        }
        allTokens += TOKEN_DELIMITER + loginToken.toString();
        user.getExpandoBridge().setAttribute(ExpandoUtil.EXPANDO_LOGIN_TOKENS, allTokens, false);
        return loginToken;
    }

    public static void revokeToken(String token, User user) {
        List<LoginToken> allTokens = getLoginTokens(user);
        String concatTokens = "";
        for (LoginToken tokens: allTokens) {
            if (tokens.getEncryptedToken().equals(token)) {
                continue;
            } else {
                concatTokens += TOKEN_DELIMITER + tokens.toString();
            }
        }
        user.getExpandoBridge().setAttribute(ExpandoUtil.EXPANDO_LOGIN_TOKENS, concatTokens, false);
    }

    private static void verifyExpandoValues() {
        ExpandoUtil.createIfNotExists(User.class, ExpandoUtil.EXPANDO_LOGIN_TOKENS, ExpandoUtil.getGuestViewPermission(), ExpandoUtil.getUserViewUpdatePermission());
    }
}
