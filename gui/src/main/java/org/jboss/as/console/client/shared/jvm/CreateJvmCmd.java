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

package org.jboss.as.console.client.shared.jvm;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.shared.BeanFactory;
import org.jboss.as.console.client.shared.util.AddressableModelCmd;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.dispatch.AsyncCommand;
import org.jboss.dmr.client.dispatch.DispatchAsync;
import org.jboss.dmr.client.dispatch.impl.DMRAction;
import org.jboss.dmr.client.dispatch.impl.DMRResponse;

import java.util.List;

import static org.jboss.dmr.client.ModelDescriptionConstants.*;

/**
 * @author Heiko Braun
 * @date 5/18/11
 */
public class CreateJvmCmd extends AddressableModelCmd implements AsyncCommand<Boolean>{

    public CreateJvmCmd(DispatchAsync dispatcher, BeanFactory factory, ModelNode address) {
        super(dispatcher, factory, address);
    }

    @Override
    public void execute(AsyncCallback<Boolean> callback) {
        throw new RuntimeException();
    }

    public void execute(final Jvm jvm, final AsyncCallback<Boolean> callback) {
        ModelNode operation = new ModelNode();
        operation.get(OP).set(ADD);
        operation.get(ADDRESS).set(address);

        //ModelNode jvmModel = new ModelNode();
        if (jvm.getPermgen() != null && jvm.getPermgen().trim().length() != 0) {
            operation.get("heap-size").set(jvm.getHeapSize());
        }
        if (jvm.getPermgen() != null && jvm.getPermgen().trim().length() != 0) {
            operation.get("max-heap-size").set(jvm.getMaxHeapSize());
        }
        operation.get("debug-enabled").set(jvm.isDebugEnabled());
        if (jvm.getPermgen() != null && jvm.getPermgen().trim().length() != 0) {
            operation.get("permgen-size").set(jvm.getPermgen());
        }
        if (jvm.getMaxPermgen() != null && jvm.getMaxPermgen().trim().length() != 0) {
            operation.get("max-permgen-size").set(jvm.getMaxPermgen());
        }
        List<String> options = jvm.getOptions();
        if (options != null && !options.isEmpty()) {
            for (String option : options) {
                operation.get("jvm-options").add(option);
            }
        }

        //operation.get("jvm").set(jvm.getName(), jvmModel);

        dispatcher.execute(new DMRAction(operation), new AsyncCallback<DMRResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(DMRResponse result) {
                ModelNode response = result.get();
                boolean success = response.get(OUTCOME).asString().equals(SUCCESS);

                if(success)
                    Console.info(Console.MESSAGES.added("JVM Config"));
                else
                    Console.error(Console.MESSAGES.addingFailed("JVM Config"), response.getFailureDescription());

                callback.onSuccess(success);
            }
        });

    }

}
