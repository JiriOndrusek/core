package org.jboss.as.console.client.search;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.BootstrapContext;
import org.jboss.as.console.client.plugins.AccessControlRegistry;
import org.jboss.as.console.mbui.behaviour.CoreGUIContext;
import org.jboss.as.console.mbui.model.mapping.AddressMapping;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.dispatch.DispatchAsync;
import org.jboss.dmr.client.dispatch.impl.DMRAction;
import org.jboss.dmr.client.dispatch.impl.DMRResponse;
import org.jboss.gwt.flow.client.Async;
import org.jboss.gwt.flow.client.Control;
import org.jboss.gwt.flow.client.Function;
import org.jboss.gwt.flow.client.Outcome;
import org.useware.kernel.gui.behaviour.FilteringStatementContext;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.dmr.client.ModelDescriptionConstants.*;

/**
 * Creates search indexes.
 *
 * @author Heiko Braun
 * @date 16/01/14
 */
public class Harvest {

    final AccessControlRegistry accessControlMetaData;
    final DispatchAsync dispatcher;
    final CoreGUIContext statementContext;
    private final BootstrapContext bootstrap;
    private final FilteringStatementContext filteringStatementContext;

    public interface Handler {
        void onStart();
        void onHarvest(String token, String address);
        void onFinish();
        void onError(Throwable t);
    }

    @Inject
    public Harvest(
            AccessControlRegistry accessControlMetaData,
            DispatchAsync dispatcher,
            CoreGUIContext statementContext, final BootstrapContext bootstrap) {

        this.accessControlMetaData = accessControlMetaData;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.bootstrap = bootstrap;

        this.filteringStatementContext = new FilteringStatementContext(
                statementContext,
                new FilteringStatementContext.Filter() {
                    @Override
                    public String filter(String key) {

                        if ("selected.entity".equals(key)) {
                            return "*";
                        } else if ("addressable.group".equals(key)) {
                            return bootstrap.getAddressableGroups().isEmpty() ? "*" : bootstrap.getAddressableGroups().iterator().next();
                        } else if ("addressable.host".equals(key)) {
                            return bootstrap.getAddressableHosts().isEmpty() ? "*" : bootstrap.getAddressableHosts().iterator().next();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public String[] filterTuple(String key) {
                        return null;
                    }
                }
        );
    }

    public void run(final Handler handler) {
        handler.onStart();


        Set<Function> functions = new HashSet<Function>();

        for(final String token : accessControlMetaData.getTokens())
        {

            Set<String> resources = accessControlMetaData.getResources(token);
            for(final String resource : resources)
            {
                // TODO
                if(resource.startsWith("opt:")) continue;

                final ModelNode op = AddressMapping.fromString(resource).asResource(filteringStatementContext);
                op.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION);

                Function f = new Function() {
                    @Override
                    public void execute(final Control control) {

                        dispatcher.execute(new DMRAction(op), new AsyncCallback<DMRResponse>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                handler.onError(caught);
                                control.proceed();
                            }

                            @Override
                            public void onSuccess(DMRResponse result) {
                                ModelNode response = result.get();
                                if(response.isFailure())
                                {
                                    handler.onError(new RuntimeException(response.getFailureDescription()));
                                    control.proceed();
                                }
                                else
                                {

                                    if(response.hasDefined(RESULT)){
                                        ModelNode desc = response.get(RESULT);
                                        if(desc.hasDefined(DESCRIPTION))
                                        {
                                            String text = desc.get(DESCRIPTION).asString();
                                            // create index
                                            Index.get().add(token, text);
                                            handler.onHarvest(token, op.get(ADDRESS).asString());
                                            control.proceed();
                                        }
                                    }

                                }


                            }
                        });
                    }
                } ;

                functions.add(f);

            }
        }


        new Async().waterfall("", new Outcome() {
            @Override
            public void onFailure(Object context) {
                Console.error("Harvest failed");
            }

            @Override
            public void onSuccess(Object context) {
                handler.onFinish();
            }
        }, functions.toArray(new Function[]{}));

    }
}