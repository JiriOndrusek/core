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

package org.jboss.as.console.client.shared.subsys.jca.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.as.console.client.shared.BeanFactory;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.dispatch.DispatchAsync;
import org.jboss.dmr.client.dispatch.impl.DMRAction;
import org.jboss.dmr.client.dispatch.impl.DMRResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.dmr.client.ModelDescriptionConstants.*;

/**
 * @author Heiko Braun
 */
public class StandaloneDriverStrategy implements DriverStrategy {

    final List<JDBCDriver> drivers = new ArrayList<JDBCDriver>();
    private BeanFactory factory;
    private DispatchAsync dispatcher;

    @Inject
    public StandaloneDriverStrategy(DispatchAsync dispatcher, BeanFactory factory) {
        this.dispatcher = dispatcher;
        this.factory = factory;
    }

    @Override
    public void refreshDrivers(final AsyncCallback<List<JDBCDriver>> callback) {

        drivers.clear();

        ModelNode operation = new ModelNode();
        operation.get(OP).set("installed-drivers-list");
        operation.get(ADDRESS).add("subsystem", "datasources");

        dispatcher.execute(new DMRAction(operation), new AsyncCallback<DMRResponse>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(DMRResponse result) {
                ModelNode response = result.get();

                if (!response.isFailure()) {
                    if (response.hasDefined(RESULT)) {
                        List<ModelNode> payload = response.get(RESULT).asList();

                        for (ModelNode item : payload) {
                            JDBCDriver driver = factory.jdbcDriver().as();
                            driver.setDriverClass(item.get("driver-class-name").asString());
                            driver.setName(item.get("driver-name").asString());
                            driver.setDeploymentName(item.get("deployment-name").asString());
                            driver.setMajorVersion(item.get("driver-major-version").asInt());
                            driver.setMinorVersion(item.get("driver-minor-version").asInt());

                            if (item.hasDefined("driver-xa-datasource-class-name")) {
                                driver.setXaDataSourceClass(item.get("driver-xa-datasource-class-name").asString());
                            }
                            addIfNotExists(driver);
                        }
                    }
                }
                callback.onSuccess(drivers);
            }
        });
    }

    private void addIfNotExists(JDBCDriver driver) {
        boolean doesExist = false;
        for (JDBCDriver existing : drivers) {
            // we don't control the AutoBean hash() or equals() method.
            if (existing.getName().equals(driver.getName()) &&
                    existing.getGroup().equals(driver.getGroup())) {
                doesExist = true;
                break;
            }
        }
        if (!doesExist) { drivers.add(driver); }
    }
}
