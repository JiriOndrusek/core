package org.jboss.as.console.client.shared.general.wizard;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.shared.general.SocketBindingPresenter;
import org.jboss.as.console.client.shared.general.model.SocketBinding;
import org.jboss.as.console.client.shared.help.FormHelpPanel;
import org.jboss.ballroom.client.widgets.forms.ComboBoxItem;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.FormValidation;
import org.jboss.ballroom.client.widgets.forms.NumberBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.ballroom.client.widgets.window.DialogueOptions;
import org.jboss.ballroom.client.widgets.window.WindowContentBuilder;
import org.jboss.dmr.client.ModelNode;

import java.util.List;

/**
 * @author Heiko Braun
 * @date 6/10/11
 */
public class NewSocketWizard {

    private SocketBindingPresenter presenter;
    //private List<String> bindingGroups;

    private String bindingGroup;
    public NewSocketWizard(SocketBindingPresenter socketBindingPresenter, String bindingGroup) {
        this.presenter = socketBindingPresenter;
        this.bindingGroup = bindingGroup;
    }

    public Widget asWidget() {
        VerticalPanel layout = new VerticalPanel();
        layout.setStyleName("window-content");

        final Form<SocketBinding> form = new Form(SocketBinding.class);

        TextBoxItem nameItem = new TextBoxItem("name", "Name");
        NumberBoxItem portItem = new NumberBoxItem("port", "Port");
       /* final ComboBoxItem groupItem = new ComboBoxItem("group", "Binding Group");

        groupItem.setValueMap(bindingGroups);


        int i=0;
        for(String group : bindingGroups)
        {
            if(group.equals("standard-sockets"))
                break;
            i++;
        }
        groupItem.selectItem(i);
*/


        TextItem groupItem = new TextItem("group", "Group");
        groupItem.setValue(bindingGroup);
        form.setFields(nameItem, portItem, groupItem);

        DialogueOptions options = new DialogueOptions(

                // save
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {

                        FormValidation validation = form.validate();
                        if(!validation.hasErrors())
                        {
                            SocketBinding newGroup = form.getUpdatedEntity();
                            presenter.createNewSocketBinding(newGroup);
                        }
                    }
                },

                // cancel
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.closeDialoge();
                    }
                }

        );

        // ----------------------------------------

        Widget formWidget = form.asWidget();

        final FormHelpPanel helpPanel = new FormHelpPanel(
                new FormHelpPanel.AddressCallback() {
                    @Override
                    public ModelNode getAddress() {
                        ModelNode address = new ModelNode();
                        address.add("socket-binding-group", "*");
                        address.add("socket-binding", "*");
                        return address;
                    }
                }, form
        );
        layout.add(helpPanel.asWidget());

        layout.add(formWidget);

        return new WindowContentBuilder(layout, options).build();
    }
}
