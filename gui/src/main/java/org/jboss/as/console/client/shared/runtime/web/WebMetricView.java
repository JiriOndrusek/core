package org.jboss.as.console.client.shared.runtime.web;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.SuspendableViewImpl;
import org.jboss.as.console.client.layout.SimpleLayout;
import org.jboss.as.console.client.shared.help.HelpSystem;
import org.jboss.as.console.client.shared.runtime.Metric;
import org.jboss.as.console.client.shared.runtime.RuntimeBaseAddress;
import org.jboss.as.console.client.shared.runtime.Sampler;
import org.jboss.as.console.client.shared.runtime.charts.BulletGraphView;
import org.jboss.as.console.client.shared.runtime.charts.Column;
import org.jboss.as.console.client.shared.runtime.charts.NumberColumn;
import org.jboss.as.console.client.shared.runtime.plain.PlainColumnView;
import org.jboss.as.console.client.shared.subsys.web.model.HttpConnector;
import org.jboss.ballroom.client.widgets.tables.DefaultCellTable;
import org.jboss.ballroom.client.widgets.tools.ToolButton;
import org.jboss.ballroom.client.widgets.tools.ToolStrip;
import org.jboss.dmr.client.ModelDescriptionConstants;
import org.jboss.dmr.client.ModelNode;

import java.util.List;

/**
 * @author Heiko Braun
 * @date 12/10/11
 */
public class WebMetricView extends SuspendableViewImpl implements WebMetricPresenter.MyView {

    private WebMetricPresenter presenter;
    private Sampler sampler;
    private DefaultCellTable<HttpConnector> connectorTable;
    private ListDataProvider<HttpConnector> connectorProvider;

    @Override
    public Widget createWidget() {


        final ToolStrip toolStrip = new ToolStrip();
        toolStrip.addToolButtonRight(new ToolButton(Console.CONSTANTS.common_label_refresh(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.setSelectedConnector(getCurrentSelection());
            }
        }));

        // ----

        ProvidesKey<HttpConnector> providesKey = new ProvidesKey<HttpConnector>() {
            @Override
            public Object getKey(final HttpConnector item) {
                return item.getName() + "_" + item.getProtocol();
            }
        };
        connectorTable = new DefaultCellTable<HttpConnector>(10, providesKey);
        connectorTable.setSelectionModel(new SingleSelectionModel<HttpConnector>(providesKey));

        connectorProvider = new ListDataProvider<HttpConnector>();
        connectorProvider.addDataDisplay(connectorTable);

        com.google.gwt.user.cellview.client.Column<HttpConnector, String> nameColumn = new com.google.gwt.user.cellview.client.Column<HttpConnector, String>(new TextCell()) {
            @Override
            public String getValue(HttpConnector object) {
                return object.getName();
            }
        };


        com.google.gwt.user.cellview.client.Column<HttpConnector, String> protocolColumn = new com.google.gwt.user.cellview.client.Column<HttpConnector, String>(new TextCell()) {
            @Override
            public String getValue(HttpConnector object) {
                return object.getProtocol();
            }
        };

        connectorTable.addColumn(nameColumn, "Name");
        connectorTable.addColumn(protocolColumn, "Protocol");

        connectorTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler(){
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                HttpConnector connector = getCurrentSelection();
                presenter.setSelectedConnector(connector);

            }
        });
        connectorTable.getElement().setAttribute("style", "margin-top:15px;margin-bottom:15px;");

        // ----


        NumberColumn requestCount = new NumberColumn("requestCount","Number of Requests");

        Column[] cols = new Column[] {
                requestCount.setBaseline(true),
                new NumberColumn("errorCount","Errors").setComparisonColumn(requestCount)
        };

        String title = "Request per Connector";

        final HelpSystem.AddressCallback addressCallback = new HelpSystem.AddressCallback() {
            @Override
            public ModelNode getAddress() {
                ModelNode address = new ModelNode();
                address.get(ModelDescriptionConstants.ADDRESS).set(RuntimeBaseAddress.get());
                address.get(ModelDescriptionConstants.ADDRESS).add("subsystem", "web");
                address.get(ModelDescriptionConstants.ADDRESS).add("connector", "*");
                return address;
            }
        };


        if(Console.protovisAvailable())
        {
            sampler = new BulletGraphView(title, "total number")
                    .setColumns(cols);
        }
        else
        {
            sampler = new PlainColumnView(title, addressCallback)
                    .setColumns(cols)
                    .setWidth(100, Style.Unit.PCT);
        }

        // ----

        SimpleLayout layout = new SimpleLayout()
                .setTitle("Web")
                .setTopLevelTools(toolStrip.asWidget())
                .setHeadline("Web Metrics")
                .setDescription(Console.CONSTANTS.subys_web_metric_desc())
                .addContent("Connector Selection", connectorTable)
                .addContent("Connector Metrics", sampler.asWidget());

        return layout.build();
    }

    private HttpConnector getCurrentSelection() {
        return ((SingleSelectionModel<HttpConnector>) connectorTable.getSelectionModel()).getSelectedObject();
    }

    @Override
    public void setPresenter(WebMetricPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clearSamples() {
        sampler.clearSamples();
    }

    @Override
    public void setConnectorMetric(Metric metric) {
        sampler.addSample(metric);
    }

    @Override
    public void setConnectors(List<HttpConnector> list) {
        connectorProvider.setList(list);
        connectorTable.selectDefaultEntity();
    }
}
