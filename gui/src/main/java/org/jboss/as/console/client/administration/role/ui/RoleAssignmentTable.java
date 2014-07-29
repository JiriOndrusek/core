/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.as.console.client.administration.role.ui;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jboss.as.console.client.administration.role.model.Principal;
import org.jboss.as.console.client.administration.role.model.RoleAssignment;
import org.jboss.as.console.client.administration.role.model.RoleAssignments;
import org.jboss.ballroom.client.widgets.tables.DefaultCellTable;
import org.jboss.ballroom.client.widgets.tables.DefaultPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jboss.as.console.client.administration.role.model.Principal.Type.GROUP;
import static org.jboss.as.console.client.administration.role.model.Principal.Type.USER;

/**
 * @author Harald Pehl
 */
public class RoleAssignmentTable implements IsWidget {

    private final Principal.Type type;
    private DefaultCellTable<RoleAssignment> table;
    private ListDataProvider<RoleAssignment> dataProvider;
    private SingleSelectionModel<RoleAssignment> selectionModel;

    public RoleAssignmentTable(final Principal.Type type) {this.type = type;}

    @SuppressWarnings("unchecked")
    public Widget asWidget() {
        VerticalPanel content = new VerticalPanel();
        content.setStyleName("fill-layout-width");

        // table
        RoleAssignment.Key keyProvider = new RoleAssignment.Key();
        table = new DefaultCellTable<RoleAssignment>(5, keyProvider);
        dataProvider = new ListDataProvider<RoleAssignment>(keyProvider);
        dataProvider.addDataDisplay(table);
        selectionModel = new SingleSelectionModel<RoleAssignment>(keyProvider);
        table.setSelectionModel(selectionModel);

        // columns
        Column<RoleAssignment, RoleAssignment> principalColumn =
                new Column<RoleAssignment, RoleAssignment>(UIHelper.newPrincipalCell()) {
                    @Override
                    public RoleAssignment getValue(final RoleAssignment assignment) {
                        return assignment;
                    }
                };
        Column<RoleAssignment, RoleAssignment> roleColumn =
                new Column<RoleAssignment, RoleAssignment>(UIHelper.newRolesCell()) {
                    @Override
                    public RoleAssignment getValue(final RoleAssignment assignment) {
                        return assignment;
                    }
            };
        table.addColumn(principalColumn,type == GROUP ? "Group" : "User");
        table.addColumn(roleColumn, "Roles");
        content.add(table);

        // pager
        DefaultPager pager = new DefaultPager();
        pager.setDisplay(table);
        content.add(pager);

        return content;
    }

    public void update(final RoleAssignments assignments) {
        if (type == GROUP) {
            List<RoleAssignment> groupAssignments = new ArrayList<RoleAssignment>(assignments.getGroupAssignments());
            Collections.sort(groupAssignments, new RoleAssignmentComparator());
            dataProvider.setList(groupAssignments);
        } else if (type == USER) {
            List<RoleAssignment> userAssignments = new ArrayList<RoleAssignment>(assignments.getUserAssignments());
            Collections.sort(userAssignments, new RoleAssignmentComparator());
            dataProvider.setList(userAssignments);
        }
        table.selectDefaultEntity();
    }

    public RoleAssignment getSelectedAssignment() {
        if (selectionModel != null) {
            return selectionModel.getSelectedObject();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    CellTable<RoleAssignment> getCellTable() {
        return table;
    }

    public void clearSelection() {
        selectionModel.clear();
    }

    private class RoleAssignmentComparator implements java.util.Comparator<RoleAssignment> {

        @Override
        public int compare(final RoleAssignment left, final RoleAssignment right) {
            return left.getPrincipal().getName().compareTo(right.getPrincipal().getName());
        }
    }
}
