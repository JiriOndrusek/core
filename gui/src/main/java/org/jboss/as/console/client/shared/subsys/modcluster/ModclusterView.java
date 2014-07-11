package org.jboss.as.console.client.shared.subsys.modcluster;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.DisposableViewImpl;
import org.jboss.as.console.client.layout.OneToOneLayout;
import org.jboss.as.console.client.shared.subsys.modcluster.model.Modcluster;
import org.jboss.ballroom.client.widgets.forms.CheckBoxItem;
import org.jboss.ballroom.client.widgets.forms.NumberBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextAreaItem;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;

/**
 * @author Pavel Slegr
 * @date 02/16/12
 */
public class ModclusterView extends DisposableViewImpl implements ModclusterPresenter.MyView{

    private ModclusterPresenter presenter;
    private ModclusterForm form;
    private ModclusterForm contextForm;
    private ModclusterForm proxyForm;
    private ModclusterForm sessionForm;
    private ModclusterForm networkingForm;

    private SSLEditor sslEditor;

    @Override
    public Widget createWidget() {


        form = new ModclusterForm(presenter);

        CheckBoxItem advertise = new CheckBoxItem("advertise", "Advertise");
        TextBoxItem advertiseSocket = new TextBoxItem("advertiseSocket", "Advertise Socket");
        TextBoxItem advertiseKey= new TextBoxItem("advertiseKey", "Advertise Key", false);

        TextBoxItem balancer = new TextBoxItem("balancer", "Balancer", false);
        TextBoxItem loadBalancingGroup = new TextBoxItem("loadBalancingGroup", "Load Balancing Group", false);

        form.setFields(loadBalancingGroup, balancer, advertiseSocket, advertiseKey, advertise);

        // ---

        contextForm = new ModclusterForm(presenter);

        TextAreaItem excludedContexts = new TextAreaItem("excludedContexts", "Excluded Contexts");
        excludedContexts.setRequired(false);
        CheckBoxItem autoEnableContexts = new CheckBoxItem("autoEnableContexts", "Auto Enable Contexts");

        contextForm.setFields(autoEnableContexts, excludedContexts);


        // ---

        proxyForm = new ModclusterForm(presenter);

        TextAreaItem proxyList = new TextAreaItem("proxyList", "Proxy List");
        proxyList.setRequired(false);
        TextBoxItem proxyUrl = new TextBoxItem("proxyUrl", "Proxy Url");

        proxyForm.setFields(proxyUrl, proxyList);


        //---
        sessionForm = new ModclusterForm(presenter);

        CheckBoxItem stickySession = new CheckBoxItem("stickySession", "Sticky Session");
        CheckBoxItem stickySessionForce = new CheckBoxItem("stickySessionForce", "Sticky Session Force");
        CheckBoxItem stickySessionRemove = new CheckBoxItem("stickySessionRemove", "Sticky Session Remove");

        sessionForm.setFields(stickySession, stickySessionForce, stickySessionRemove);

        // --

        networkingForm = new ModclusterForm(presenter);

        NumberBoxItem nodeTimeout = new NumberBoxItem("nodeTimeout", "Node Timeout", true);
        NumberBoxItem socketTimeout = new UndefinedAwareNumberBoxItem("socketTimeout", "Socket Timeout");
        NumberBoxItem stopContextTimeout = new UndefinedAwareNumberBoxItem("stopContextTimeout", "Stop Context Timeout");

        NumberBoxItem maxAttemps = new NumberBoxItem("maxAttemps", "Max Attemps", true);
        CheckBoxItem flushPackets = new CheckBoxItem("flushPackets", "Flush Packets");
        NumberBoxItem flushWait = new NumberBoxItem("flushWait", "Flush Wait", true);
        NumberBoxItem ping = new NumberBoxItem("ping", "Ping");
        NumberBoxItem workerTimeout = new NumberBoxItem("workerTimeout", "Worker Timeout", true) {
            @Override
            public boolean validate(final Number value) {
                return super.validate(value) && value.intValue() >= -1;
            }
        };
        NumberBoxItem ttl = new NumberBoxItem("ttl", "TTL", true);

        networkingForm.setFields(nodeTimeout, socketTimeout, stopContextTimeout, maxAttemps, flushPackets, flushWait, ping, ttl, workerTimeout);

        //  --

        sslEditor = new SSLEditor(presenter);

        // --

        OneToOneLayout layout = new OneToOneLayout()
                .setTitle("mod_cluster")
                .setHeadline("mod_cluster Subsystem")
                .setDescription(Console.CONSTANTS.subsys_modcluster_desc())
                .addDetail("Advertising", form.asWidget())
                .addDetail("Sessions", sessionForm.asWidget())
                .addDetail("Web Contexts", contextForm.asWidget())
                .addDetail("Proxies", proxyForm.asWidget())
                .addDetail("SSL", sslEditor.asWidget())
                .addDetail("Networking", networkingForm.asWidget());


        return layout.build();
    }

    @Override
    public void setPresenter(ModclusterPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateFrom(Modcluster modcluster) {
        form.updateFrom(modcluster);
        sessionForm.updateFrom(modcluster);
        contextForm.updateFrom(modcluster);
        proxyForm.updateFrom(modcluster);
        networkingForm.updateFrom(modcluster);

        sslEditor.edit(modcluster.getSSLConfig());
    }

    static class UndefinedAwareNumberBoxItem extends NumberBoxItem {

        public UndefinedAwareNumberBoxItem(final String name, final String title) {
            super(name, title);
        }

        @Override
        public boolean validate(final Number value) {
            if (value != null && value.intValue() <= 0) {
                setUndefined(true);
                setModified(true);
                return true;
            }
            return super.validate(value);
        }
    }
}
