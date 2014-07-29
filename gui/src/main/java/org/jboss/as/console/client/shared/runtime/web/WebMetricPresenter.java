package org.jboss.as.console.client.shared.runtime.web;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.Proxy;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.NameTokens;
import org.jboss.as.console.client.domain.model.LoggingCallback;
import org.jboss.as.console.client.shared.BeanFactory;
import org.jboss.as.console.client.shared.runtime.Metric;
import org.jboss.as.console.client.shared.runtime.RuntimeBaseAddress;
import org.jboss.as.console.client.shared.subsys.RevealStrategy;
import org.jboss.as.console.client.shared.subsys.web.LoadConnectorCmd;
import org.jboss.as.console.client.shared.subsys.web.model.HttpConnector;
import org.jboss.as.console.client.widgets.forms.ApplicationMetaData;
import org.jboss.as.console.spi.AccessControl;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.dispatch.DispatchAsync;
import org.jboss.dmr.client.dispatch.impl.DMRAction;
import org.jboss.dmr.client.dispatch.impl.DMRResponse;
import org.jboss.gwt.circuit.PropagatesChange;

import java.util.Collections;
import java.util.List;

import static org.jboss.dmr.client.ModelDescriptionConstants.*;

/**
 * @author Heiko Braun
 * @date 12/9/11
 */
public class WebMetricPresenter extends Presenter<WebMetricPresenter.MyView, WebMetricPresenter.MyProxy>
{

    private DispatchAsync dispatcher;
    private RevealStrategy revealStrategy;
    private HttpConnector selectedConnector;
    private BeanFactory factory;
    private final LoadConnectorCmd cmd;

    @ProxyCodeSplit
    @NameToken(NameTokens.WebMetricPresenter)
    @AccessControl(resources = {
            "/{selected.host}/{selected.server}/subsystem=web"
    }, recursive = false)
    public interface MyProxy extends Proxy<WebMetricPresenter>, Place {
    }

    public interface MyView extends View {
        void setPresenter(WebMetricPresenter presenter);
        void clearSamples();
        void setConnectorMetric(Metric metric);
        void setConnectors(List<HttpConnector> list);
    }

    @Inject
    public WebMetricPresenter(
            EventBus eventBus, MyView view, MyProxy proxy,
            DispatchAsync dispatcher,
            ApplicationMetaData metaData, RevealStrategy revealStrategy,
            BeanFactory factory) {
        super(eventBus, view, proxy);

        this.dispatcher = dispatcher;
        this.revealStrategy = revealStrategy;
        this.factory = factory;
        this.cmd = new LoadConnectorCmd(dispatcher, factory, true);
    }

    public void setSelectedConnector(HttpConnector selection) {
        this.selectedConnector = selection;
        if(selection!=null)
            loadConnectorMetrics();

    }

    public void refresh() {

        // TODO Why is the list cleared and re-initialized afterwards?
        getView().setConnectors(Collections.<HttpConnector>emptyList());

        cmd.execute(new LoggingCallback<List<HttpConnector>>() {

            @Override
            public void onFailure(Throwable caught) {
                Log.error(caught.getMessage());
            }

            @Override
            public void onSuccess(List<HttpConnector> result) {
                getView().setConnectors(result);
            }
        });
    }

    private void loadConnectorMetrics() {

        if(null==selectedConnector)
            throw new RuntimeException("connector selection is null!");

        getView().clearSamples();

        ModelNode operation = new ModelNode();
        operation.get(ADDRESS).set(RuntimeBaseAddress.get());
        operation.get(ADDRESS).add("subsystem", "web");
        operation.get(ADDRESS).add("connector", selectedConnector.getName());

        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(INCLUDE_RUNTIME).set(true);

        dispatcher.execute(new DMRAction(operation), new LoggingCallback<DMRResponse>() {
            @Override
            public void onSuccess(DMRResponse dmrResponse) {
                ModelNode response = dmrResponse.get();

                if(response.isFailure())
                {
                    Log.error(Console.MESSAGES.failed("Web Metrics"), response.getFailureDescription());
                }
                else
                {
                    ModelNode result = response.get(RESULT).asObject();

                    Metric metric = new Metric(
                            result.get("requestCount").asLong(),
                            result.get("errorCount").asLong(),
                            result.get("processingTime").asLong(),
                            result.get("maxTime").asLong()
                    );

                    getView().setConnectorMetric(metric);
                }
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
        Console.MODULES.getServerStore().addChangeHandler(new PropagatesChange.Handler() {
            @Override
            public void onChange(Class<?> source) {

                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        getView().clearSamples();
                        if(isVisible()) refresh();
                    }
                });
            }
        });
    }


    @Override
    protected void onReset() {
        super.onReset();
        refresh();
    }

    @Override
    protected void revealInParent() {
        revealStrategy.revealInRuntimeParent(this);
    }
}
