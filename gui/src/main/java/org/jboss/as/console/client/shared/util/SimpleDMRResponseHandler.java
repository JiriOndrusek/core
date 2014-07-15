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
package org.jboss.as.console.client.shared.util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.as.console.client.Console;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.dispatch.impl.DMRResponse;

/**
 * @author David Bosschaert
 */
public class SimpleDMRResponseHandler implements AsyncCallback<DMRResponse> {
    private final String operation;
    private final String entityName;
    private final String id;
    private final Command callback;

    public SimpleDMRResponseHandler(String operationName, String entityName, String id, Command callback) {
        this.operation = operationName;
        this.entityName = entityName;
        this.id = id;
        this.callback = callback;
    }

    @Override
    public void onFailure(Throwable caught) {
        Log.error("Failed to modify security resource", caught);
        Console.error(Console.MESSAGES.modificationFailed(entityName) , caught.getMessage());
    }

    @Override
    public void onSuccess(DMRResponse result) {
        ModelNode response = result.get();

        if (response.isFailure())
        {
            Console.error(
                    Console.MESSAGES.modificationFailed(entityName+ ": " + id) ,
                    response.getFailureDescription()
            );
        }
        else
        {
            Console.info(Console.CONSTANTS.common_label_success() + " " + operation + " " + entityName + ": " + id);
        }

        Scheduler.get().scheduleDeferred(callback);
    }
}
