package org.jboss.as.console.client.shared.subsys.jca;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.shared.expr.ExpressionAdapter;
import org.jboss.as.console.client.shared.help.FormHelpPanel;
import org.jboss.as.console.client.shared.subsys.Baseadress;
import org.jboss.as.console.client.shared.subsys.jca.model.PoolConfig;
import org.jboss.as.console.client.widgets.forms.FormToolStrip;
import org.jboss.ballroom.client.widgets.common.ButtonDropdown;
import org.jboss.ballroom.client.widgets.forms.CheckBoxItem;
import org.jboss.ballroom.client.widgets.forms.ComboBoxItem;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.FormValidation;
import org.jboss.ballroom.client.widgets.forms.NumberBoxItem;
import org.jboss.ballroom.client.widgets.tools.ToolButton;
import org.jboss.ballroom.client.widgets.tools.ToolButtonDropdown;
import org.jboss.dmr.client.ModelNode;

import java.util.Map;

/**
 * @author Heiko Braun
 * @date 9/16/11
 */
public class PoolConfigurationView {

    private Form<PoolConfig> form;
    private String editedName = null;
    private PoolManagement management;
    private boolean xaDisplay = false;

    public PoolConfigurationView(PoolManagement management) {
        this.management = management;
    }

    Widget asWidget() {

        final NumberBoxItem maxCon = new NumberBoxItem("maxPoolSize", "Max Pool Size");
        final NumberBoxItem minCon = new NumberBoxItem("minPoolSize", "Min Pool Size");
        CheckBoxItem strictMin = new CheckBoxItem("poolStrictMin", "Strict Minimum");
        CheckBoxItem prefill = new CheckBoxItem("poolPrefill", "Prefill enabled");

        ComboBoxItem flushStrategy = new ComboBoxItem("flushStrategy", "Flush Strategy");
        flushStrategy.setValueMap(new String[] {"FailingConnectionOnly", "IdleConnections", "EntirePool"});

        final NumberBoxItem idleTimeout = new NumberBoxItem("idleTimeout", "Idle Timeout");

        ComboBoxItem trackStmt = new ComboBoxItem("trackStatements", "Track Statements");
        trackStmt.setValueMap(new String[] {"true", "false", "nowarn"});

        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("fill-layout");
        form = new Form<PoolConfig>(PoolConfig.class) {
            @Override
            public FormValidation validate() {
                FormValidation superValidation = super.validate();
                PoolConfig updatedEntity = this.getUpdatedEntity();

                // only works on real values
                if(ExpressionAdapter.getExpressions(updatedEntity).isEmpty())
                {
                    int minPoolSize = updatedEntity.getMinPoolSize();
                    int maxPoolSize = updatedEntity.getMaxPoolSize();
                    if(minPoolSize > maxPoolSize){
                        superValidation.addError("maxPoolSize");
                        maxCon.setErroneous(true);
                        maxCon.setErrMessage("Max Pool Size must be greater than Min Pool Size");
                    }
                }
                return superValidation;
            }

        };
        form.setNumColumns(2);
        form.setEnabled(false);

        form.setFields(minCon, maxCon, strictMin, prefill, flushStrategy, idleTimeout, trackStmt);

        FormToolStrip<PoolConfig> toolStrip = new FormToolStrip<PoolConfig>(
                form,
                new FormToolStrip.FormCallback<PoolConfig>() {
                    @Override
                    public void onSave(Map<String, Object> changeset) {
                        management.onSavePoolConfig(editedName, changeset);
                    }

                    @Override
                    public void onDelete(PoolConfig entity) {

                    }
                }
        );

        // TODO: https://issues.jboss.org/browse/AS7-3254
        if(Console.getBootstrapContext().isStandalone()) {
            final ToolButtonDropdown flushDropdown = new ToolButtonDropdown("Flush Gracefully", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    management.onDoFlush(editedName, "flush-gracefully-connection-in-pool");
                }
            });
            flushDropdown.addItem("Flush Idle", new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    management.onDoFlush(editedName, "flush-idle-connection-in-pool");
                }
            });
            flushDropdown.addItem("Flush Invalid", new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    management.onDoFlush(editedName, "flush-invalid-connection-in-pool");
                }
            });
            flushDropdown.addItem("Flush All", new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    management.onDoFlush(editedName, "flush-all-connection-in-pool");
                }
            });
            toolStrip.addToolButtonRight(flushDropdown);
        }

        FormHelpPanel helpPanel = new FormHelpPanel(new FormHelpPanel.AddressCallback() {
            @Override
            public ModelNode getAddress() {

                ModelNode address = Baseadress.get();
                address.add("subsystem", "datasources");

                if(xaDisplay)
                    address.add("xa-data-source", "*");
                else
                    address.add("data-source", "*");
                return address;
            }
        }, form);

        panel.add(toolStrip.asWidget());
        panel.add(helpPanel.asWidget());
        panel.add(form.asWidget());

        return panel;
    }

    public Form<PoolConfig> getForm() {
        return form;
    }

    public void updateFrom(String name, PoolConfig poolConfig) {
        this.editedName = name;
        form.edit(poolConfig);
    }
}
