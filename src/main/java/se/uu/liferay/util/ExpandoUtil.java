package se.uu.liferay.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.NoSuchTableException;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.util.ExpandoBridgeFactoryUtil;

/**
 * Utility class for creating expando values programmatically, and of course checking if the values already exists
 */
public class ExpandoUtil {
    private static Log log = LogFactoryUtil.getLog(ExpandoUtil.class);
    private static RolePermission guestViewPermission = null;
    private static RolePermission userViewUpdatePermission = null;

    private static Role guestRole = null;
    private static Role userRole = null;

    public static final String EXPANDO_LOGIN_TOKENS = "login_tokens";
    /**
     * @see #createIfNotExists(String, String, se.uu.its.liferay.common.ExpandoUtil.RolePermission...)
     */
    public static void createIfNotExists(final Class clazz, final String fieldName, final RolePermission... rolePermissions) {
        createIfNotExists(clazz.getName(), fieldName, rolePermissions);
    }

    /**
     * Creates an expando value if it doesn't already exist.
     * Supply the expando column name and the permissions associated, probably at least GUEST/VIEW.
     *
     * @param fieldName The expando column name
     * @param rolePermissions
     */
    public static void createIfNotExists(final String className, final String fieldName, final RolePermission... rolePermissions) {
        long companyId = PortalUtil.getDefaultCompanyId();

        try {
            ExpandoTable table = null;

            try {
                table = ExpandoTableLocalServiceUtil.getDefaultTable(companyId, className);
            } catch ( NoSuchTableException e ) {
                // Create default table. That's what Brian Boitano'd do
                log.info("Creating default expando table for " + className);
                table = ExpandoTableLocalServiceUtil.addDefaultTable(companyId, className);
            }

            ExpandoColumn col = null;
            if (!ExpandoBridgeFactoryUtil.getExpandoBridge(companyId, className).hasAttribute(fieldName)) {
                // Slightly different terminology: "attribute" in ExpandoBridgeImpl.java means "column" everywhere else
                col = ExpandoColumnLocalServiceUtil.addColumn(table.getTableId(), fieldName, ExpandoColumnConstants.STRING);
                log.info("Created expando column for class " + className + ": " + fieldName);
            } else {
                log.info("Expando column (" + fieldName +  ") already configured");
                col = ExpandoColumnLocalServiceUtil.getColumn(table.getTableId(), fieldName);
            }
            if (col != null) {
                log.info("Verifying permissions for column " + col.getColumnId() + "(" + fieldName + ")");
                for (RolePermission rolePermission : rolePermissions) {
                    ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, ExpandoColumn.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(col.getColumnId()), rolePermission.role.getRoleId(), rolePermission.permissions);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /* Some commonly used permission variants */

    public static synchronized RolePermission getGuestViewPermission() {
        if (guestViewPermission == null) {
            guestViewPermission = new RolePermission(getGuestRole(), ActionKeys.VIEW);
        }
        return guestViewPermission;
    }

    public static synchronized RolePermission getUserViewUpdatePermission() {
        if (userViewUpdatePermission == null) {
            userViewUpdatePermission = new RolePermission(getUserRole(), ActionKeys.VIEW, ActionKeys.UPDATE);
        }
        return userViewUpdatePermission;
    }
    
    public static synchronized RolePermission getUserViewPermission() {
        if (userViewUpdatePermission == null) {
            userViewUpdatePermission = new RolePermission(getUserRole(), ActionKeys.VIEW);
        }
        return userViewUpdatePermission;
    }
    /* --End permission variants */

    private static Role getGuestRole() {
        if (guestRole == null) {
            try {
                guestRole = RoleLocalServiceUtil.getRole(PortalUtil.getDefaultCompanyId(), RoleConstants.GUEST);
            } catch (Exception e) {
                log.error("Cannot locate the GUEST role, this error is fatal to expando functionality", e);
            }
        }
        return guestRole;
    }

    private static Role getUserRole() {
        if (userRole == null) {
            try {
                userRole = RoleLocalServiceUtil.getRole(PortalUtil.getDefaultCompanyId(), RoleConstants.USER);
            } catch (Exception e) {
                log.error("Cannot locate the GUEST role, this error is fatal to expando functionality", e);
            }
        }
        return userRole;
    }

    public static class RolePermission {
        public RolePermission(Role role, String... permissions) {
            this.role = role;
            this.permissions = permissions;

        }
        private Role role;
        private String[] permissions;
    }
}
