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

package org.jboss.as.console.client.shared.subsys.jca;

import java.util.Map;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.shared.help.FormHelpPanel;
import org.jboss.as.console.client.shared.subsys.Baseadress;
import org.jboss.as.console.client.shared.subsys.jca.model.DataSource;
import org.jboss.as.console.client.widgets.forms.FormToolStrip;
import org.jboss.as.console.client.widgets.forms.items.JndiNameItem;
import org.jboss.ballroom.client.widgets.forms.CheckBoxItem;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.StatusItem;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.dmr.client.ModelNode;

/**
 * @author Heiko Braun
 * @date 5/4/11
 */
public class DataSourceDetails {

    private Form<DataSource> form;
    private DataSourcePresenter presenter;

    public DataSourceDetails(DataSourcePresenter presenter) {
        this.presenter = presenter;
        form = new Form(DataSource.class);
        form.setNumColumns(2);

    }

    public Widget asWidget() {
        VerticalPanel detailPanel = new VerticalPanel();
        detailPanel.setStyleName("fill-layout-width");

        FormToolStrip<DataSource> toolStrip = new FormToolStrip<DataSource>(
                form,
                new FormToolStrip.FormCallback<DataSource>() {
                    @Override
                    public void onSave(Map<String, Object> changeset) {
                         presenter.onSaveDSDetails(form.getEditedEntity().getName(), form.getChangedValues());
                    }

                    @Override
                    public void onDelete(DataSource entity) {

                    }
                });


        toolStrip.providesDeleteOp(false);

        detailPanel.add(toolStrip.asWidget());

        final TextItem nameItem = new TextItem("name", "Name");
        TextBoxItem jndiItem = new JndiNameItem("jndiName", "JNDI");
        StatusItem enabledFlagItem = new StatusItem("enabled", "Is enabled?");
        CheckBoxItem stats = new CheckBoxItem("statisticsEnabled", "Statistics enabled?");
        TextBoxItem driverItem = new TextBoxItem("driverName", "Driver");
        CheckBoxItem spy = new CheckBoxItem("spy", "SPY");


        form.setFields(nameItem, jndiItem, enabledFlagItem, stats, driverItem, spy);

        form.setEnabled(false); // currently not editable

        Widget formWidget = form.asWidget();


        final FormHelpPanel helpPanel = new FormHelpPanel(
                new FormHelpPanel.AddressCallback() {
                    @Override
                    public ModelNode getAddress() {
                        ModelNode address = Baseadress.get();
                        address.add("subsystem", "datasources");
                        address.add("data-source", "*");
                        return address;
                    }
                }, form
        );
        detailPanel.add(helpPanel.asWidget());


        detailPanel.add(formWidget);

        ScrollPanel scroll = new ScrollPanel(detailPanel);
        return scroll;
    }


    public void setEnabled(boolean b) {
        form.setEnabled(b);
    }

    public DataSource getCurrentSelection() {
        return form.getEditedEntity();
    }

    public void updateFrom(DataSource ds)
    {
        form.edit(ds);
    }
}
