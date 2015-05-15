package org.jboss.as.console.client.shared.runtime.tx;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.shared.help.HelpSystem;
import org.jboss.as.console.client.shared.runtime.Metric;
import org.jboss.as.console.client.shared.runtime.RuntimeBaseAddress;
import org.jboss.as.console.client.shared.runtime.Sampler;
import org.jboss.as.console.client.shared.runtime.charts.BulletGraphView;
import org.jboss.as.console.client.shared.runtime.charts.Column;
import org.jboss.as.console.client.shared.runtime.charts.NumberColumn;
import org.jboss.as.console.client.shared.runtime.plain.PlainColumnView;
import org.jboss.as.console.client.shared.subsys.tx.TransactionPresenter;
import org.jboss.dmr.client.ModelDescriptionConstants;
import org.jboss.dmr.client.ModelNode;

/**
 * @author Heiko Braun
 * @date 10/25/11
 */
public class TXExecutionView implements Sampler {

    private TransactionPresenter presenter;
    private Sampler sampler = null;

    public TXExecutionView() {
    }

    public Widget asWidget() {
        return displayStrategy();
    }

    private Widget displayStrategy() {


        NumberColumn total = new NumberColumn("number-of-transactions","Number of Transactions");

        Column[] cols = new Column[] {
                total.setBaseline(true),
                new NumberColumn("number-of-committed-transactions","Committed").setComparisonColumn(total),
                new NumberColumn("number-of-aborted-transactions","Aborted").setComparisonColumn(total),
                new NumberColumn("number-of-timed-out-transactions", "Timed Out").setComparisonColumn(total)
        };

        String title = "Success Ratio";
        if(Console.protovisAvailable()) {
            sampler = new BulletGraphView(title, "total number")
                    .setColumns(cols);
        }
        else
        {
            final HelpSystem.AddressCallback addressCallback = new HelpSystem.AddressCallback() {
                @Override
                public ModelNode getAddress() {
                    ModelNode address = new ModelNode();
                    address.get(ModelDescriptionConstants.ADDRESS).set(RuntimeBaseAddress.get());
                    address.get(ModelDescriptionConstants.ADDRESS).add("subsystem", "transactions");
                    return address;
                }
            };

            sampler = new PlainColumnView(title, addressCallback)
                    .setColumns(cols)
                    .setWidth(100, Style.Unit.PCT);
        }

        return sampler.asWidget();
    }

    @Override
    public void addSample(Metric metric) {
        sampler.addSample(metric);
    }

    @Override
    public void clearSamples() {
        sampler.clearSamples();
    }

    @Override
    public long numSamples() {
        return sampler.numSamples();
    }

    @Override
    public void recycle() {
        sampler.recycle();
    }
}
