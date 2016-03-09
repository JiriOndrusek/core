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

package org.jboss.as.console.client.shared.general;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.layout.FormLayout;
import org.jboss.as.console.client.layout.MultipleToOneLayout;
import org.jboss.as.console.client.shared.general.model.SocketBinding;
import org.jboss.as.console.client.shared.help.FormHelpPanel;
import org.jboss.as.console.client.widgets.forms.FormToolStrip;
import org.jboss.ballroom.client.widgets.ContentHeaderLabel;
import org.jboss.ballroom.client.widgets.forms.CheckBoxItem;
import org.jboss.ballroom.client.widgets.forms.DisclosureGroupRenderer;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.NumberBoxItem;
import org.jboss.ballroom.client.widgets.forms.StatusItem;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.ballroom.client.widgets.tools.ToolButton;
import org.jboss.ballroom.client.widgets.tools.ToolStrip;
import org.jboss.ballroom.client.widgets.window.Feedback;
import org.jboss.dmr.client.ModelNode;

import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @date 4/6/11
 */
public class SocketList {

    private SocketBindingPresenter presenter;
    private SocketTable socketTable;
    private Form<SocketBinding> form;
    private ContentHeaderLabel headline;

    public SocketList(SocketBindingPresenter presenter) {
        this.presenter = presenter;
    }

    public Widget asWidget() {

        ToolStrip toolstrip = new ToolStrip();

        ToolButton addBtn = new ToolButton(Console.CONSTANTS.common_label_add(), new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                presenter.launchNewSocketDialogue();
            }
        });
        addBtn.ensureDebugId(Console.DEBUG_CONSTANTS.debug_label_add_socketBindingView());
        toolstrip.addToolButtonRight(addBtn);

        ToolButton removeBtn = new ToolButton(Console.CONSTANTS.common_label_delete(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final SocketBinding editedEntity = form.getEditedEntity();
                Feedback.confirm(
                        Console.MESSAGES.deleteTitle("Socket Binding"),
                        Console.MESSAGES.deleteConfirm("Socket Binding " + editedEntity.getName()),
                        new Feedback.ConfirmationHandler() {
                            @Override
                            public void onConfirmation(boolean isConfirmed) {
                                if(isConfirmed)
                                    presenter.onDelete(editedEntity);
                            }
                        });
            }
        });
        removeBtn.ensureDebugId(Console.DEBUG_CONSTANTS.debug_label_remove_socketBindingView());
        toolstrip.addToolButtonRight(removeBtn);

        // -----------

        socketTable = new SocketTable();

        // -----------

        form = new Form<SocketBinding>(SocketBinding.class);
        form.setNumColumns(2);
        form.setEnabled(false);

        FormToolStrip<SocketBinding> detailToolStrip = new FormToolStrip<SocketBinding>(
                form,
                new FormToolStrip.FormCallback<SocketBinding>()
                {
                    @Override
                    public void onSave(Map<String, Object> changeset) {
                        SocketBinding updatedEntity = form.getUpdatedEntity();
                        presenter.saveSocketBinding(
                                updatedEntity.getName(),
                                form.getEditedEntity().getGroup(),  // TODO: why does it not get pushed through?
                                form.getChangedValues()
                        );
                    }

                    @Override
                    public void onDelete(SocketBinding entity) {

                    }
                }
        );

        detailToolStrip.providesDeleteOp(false);

        // ---
        TextItem nameItem = new TextItem("name", "Name");
        TextItem interfaceItem = new TextItem("interface", "Interface");
        interfaceItem.setEnabled(true);
        //TextItem defaultInterface = new TextItem("defaultInterface", "Default Interface");
        NumberBoxItem portItem = new NumberBoxItem("port", "Port");
        portItem.setEnabled(true);
        CheckBoxItem fixedPort = new CheckBoxItem("fixedPort", "Fixed Port");
        fixedPort.setEnabled(true);

        TextBoxItem multicastItem = new TextBoxItem("multiCastAddress", "Multicast Address") {
            @Override
            public boolean isRequired() {
                return false;
            }
        };
        NumberBoxItem multicastPortItem = new NumberBoxItem("multiCastPort", "Multicast Port") {
            @Override
            public boolean isRequired() {
                return false;
            }
        };

        form.setFields(nameItem, interfaceItem, portItem, fixedPort);
        form.setFieldsInGroup("Multicast", new DisclosureGroupRenderer(), multicastPortItem, multicastItem);
        form.bind(socketTable.getCellTable());

        final FormHelpPanel helpPanel = new FormHelpPanel(
                new FormHelpPanel.AddressCallback() {
                    @Override
                    public ModelNode getAddress() {
                        ModelNode address = new ModelNode();
                        address.add("socket-binding-group", form.getEditedEntity().getGroup());
                        address.add("socket-binding", "*");
                        return address;
                    }
                }, form
        );


        FormLayout formLayout = new FormLayout()
                .setTools(detailToolStrip)
                .setHelp(helpPanel)
                .setForm(form);


        // ------------------------------------------



        headline = new ContentHeaderLabel();

        MultipleToOneLayout layout = new MultipleToOneLayout()
                .setPlain(true)
                .setHeadlineWidget(headline)
                .setDescription(Console.CONSTANTS.common_socket_bindings_desc())
                .setMaster(Console.MESSAGES.available("Socket Bindings"), socketTable.asWidget())
                .setMasterTools(toolstrip)
                .setDetail(Console.CONSTANTS.common_label_selection(), formLayout.build());


        return layout.build();

    }

    public void setPresenter(SocketBindingPresenter presenter) {
        this.presenter = presenter;
    }

    public void setBindings(String groupName, List<SocketBinding> bindings) {
        headline.setText("Socket Bindings: Group "+ groupName);
        socketTable.updateFrom(groupName, bindings);
    }

    public void setEnabled(boolean b) {
        form.setEnabled(b);
    }
}
