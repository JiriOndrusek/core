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
package org.jboss.as.console.client.shared.subsys.logging;

import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.shared.subsys.logging.LoggingLevelProducer.LogLevelConsumer;
import org.jboss.as.console.client.shared.subsys.logging.model.AsyncHandler;
import org.jboss.as.console.client.shared.viewframework.*;
import org.jboss.as.console.client.widgets.forms.ApplicationMetaData;
import org.jboss.as.console.client.widgets.forms.FormMetaData;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.FormAdapter;
import org.jboss.dmr.client.dispatch.DispatchAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Subview for Async Handlers.
 * 
 * @author Stan Silvert ssilvert@redhat.com (C) 2011 Red Hat Inc.
 */
public class AsyncHandlerSubview extends AbstractHandlerSubview<AsyncHandler>
        implements FrameworkView,
        LogLevelConsumer,
        HandlerProducer,
        HandlerConsumer {

    private EmbeddedHandlerView handlerView;

    public AsyncHandlerSubview(ApplicationMetaData applicationMetaData, 
                                 DispatchAsync dispatcher, 
                                 HandlerListManager handlerListManager) {
        super(AsyncHandler.class, applicationMetaData, dispatcher, handlerListManager);
    }

    @Override
    public String getManagementModelType() {
        return "async-handler";
    }

    @Override
    protected String provideDescription() {
        return Console.CONSTANTS.subsys_logging_asyncHandlers_desc();
    }

    @Override
    public void handlersUpdated(List<String> handlerList) {
        // HAL-313: async handlers cannot add other async handlers
        List<String> withoutAsyncHandler = new ArrayList<>();
        List<NamedEntity> ownHandlers = getHandlers();
        for (String handler : handlerList) {
            boolean nesting = false;
            for (NamedEntity ownHandler : ownHandlers) {
                if (ownHandler.getName().equals(handler)) {
                    nesting = true;
                    break;
                }
            }
            if (!nesting) {
                withoutAsyncHandler.add(handler);
            }
        }
        handlerView.getListView().setAvailableChoices(withoutAsyncHandler);
    }
    
    @Override
    protected FormAdapter<AsyncHandler> makeAddEntityForm() {
        Form<AsyncHandler> form = new Form(type);
        form.setNumColumns(1);
        form.setFields(formMetaData.findAttribute("name").getFormItemForAdd(), 
                       levelItemForAdd,
                       formMetaData.findAttribute("queueLength").getFormItemForAdd(),
                       formMetaData.findAttribute("overflowAction").getFormItemForAdd(this));
        return form;
    }
    
    @Override
    protected String getEntityDisplayName() {
        return Console.CONSTANTS.subsys_logging_asyncHandlers();
    }

    @Override
    protected List<SingleEntityView<AsyncHandler>> provideAdditionalTabs(
            Class<?> beanType,
            FormMetaData formMetaData,
            FrameworkPresenter presenter) {

        List<SingleEntityView<AsyncHandler>> additionalTabs =
                new ArrayList<SingleEntityView<AsyncHandler>>(1);

        this.handlerView = new EmbeddedHandlerView(new FrameworkPresenter() {
            @Override
            public EntityToDmrBridge getEntityBridge() {
                return AsyncHandlerSubview.this.getEntityBridge();
            }
        });
        additionalTabs.add(handlerView);

        return additionalTabs;
    }
}
