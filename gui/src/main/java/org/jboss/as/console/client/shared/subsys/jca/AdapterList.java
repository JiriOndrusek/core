package org.jboss.as.console.client.shared.subsys.jca;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.NameTokens;
import org.jboss.as.console.client.layout.MultipleToOneLayout;
import org.jboss.as.console.client.shared.help.FormHelpPanel;
import org.jboss.as.console.client.shared.properties.NewPropertyWizard;
import org.jboss.as.console.client.shared.properties.PropertyEditor;
import org.jboss.as.console.client.shared.properties.PropertyManagement;
import org.jboss.as.console.client.shared.properties.PropertyRecord;
import org.jboss.as.console.client.shared.subsys.Baseadress;
import org.jboss.as.console.client.shared.subsys.jca.model.ResourceAdapter;
import org.jboss.as.console.client.widgets.forms.FormToolStrip;
import org.jboss.as.console.client.widgets.tables.ViewLinkCell;
import org.jboss.ballroom.client.widgets.forms.CheckBoxItem;
import org.jboss.ballroom.client.widgets.forms.ComboBoxItem;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.ListItem;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.ballroom.client.widgets.tables.DefaultCellTable;
import org.jboss.ballroom.client.widgets.tools.ToolButton;
import org.jboss.ballroom.client.widgets.tools.ToolStrip;
import org.jboss.ballroom.client.widgets.window.DefaultWindow;
import org.jboss.ballroom.client.widgets.window.Feedback;
import org.jboss.dmr.client.ModelNode;

import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @date 12/12/11
 */
public class AdapterList implements PropertyManagement {

    private ResourceAdapterPresenter presenter;
    private DefaultCellTable<ResourceAdapter> table;
    private ListDataProvider<ResourceAdapter> dataProvider;

    private Form<ResourceAdapter> attributesForm;
    private Form<ResourceAdapter> wmForm;
    private PropertyEditor propertyEditor;
    private DefaultWindow window;

    public AdapterList(ResourceAdapterPresenter presenter) {
        this.presenter = presenter;
    }

    Widget asWidget() {


        ToolStrip topLevelTools = new ToolStrip();
        topLevelTools.addToolButtonRight(new ToolButton(Console.CONSTANTS.common_label_add(), new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                presenter.launchNewAdapterWizard();
            }
        }));

        ClickHandler clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                final ResourceAdapter selection = getCurrentSelection();

                Feedback.confirm(
                        Console.MESSAGES.deleteTitle("Resource Adapter"),
                        Console.MESSAGES.deleteConfirm("Resource Adapter " + selection.getName()),
                        new Feedback.ConfirmationHandler() {
                            @Override
                            public void onConfirmation(boolean isConfirmed) {
                                if (isConfirmed) {
                                    presenter.onDelete(selection);
                                }
                            }
                        });
            }
        };
        ToolButton deleteBtn = new ToolButton(Console.CONSTANTS.common_label_delete());
        deleteBtn.addClickHandler(clickHandler);
        topLevelTools.addToolButtonRight(deleteBtn);

        // -------


        table = new DefaultCellTable<ResourceAdapter>(5,
                new ProvidesKey<ResourceAdapter>() {
                    @Override
                    public Object getKey(ResourceAdapter item) {
                        return item.getName();
                    }
                });

        dataProvider = new ListDataProvider<ResourceAdapter>();
        dataProvider.addDataDisplay(table);

        TextColumn<ResourceAdapter> nameColumn = new TextColumn<ResourceAdapter>() {
                    @Override
                    public String getValue(ResourceAdapter record) {
                        return record.getName();
                    }
                };

        TextColumn<ResourceAdapter> numberConnections = new TextColumn<ResourceAdapter>() {
            @Override
            public String getValue(ResourceAdapter record) {
                return String.valueOf(record.getConnectionDefinitions().size());
            }
        };

        Column<ResourceAdapter, ResourceAdapter> option = new Column<ResourceAdapter, ResourceAdapter>(
                new ViewLinkCell<ResourceAdapter>(Console.CONSTANTS.common_label_view(), new ActionCell.Delegate<ResourceAdapter>() {
                    @Override
                    public void execute(ResourceAdapter selection) {
                        presenter.getPlaceManager().revealPlace(
                                new PlaceRequest.Builder().nameToken(NameTokens.ResourceAdapterPresenter)
                                        .with("name", selection.getName()).build());
                    }
                })
        ) {
            @Override
            public ResourceAdapter getValue(ResourceAdapter manager) {
                return manager;
            }
        };

        table.addColumn(nameColumn, "Name");
        table.addColumn(numberConnections, "Connection Definition");
        table.addColumn(option, "Option");


        // -------

        VerticalPanel attributesFormPanel = new VerticalPanel();
        attributesFormPanel.setStyleName("fill-layout-width");

        attributesForm = new Form<ResourceAdapter>(ResourceAdapter.class);
        attributesForm.setNumColumns(2);

        FormToolStrip<ResourceAdapter> attributesToolStrip = new FormToolStrip<ResourceAdapter>(
                attributesForm,
                new FormToolStrip.FormCallback<ResourceAdapter>() {
                    @Override
                    public void onSave(Map<String, Object> changeset) {
                        presenter.onSave(attributesForm.getEditedEntity(), attributesForm.getChangedValues());
                    }

                    @Override
                    public void onDelete(ResourceAdapter entity) {

                    }
                });

        attributesToolStrip.providesDeleteOp(false);


        attributesFormPanel.add(attributesToolStrip.asWidget());

        // ----

        TextItem nameItem = new TextItem("name", "Name");
        TextBoxItem archiveItem = new TextBoxItem("archive", "Archive");
        TextBoxItem moduleItem = new TextBoxItem("module", "Module");
        ComboBoxItem txItem = new ComboBoxItem("transactionSupport", "Transaction Support");
        txItem.setDefaultToFirstOption(true);
        txItem.setValueMap(new String[]{"NoTransaction", "LocalTransaction", "XATransaction"});
        CheckBoxItem statisticsEnabled = new CheckBoxItem("statisticsEnabled", "Statistics Enabled");
        TextBoxItem bootstrapContext = new TextBoxItem("bootstrapContext", "Bootstrap Context");
        ListItem beanValidationGroups = new ListItem("beanValidationGroups", "Bean Validation Groups");

        attributesForm.setFields(nameItem, archiveItem, moduleItem, txItem, statisticsEnabled, bootstrapContext,
                beanValidationGroups);

        final FormHelpPanel attributesHelpPanel = new FormHelpPanel(
                new FormHelpPanel.AddressCallback() {
                    @Override
                    public ModelNode getAddress() {
                        ModelNode address = Baseadress.get();
                        address.add("subsystem", "resource-adapters");
                        address.add("resource-adapter", "*");
                        return address;
                    }
                }, attributesForm
        );
        attributesFormPanel.add(attributesHelpPanel.asWidget());
        attributesForm.bind(table);

        attributesFormPanel.add(attributesForm.asWidget());

        attributesForm.setEnabled(false);



        // -------

        VerticalPanel wmFormPanel = new VerticalPanel();
        wmFormPanel.setStyleName("fill-layout-width");

        wmForm = new Form<ResourceAdapter>(ResourceAdapter.class);
        wmForm.setNumColumns(2);

        FormToolStrip<ResourceAdapter> wmToolStrip = new FormToolStrip<ResourceAdapter>(
                wmForm,
                new FormToolStrip.FormCallback<ResourceAdapter>() {
                    @Override
                    public void onSave(Map<String, Object> changeset) {
                        presenter.onSave(wmForm.getEditedEntity(), wmForm.getChangedValues());
                    }

                    @Override
                    public void onDelete(ResourceAdapter entity) {

                    }
                });

        wmToolStrip.providesDeleteOp(false);
        wmFormPanel.add(wmToolStrip.asWidget());

        // ----

        CheckBoxItem wmEnabled = new CheckBoxItem("wmEnabled", "Enabled");
        ListItem wmDefaultGroups = new ListItem("wmDefaultGroups", "Default Groups");
        TextBoxItem wmDefaultPrincipal = new TextBoxItem("wmDefaultPrincipal", "Default Principal");
        TextBoxItem wmSecurityDomain = new TextBoxItem("wmSecurityDomain", "Security Domain");
        CheckBoxItem wmMappingRequired = new CheckBoxItem("wmMappingRequired", "Mapping Required");

        wmForm.setFields(wmEnabled, wmDefaultGroups, wmDefaultPrincipal, wmSecurityDomain, wmMappingRequired);

        final FormHelpPanel wmHelpPanel = new FormHelpPanel(
                new FormHelpPanel.AddressCallback() {
                    @Override
                    public ModelNode getAddress() {
                        ModelNode address = Baseadress.get();
                        address.add("subsystem", "resource-adapters");
                        address.add("resource-adapter", "*");
                        return address;
                    }
                }, wmForm
        );
        wmFormPanel.add(wmHelpPanel.asWidget());
        wmForm.bind(table);

        wmFormPanel.add(wmForm.asWidget());

        wmForm.setEnabled(false);



        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                ResourceAdapter ra = getCurrentSelection();
                propertyEditor.setProperties("", ra.getProperties());
            }
        });

        // ----

        propertyEditor = new PropertyEditor(this, true);

        // ----
        MultipleToOneLayout layoutBuilder = new MultipleToOneLayout()
                .setPlain(true)
                .setTitle("Resource Adapter")
                .setHeadline("Resource Adapters")
                .setDescription(Console.CONSTANTS.subsys_jca_resource_adapter_desc())
                .setMaster(Console.MESSAGES.available("Resource Adapters"), table)
                .setMasterTools(topLevelTools.asWidget())
                .addDetail("Attributes", attributesFormPanel)
                .addDetail("Work Manager Security", wmFormPanel)
                .addDetail("Properties", propertyEditor.asWidget());

        return layoutBuilder.build();
    }


    private ResourceAdapter getCurrentSelection() {
        ResourceAdapter selection = ((SingleSelectionModel<ResourceAdapter>) table.getSelectionModel()).getSelectedObject();
        return selection;
    }

    public void setAdapters(List<ResourceAdapter> adapters) {
        dataProvider.setList(adapters);

        propertyEditor.clearValues();
        table.selectDefaultEntity();

    }

    @Override
    public void onCreateProperty(String reference, PropertyRecord prop) {
        closePropertyDialoge();

        presenter.onCreateAdapterProperty(getCurrentSelection(), prop);
    }

    @Override
    public void onDeleteProperty(String reference, PropertyRecord prop) {
        presenter.onRemoveAdapterProperty(getCurrentSelection(), prop);
    }

    @Override
    public void onChangeProperty(String reference, PropertyRecord prop) {
        // not used
    }

    @Override
    public void launchNewPropertyDialoge(String reference) {
        window = new DefaultWindow(Console.MESSAGES.createTitle("Config Property"));
        window.setWidth(480);
        window.setHeight(360);

        window.trapWidget(
                new NewPropertyWizard(this, "").asWidget()
        );

        window.setGlassEnabled(true);
        window.center();
    }

    @Override
    public void closePropertyDialoge() {
        window.hide();
    }
}
