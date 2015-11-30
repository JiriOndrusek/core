package org.jboss.as.console.client.standalone;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.DisposableViewImpl;
import org.jboss.as.console.client.core.UIConstants;
import org.jboss.as.console.client.layout.OneToOneLayout;
import org.jboss.as.console.client.shared.runtime.ext.Extension;
import org.jboss.as.console.client.shared.runtime.ext.ExtensionView;
import org.jboss.as.console.client.shared.state.ReloadState;
import org.jboss.as.console.client.shared.state.ServerState;
import org.jboss.as.console.client.widgets.tabs.DefaultTabLayoutPanel;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.ballroom.client.widgets.icons.Icons;
import org.jboss.ballroom.client.widgets.tools.ToolButton;
import org.jboss.ballroom.client.widgets.tools.ToolStrip;
import org.jboss.ballroom.client.widgets.window.Feedback;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @deprecated to be removed
 * @author Heiko Braun
 * @date 6/7/11
 */
public class StandaloneServerView extends DisposableViewImpl implements StandaloneServerPresenter.MyView {

    private StandaloneServerPresenter presenter;
    private Label headline;
    private DeckPanel reloadPanel;
    private Form<StandaloneServer> form;
    private ExtensionView extensions = new ExtensionView();
    private HTML reloadMessage;
    private ToolButton reloadBtn;

    @Override
    public Widget createWidget() {

        reloadBtn = new ToolButton(Console.CONSTANTS.common_label_reload(), new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                Feedback.confirm(Console.CONSTANTS.server_reload_title(),
                        Console.MESSAGES.server_reload_confirm(form.getEditedEntity().getName()),
                        new Feedback.ConfirmationHandler() {
                            @Override
                            public void onConfirmation(boolean isConfirmed) {
                                if (isConfirmed) {
                                    presenter.onReloadServerConfig();
                                }
                            }
                        });
            }
        });
        reloadBtn.ensureDebugId(Console.DEBUG_CONSTANTS.debug_label_reload_standaloneServerView());
        ToolStrip tools = new ToolStrip();
        tools.addToolButtonRight(reloadBtn);

        // TODO HAL-327 Replace with reloadBtn.setOperationAddress(...) once the presenter is no longer
        // annotated with @NoGatekeeper. That is once the presenter is no longer the homepage for standalone
        boolean granted = true;
        Set<String> roles = Console.getBootstrapContext().getRoles();
        for (Iterator<String> iterator = roles.iterator(); iterator.hasNext() && granted; ) {
            String role = iterator.next();
            granted = "superuser".equalsIgnoreCase(role) || "administrator".equalsIgnoreCase(role) || "maintainer"
                    .equalsIgnoreCase(role) || "operator".equalsIgnoreCase(role);
        }
        if (!granted) {
            tools.setVisible(false);
            tools.getElement().addClassName("rbac-suppressed");
        }

        headline = new Label("HEADLINE");
        headline.setStyleName("content-header-label");

        form = new Form<StandaloneServer>(StandaloneServer.class);
        form.setNumColumns(2);
        form.setEnabled(false);

        TextItem codename = new TextItem("releaseCodename", "Code Name");
        TextItem version = new TextItem("releaseVersion", "Version");
        TextItem state = new TextItem("serverState", "Server State");

        form.setFields(codename, version, state);

        // ----

        reloadPanel = new DeckPanel();
        reloadPanel.setStyleName("fill-layout-width");

        // ----

        VerticalPanel configUptodate = new VerticalPanel();
        HorizontalPanel uptodateContent = new HorizontalPanel();
        uptodateContent.setStyleName("status-panel");
        uptodateContent.addStyleName("serverUptoDate");

        Image img = new Image(Icons.INSTANCE.status_good());
        HTML desc = new HTML(Console.CONSTANTS.server_config_uptodate());
        uptodateContent.add(desc);
        uptodateContent.add(img);

        //img.getElement().getParentElement().addClassName("InfoBlock");
        img.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:middle;width:20%");
        desc.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:middle");

        configUptodate.add(uptodateContent);

        // --

        VerticalPanel configNeedsUpdate = new VerticalPanel();
        configNeedsUpdate.add(tools.asWidget());

        HorizontalPanel staleContent = new HorizontalPanel();
        staleContent.setStyleName("status-panel");
        staleContent.addStyleName("serverNeedsUpdate");

        Image img2 = new Image(Icons.INSTANCE.status_warn());
        reloadMessage = new HTML(Console.CONSTANTS.server_reload_desc());
        staleContent.add(reloadMessage);
        staleContent.add(img2);

        //img2.getElement().getParentElement().addClassName("WarnBlock");
        img2.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:middle;width:20%");
        reloadMessage.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:middle");

        configNeedsUpdate.add(staleContent);

        reloadPanel.add(configUptodate);
        reloadPanel.add(configNeedsUpdate);
        reloadPanel.showWidget(0);

        OneToOneLayout layout = new OneToOneLayout()
                .setTitle("Standalone Server")
                .setPlain(true)
                .setHeadlineWidget(headline)
                .setDescription(Console.CONSTANTS.server_config_desc())
                .setMaster("Configuration", form.asWidget())
                .addDetail("Status", reloadPanel);

        // ---------------------
        DefaultTabLayoutPanel tabLayoutpanel = new DefaultTabLayoutPanel(40, Style.Unit.PX);
        tabLayoutpanel.addStyleName("default-tabpanel");


        tabLayoutpanel.add(layout.build(), "Server", true);
        tabLayoutpanel.add(extensions.asWidget(), "Extensions", true);

        tabLayoutpanel.selectTab(0);

        return tabLayoutpanel;

    }

    @Override
    public void setPresenter(StandaloneServerPresenter presenter) {
        this.presenter = presenter;
        extensions.setPresenter(presenter);
    }

    @Override
    public void updateFrom(StandaloneServer server) {
        form.edit(server);
        headline.setText("Server: "+ server.getName());
    }

    @Override
    public void setReloadRequired(ReloadState reloadState) {

        if(reloadState.isStaleModel()) {

            final Map<String, ServerState> serverStates = reloadState.getServerStates();

            StringBuffer sb = new StringBuffer();
            ServerState serverState = serverStates.values().iterator().next();
            String message = serverState.isReloadRequired() ?
                    ((UIConstants) GWT.create(UIConstants.class)).serverConfigurationNeedsToBeReloaded() :
                    ((UIConstants) GWT.create(UIConstants.class)).serverNeedsToBeRestarted();

            sb.append(message).append("\n\n");

            reloadMessage.setText(sb.toString());

            reloadBtn.setVisible(serverState.isReloadRequired());
        }

        reloadPanel.showWidget( reloadState.isStaleModel() ? 1:0);
    }

    @Override
    public void setExtensions(List<Extension> result) {
        extensions.setExtensions(result);
    }
}
