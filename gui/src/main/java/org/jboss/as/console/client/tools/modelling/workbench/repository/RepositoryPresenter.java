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
package org.jboss.as.console.client.tools.modelling.workbench.repository;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.MainLayoutPresenter;
import org.jboss.as.console.client.domain.model.SimpleCallback;
import org.jboss.as.console.client.rbac.SecurityFramework;
import org.jboss.as.console.client.tools.modelling.workbench.repository.vfs.Entry;
import org.jboss.as.console.client.tools.modelling.workbench.repository.vfs.Vfs;
import org.jboss.as.console.mbui.Framework;
import org.jboss.as.console.mbui.Kernel;
import org.jboss.as.console.mbui.behaviour.CoreGUIContext;
import org.jboss.as.console.mbui.marshall.Marshaller;
import org.jboss.ballroom.client.widgets.window.DefaultWindow;
import org.jboss.dmr.client.dispatch.DispatchAsync;
import org.useware.kernel.model.Dialog;

import java.util.List;

/**
 * Lists the available interaction units and let the user create new interaction units.
 *
 * Events fired:
 * <ul>
 *     <li>Reify</li>
 * </ul>
 *
 * @author Harald Pehl
 * @date 10/30/2012
 */
public class RepositoryPresenter
        extends Presenter<RepositoryPresenter.MyView, RepositoryPresenter.MyProxy>
        implements EditorResizeEvent.ResizeListener
{

    private Vfs vfs;


    @ContentSlot
    public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_MainContent = new GwtEvent.Type<RevealContentHandler<?>>();
    private final Kernel kernel;
    private final DispatchAsync dispatcher;
    private SampleRepository sampleRepository;
    private DialogRef activeDialog;
    private DefaultWindow preview;
    public void setActiveDialog(DialogRef activeDialog) {
        this.activeDialog = activeDialog;
    }

    public interface MyView extends View
    {
        void setPresenter(RepositoryPresenter presenter);
        void setDocument(String name, String content);
        void setFullScreen(boolean fullscreen);
        void updateDirectory(Entry dir, List<Entry> entries);

        void clearHistory();
    }

    @ProxyStandard
    @NameToken("mbui-workbench")
    @NoGatekeeper
    public interface MyProxy extends ProxyPlace<RepositoryPresenter> {}

    @Inject
    public RepositoryPresenter(
            EventBus eventBus, MyView view, MyProxy proxy,
            final SampleRepository sampleRepository, final DispatchAsync dispatcher) {
        super(eventBus, view, proxy);

        this.sampleRepository = sampleRepository;
        this.dispatcher = dispatcher;

        CoreGUIContext globalContext = new CoreGUIContext(
                Console.MODULES.getCurrentSelectedProfile(),
                Console.MODULES.getCurrentUser(), Console.MODULES.getDomainEntityManager()
        );

        // mbui kernel instance
        this.kernel = new Kernel(sampleRepository, new Framework() {
            @Override
            public DispatchAsync getDispatcher() {
                return dispatcher;
            }

            @Override
            public SecurityFramework getSecurityFramework() {
                return Console.MODULES.getSecurityFramework();
            }
        }, globalContext);


        this.vfs = new Vfs();

    }

    @Override
    protected void onBind()
    {
        super.onBind();
        getView().setPresenter(this);
        getEventBus().addHandler(EditorResizeEvent.TYPE, this);
    }

    @Override
    public void onResizeRequested(boolean fullscreen) {
        getView().setFullScreen(fullscreen);
    }

    @Override
    protected void onReset() {
        loadDir(Entry.ROOT, true);
    }

    public void setDisableCache(boolean disableCache) {
        kernel.setCaching(disableCache);
    }

    public void onVisualize()
    {
        if(null==activeDialog)
        {
            Console.error("No dialog selected");
            return;
        }

        Dialog dialog = sampleRepository.getDialog(activeDialog.getName());
        DialogVisualization visualization = new DialogVisualization(dialog);
        DefaultWindow window = new DefaultWindow("Dialog: "+dialog.getId());
        window.setWidth(800);
        window.setHeight(600);
        window.setModal(true);
        window.setWidget(new ScrollPanel(visualization.getChart()));
        window.center();
    }

    public void onMarshall()
    {
        Marshaller m = new Marshaller();
        Document doc = m.marshall(sampleRepository.getDialog(activeDialog.getName()));
        getView().setDocument(activeDialog.getName(), doc.toString());
    }

    public void onReify()
    {
        if(null==activeDialog)
        {
            Console.error("No dialog selected");
            return;
        }

        if(preview!=null)
            preview.hide();

        kernel.reify(activeDialog.getName(), new AsyncCallback<Widget>() {
            @Override
            public void onFailure(Throwable throwable) {
                Console.error("Reification failed", throwable.getMessage());
            }

            @Override
            public void onSuccess(Widget widget) {

                //getView().show(widget);
                //kernel.onActivate();

                doPreview(widget);

                kernel.reset();
            }
        });
    }

    private void doPreview(Widget widget) {

        if(null==preview)
        {
            preview = new DefaultWindow("Preview: "+ activeDialog.getName());
            preview.setWidth(640);
            preview.setHeight(480);

        }

        preview.setWidget(widget);
        preview.center();

    }

    public void onActivate()
    {
        kernel.activate();
    }

    public void onResetDialog() {
        kernel.reset();
    }

    public void onPassivate() {
        kernel.passivate();
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainLayoutPresenter.TYPE_MainContent, this);
    }

    public void loadDir(final Entry dir, boolean clearHistory) {

        if(clearHistory)
            getView().clearHistory();

        vfs.listEntries(
                dir,
                new SimpleCallback<List<Entry>>() {
                    @Override
                    public void onSuccess(List<Entry> result) {

                        getView().updateDirectory(dir, result);

                    }
                });
    }
}