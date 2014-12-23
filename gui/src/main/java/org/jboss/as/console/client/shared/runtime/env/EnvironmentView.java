package org.jboss.as.console.client.shared.runtime.env;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.core.SuspendableViewImpl;
import org.jboss.as.console.client.shared.general.EnvironmentProperties;
import org.jboss.as.console.client.shared.properties.PropertyRecord;

import java.util.List;

public class EnvironmentView extends SuspendableViewImpl implements EnvironmentPresenter.MyView
{
    private EnvironmentProperties properties;

    @Override
    public void setPresenter(EnvironmentPresenter environmentPresenter) {

    }

    @Override
    public Widget createWidget()
    {
        properties = new EnvironmentProperties();

        return properties.asWidget();
    }

    public void setEnvironment(final List<PropertyRecord> environment)
    {
        properties.setProperties(environment);
    }

    public void clearEnvironment()
    {
        properties.clearValues();
    }
}
