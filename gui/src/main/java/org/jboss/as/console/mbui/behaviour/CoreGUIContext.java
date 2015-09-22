package org.jboss.as.console.mbui.behaviour;

import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.auth.CurrentUser;
import org.jboss.as.console.client.domain.profiles.CurrentProfileSelection;
import org.jboss.as.console.client.v3.stores.domain.HostStore;
import org.jboss.as.console.client.v3.stores.domain.ServerStore;
import org.useware.kernel.gui.behaviour.StatementContext;

import javax.inject.Inject;
import java.util.LinkedList;

/**
 * A default context for statements that reside with the core framework.<br/>
 * Historically this have been GIN singleton classes that carry state like the selected profile, host and server,
 * but also the current user and other "global" statements.
 *
 * @author Heiko Braun
 * @date 2/6/13
 */
public class CoreGUIContext implements StatementContext {

    public final static String USER = "global.user";
    public final static String SELECTED_PROFILE = "selected.profile";
    public final static String SELECTED_HOST = "selected.host";
    public final static String SELECTED_SERVER = "selected.server";
    private final ServerStore serverStore;

    private HostStore hostStore;

    private CurrentProfileSelection profileSelection;
    private CurrentUser userSelection;
    private StatementContext delegate = null;

    @Inject
    public CoreGUIContext(
            CurrentProfileSelection profileSelection, CurrentUser userSelection,
            ServerStore serverStore, HostStore hostStore) {
        this.profileSelection = profileSelection;
        this.userSelection = userSelection;
        this.serverStore = serverStore;
        this.hostStore = hostStore;
    }

    @Override
    public String resolve(String key) {
        if(USER.equals(key))
            return userSelection.getUserName();
        else if(delegate!=null)
            return delegate.resolve(key);

        return null;
    }

    private boolean isDomainMode() {
        return !Console.getBootstrapContext().isStandalone();
    }

    @Override
    public String[] resolveTuple(String key) {
        if(SELECTED_PROFILE.equals(key) && profileSelection.isSet())
            return new String[] {"profile", profileSelection.getName()};
        else if(isDomainMode() && SELECTED_HOST.equals(key))
            return new String[] {"host", hostStore.getSelectedHost()};
        else if(isDomainMode() && SELECTED_SERVER.equals(key))
            return new String[] {"server", serverStore.getSelectedServer().getServerName()};
        return null;
    }

    @Override
    public LinkedList<String> collect(String key) {
        LinkedList<String> items = new LinkedList<String>();
        String value = resolve(key);
        if(value!=null)
            items.add(value);
        return items;
    }

    @Override
    public LinkedList<String[]> collectTuples(String key) {

        LinkedList<String[]> items = new LinkedList<String[]>();
        String[] tuple = resolveTuple(key);
        if(tuple!=null)
            items.add(tuple);
        return items;
    }

    @Override
    public String get(String key) {
        return resolve(key);
    }

    @Override
    public String[] getTuple(String key) {
        return resolveTuple(key);
    }
}
