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

package org.jboss.as.console.client.shared.general.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.as.console.client.widgets.forms.ApplicationMetaData;
import org.jboss.as.console.client.widgets.forms.EntityAdapter;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.Property;
import org.jboss.dmr.client.dispatch.AsyncCommand;
import org.jboss.dmr.client.dispatch.DispatchAsync;
import org.jboss.dmr.client.dispatch.impl.DMRAction;
import org.jboss.dmr.client.dispatch.impl.DMRResponse;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.dmr.client.ModelDescriptionConstants.*;

/**
 * @author Heiko Braun
 * @date 5/18/11
 */
public class LoadInterfacesCmd implements AsyncCommand<List<Interface>>{

    private EntityAdapter<Interface> entityAdapter;
    private DispatchAsync dispatcher;
    private ModelNode address;

    public LoadInterfacesCmd(
            DispatchAsync dispatcher,
            ModelNode address, ApplicationMetaData metaData) {
        this.dispatcher = dispatcher;
        this.address = address;
        this.entityAdapter = new EntityAdapter<Interface>(Interface.class, metaData);
    }

    @Override
    public void execute(final AsyncCallback<List<Interface>> callback) {
        ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_CHILDREN_RESOURCES_OPERATION);
        operation.get(ADDRESS).set(address);
        operation.get(CHILD_TYPE).set("interface");
        operation.get(RECURSIVE).set(Boolean.TRUE);

        dispatcher.execute(new DMRAction(operation), new AsyncCallback<DMRResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(DMRResponse result) {
                ModelNode response = result.get();
                List<Property> payload = response.get(RESULT).asPropertyList();

                List<Interface> interfaces = new ArrayList<Interface>(payload.size());
                for(Property property : payload)
                {
                    ModelNode item = property.getValue();
                    Interface intf = entityAdapter.fromDMR(item);

                    if(intf.isAnyAddress())
                        intf.setAddressWildcard(Interface.ANY_ADDRESS);

                    interfaces.add(intf);

                }

                callback.onSuccess(interfaces);
            }
        });
    }

}
