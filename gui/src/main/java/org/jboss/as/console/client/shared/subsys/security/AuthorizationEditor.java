/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.as.console.client.shared.subsys.security;

import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.shared.subsys.security.model.AuthorizationPolicyProvider;
import org.jboss.as.console.client.shared.subsys.security.wizard.NewAuthPolicyModuleWizard;

import java.util.LinkedList;
import java.util.List;

/**
 * @author David Bosschaert
 */
public class AuthorizationEditor extends AuthEditor<AuthorizationPolicyProvider> {
    AuthorizationEditor(SecurityDomainsPresenter presenter) {
        super(presenter, AuthorizationPolicyProvider.class);
        setDescription(Console.CONSTANTS.subsys_security_authorization_desc());

    }

    @Override
    String getEntityName() {
        return Console.CONSTANTS.subsys_security_authorization();
    }

    @Override
    String getStackElementName() {
        return Console.CONSTANTS.subsys_security_authorizationPolicy();
    }

    @Override
    String getStackName() {
        return Console.CONSTANTS.subsys_security_policies();
    }

    @Override
    void saveData() {
        presenter.saveAuthorization(domainName, attributesProvider.getList(), resourceExists);
    }

    @Override
    Wizard<AuthorizationPolicyProvider> getWizard() {
        List<String> flagValues = new LinkedList<String>();
        flagValues.add("required");
        flagValues.add("requisite");
        flagValues.add("sufficient");
        flagValues.add("optional");

        final List<String> codes  = new LinkedList<>();
        codes.add("DenyAll");
        codes.add("PermitAll");
        codes.add("Delegating");
        codes.add("Web");
        codes.add("JACC");
        codes.add("XACML");

        // should really wait until flagValues are set.
        return new NewAuthPolicyModuleWizard<AuthorizationPolicyProvider>(this, entityClass, flagValues,
                presenter, SecurityDomainsPresenter.AUTHORIZATION_IDENTIFIER, "policy-modules") {
            {
                setCodes(codes);
            }
        };

    }

    @Override
    void removeData() {
        presenter.removeAuthorization(domainName, attributesProvider.getList());

    }

}
