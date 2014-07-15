/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.console.client.administration.role.ui;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasName;
import org.jboss.as.console.client.administration.role.model.Principal;
import org.jboss.as.console.client.administration.role.model.Role;
import org.jboss.as.console.client.administration.role.model.RoleAssignment;
import org.jboss.as.console.client.administration.role.model.RoleComparator;
import org.jboss.as.console.client.core.EnumLabelLookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public final class UIHelper {

    final static Templates TEMPLATES = GWT.create(Templates.class);

    private UIHelper() {
    }

    public static <T> String csv(final Collection<T> values) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<T> iterator = values.iterator(); iterator.hasNext(); ) {
            T value = iterator.next();
            if (value instanceof HasName) {
                builder.append(((HasName) value).getName());
            } else {
                builder.append(String.valueOf(value));
            }
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public static Map<Role.Type, String> enumFormItemsForScopedRoleTyp() {
        Map<Role.Type, String> scopes = new LinkedHashMap<Role.Type, String>();
        scopes.put(Role.Type.HOST, EnumLabelLookup.labelFor("ScopeType", Role.Type.HOST));
        scopes.put(Role.Type.SERVER_GROUP, EnumLabelLookup.labelFor("ScopeType", Role.Type.SERVER_GROUP));
        return scopes;
    }

    public static Cell<RoleAssignment> newPrincipalCell() {
        return new PrincipalCell();
    }

    public static Cell<RoleAssignment> newRolesCell() {
        return new RolesCell();
    }

    public static SafeHtml principalAsSafeHtml(final Principal principal, final String realm) {
        if (realm != null && realm.length() != 0) {
            return TEMPLATES.principalAtRealm(principal.getName(), realm);
        } else {
            return TEMPLATES.principal(principal.getName());
        }
    }

    private static SafeHtml roleAsSafeHtml(final Role role, boolean include) {
        if (role.isStandard()) {
            return include ? TEMPLATES.role(role.getName()) : TEMPLATES.exclude(role.getName());
        } else {
            String scopes = csv(role.getScope());
            return include ?
                    TEMPLATES.scopedRole(role.getName(), role.getBaseRole().getId(), scopes) :
                    TEMPLATES.scopedExclude(role.getName(), role.getBaseRole().getId(), scopes);
        }
    }

    interface Templates extends SafeHtmlTemplates {

        @Template("<span>{0}</span>")
        SafeHtml principal(String principal);

        @Template("<span title=\"'{0}' (at) realm '{1}'\">{0}<span class=\"admin-principal-realm\">@{1}</span></span>")
        SafeHtml principalAtRealm(String principal, String realm);

        @Template("<span>{0}</span>")
        SafeHtml role(String role);

        @Template(
                "<span title=\"based on '{1}' scoped to '{2}'\">{0} <span class=\"admin-role-scope\">[{2}]</span></span>")
        SafeHtml scopedRole(String role, String baseRole, String scope);

        @Template("<span class=\"admin-role-exclude\">&dash;{0}</span>")
        SafeHtml exclude(String role);

        @Template(
                "<span class=\"admin-role-exclude\" title=\"based on '{1}' scoped to '{2}'\">&dash;{0} <span class=\"admin-role-scope\">[{2}]</span></span>")
        SafeHtml scopedExclude(String role, String baseRole, String scope);

    }

    static public class PrincipalCell extends AbstractCell<RoleAssignment> {

        @Override
        public void render(final Context context, final RoleAssignment roleAssignment, final SafeHtmlBuilder builder) {
            builder.append(principalAsSafeHtml(roleAssignment.getPrincipal(), roleAssignment.getRealm()));
        }
    }

    static public class RolesCell extends AbstractCell<RoleAssignment> {

        @Override
        public void render(final Context context, final RoleAssignment roleAssignment, final SafeHtmlBuilder builder) {
            boolean hasExcludes = !roleAssignment.getExcludes().isEmpty();
            List<Role> roles = new ArrayList<Role>(roleAssignment.getRoles());
            RoleComparator comperator = new RoleComparator();
            Collections.sort(roles, comperator);
            for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext(); ) {
                Role role = iterator.next();
                builder.append(roleAsSafeHtml(role, true));
                if (iterator.hasNext() || hasExcludes) {
                    builder.append(SafeHtmlUtils.fromString(", "));
                }
            }
            if (hasExcludes) {
                List<Role> excludes = new ArrayList<Role>(roleAssignment.getExcludes());
                Collections.sort(excludes, comperator);
                for (Iterator<Role> iterator = excludes.iterator(); iterator.hasNext(); ) {
                    Role exclude = iterator.next();
                    builder.append(roleAsSafeHtml(exclude, false));
                    if (iterator.hasNext()) {
                        builder.append(SafeHtmlUtils.fromString(", "));
                    }
                }
            }
        }
    }
}
