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
package org.jboss.as.console.mbui.reification;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.as.console.client.core.UIConstants;
import org.jboss.as.console.client.shared.help.StaticHelpPanel;
import org.jboss.as.console.client.widgets.forms.FormToolStrip;
import org.jboss.as.console.mbui.JBossQNames;
import org.jboss.as.console.mbui.model.StereoTypes;
import org.jboss.as.console.mbui.model.mapping.DMRMapping;
import org.jboss.as.console.mbui.model.mapping.ResourceAttribute;
import org.jboss.as.console.mbui.widgets.ModelNodeForm;
import org.jboss.ballroom.client.rbac.SecurityContext;
import org.jboss.ballroom.client.widgets.forms.CheckBoxItem;
import org.jboss.ballroom.client.widgets.forms.ComboBoxItem;
import org.jboss.ballroom.client.widgets.forms.FormItem;
import org.jboss.ballroom.client.widgets.forms.NumberBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.ModelType;
import org.jboss.dmr.client.Property;
import org.useware.kernel.gui.behaviour.InteractionEvent;
import org.useware.kernel.gui.behaviour.PresentationEvent;
import org.useware.kernel.gui.behaviour.SystemEvent;
import org.useware.kernel.gui.behaviour.common.CommonQNames;
import org.useware.kernel.gui.reification.Context;
import org.useware.kernel.gui.reification.ContextKey;
import org.useware.kernel.gui.reification.strategy.ReificationStrategy;
import org.useware.kernel.gui.reification.strategy.ReificationWidget;
import org.useware.kernel.model.behaviour.Resource;
import org.useware.kernel.model.behaviour.ResourceType;
import org.useware.kernel.model.mapping.MappingType;
import org.useware.kernel.model.mapping.Predicate;
import org.useware.kernel.model.structure.InteractionUnit;
import org.useware.kernel.model.structure.QName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.useware.kernel.gui.behaviour.common.CommonQNames.RESET_ID;
import static org.useware.kernel.model.behaviour.ResourceType.*;
import static org.useware.kernel.model.behaviour.ResourceType.System;

/**
 * @author Harald Pehl
 * @author Heiko Braun
 * @date 11/01/2012
 */
public class FormStrategy implements ReificationStrategy<ReificationWidget, StereoTypes>
{

    private ModelNode modelDescription;
    private EventBus eventBus;
    private static final Resource<ResourceType> SAVE_EVENT = new Resource<ResourceType>(JBossQNames.SAVE_ID, Interaction);
    private static final Resource<ResourceType> LOAD_EVENT = new Resource<ResourceType>(JBossQNames.LOAD_ID, Interaction);
    private static final Resource<ResourceType> RESET = new Resource<ResourceType>(RESET_ID, System);
    private SecurityContext securityContext;

    @Override
    public boolean prepare(InteractionUnit<StereoTypes> interactionUnit, Context context) {
        Map<QName, ModelNode> descriptions = context.get (ContextKey.MODEL_DESCRIPTIONS);

         // TODO (BUG): After the first reification the behaviour is modified,
        // so the predicate might apply to a different unit. As a result the correlation id is different!

        QName correlationId = interactionUnit.findMapping(MappingType.DMR, new Predicate<DMRMapping>() {
            @Override
            public boolean appliesTo(DMRMapping candidate) {
                return candidate.getAddress()!=null;
            }
        }).getCorrelationId();
        modelDescription = descriptions.get(correlationId);

        eventBus = context.get(ContextKey.EVENTBUS);
        securityContext = context.get(ContextKey.SECURITY_CONTEXT);

        return eventBus!=null
                && modelDescription!=null
                && securityContext!=null;
    }

    @Override
    public ReificationWidget reify(final InteractionUnit<StereoTypes> interactionUnit, final Context context)
    {
        return new FormAdapter(interactionUnit, eventBus, modelDescription);
    }

    @Override
    public boolean appliesTo(final InteractionUnit<StereoTypes> interactionUnit)
    {
        return StereoTypes.Form == interactionUnit.getStereotype();
    }

    class FormAdapter implements ReificationWidget
    {
        final ModelNodeForm form;
        final InteractionUnit interactionUnit;
        private SafeHtmlBuilder helpTexts;
        private EventBus coordinator;

        FormAdapter(final InteractionUnit<StereoTypes> interactionUnit, EventBus coordinator, final ModelNode modelDescription)
        {
            this.interactionUnit = interactionUnit;
            this.coordinator = coordinator;

            DMRMapping dmrMapping = (DMRMapping)
                    this.interactionUnit.findMapping(MappingType.DMR);

            this.form = new ModelNodeForm(dmrMapping.getAddress(), securityContext);
            this.form.setNumColumns(2);
            this.form.setEnabled(false);

            assert modelDescription.hasDefined("attributes") : "Invalid model description. Expected child 'attributes'";

            List<Property> attributeDescriptions = modelDescription.get("attributes").asPropertyList();


            List<ResourceAttribute> attributes = dmrMapping.getAttributes();

            // catch-all directive, if no explicit attributes given
            // TODO: optimisation, see below
            if(attributes.isEmpty())
            {
                for(Property attr : attributeDescriptions)
                {
                    attributes.add(new ResourceAttribute(attr.getName(), attr.getName()));
                }
            }


            List<FormItem> items = new ArrayList<FormItem>(attributes.size());

            helpTexts = new SafeHtmlBuilder();
            helpTexts.appendHtmlConstant("<table class='help-attribute-descriptions'>");

            for (ResourceAttribute attribute : attributes)
            {
                for(Property attr : attributeDescriptions)
                {
                    if(!attr.getName().equals(attribute.getName()))
                        continue;


                    char[] stringArray = attr.getName().toCharArray();
                    stringArray[0] = Character.toUpperCase(stringArray[0]);

                    String label = new String(stringArray).replace("-", " ");
                    ModelNode attrValue = attr.getValue();

                    // help
                    helpTexts.appendHtmlConstant("<tr class='help-field-row'>");
                    helpTexts.appendHtmlConstant("<td class='help-field-name'>");
                    helpTexts.appendEscaped(label).appendEscaped(": ");
                    helpTexts.appendHtmlConstant("</td>");
                    helpTexts.appendHtmlConstant("<td class='help-field-desc'>");
                    try {
                        String descWorkaround = attrValue.get("description").asString();

                        helpTexts.appendHtmlConstant(descWorkaround.equals("null") ? "n/a" : descWorkaround);
                    } catch (Throwable e) {
                        // ignore parse errors
                        helpTexts.appendHtmlConstant("<i>" + ((UIConstants) GWT.create(UIConstants.class))
                                .failedToParseDescription() + "</i>");
                    }
                    helpTexts.appendHtmlConstant("</td>");
                    helpTexts.appendHtmlConstant("</tr>");

                    boolean required = !attr.getValue().get("nillable").asBoolean();
                    ModelType type = ModelType.valueOf(attrValue.get("type").asString());
                    //System.out.println(attr.getName()+">"+type);
                    switch(type)
                    {
                        case BOOLEAN:
                            CheckBoxItem checkBoxItem = new CheckBoxItem(attr.getName(), label);
                            items.add(checkBoxItem);
                            break;
                        case DOUBLE:
                            NumberBoxItem num = new NumberBoxItem(attr.getName(), label);
                            num.setRequired(required);
                            items.add(num);
                            break;
                        case LONG:
                            NumberBoxItem num2 = new NumberBoxItem(attr.getName(), label);
                            num2.setRequired(required);
                            items.add(num2);
                            break;
                        case INT:
                            NumberBoxItem num3 = new NumberBoxItem(attr.getName(), label);
                            num3.setRequired(required);
                            items.add(num3);
                            break;
                        case STRING:
                            if(attrValue.get("allowed").isDefined())
                            {
                                List<ModelNode> allowed = attrValue.get("allowed").asList();
                                Set<String> allowedValues = new HashSet<String>(allowed.size());
                                for(ModelNode value : allowed)
                                    allowedValues.add(value.asString());

                                ComboBoxItem combo = new ComboBoxItem(attr.getName(), label);
                                combo.setValueMap(allowedValues);
                            }
                            else
                            {
                                TextBoxItem tb = new TextBoxItem(attr.getName(), label);
                                tb.setRequired(required);
                                items.add(tb);
                            }
                            break;
                        default:
                            Log.debug("Unsupported ModelType " + type);
                    }

                }
            }

            helpTexts.appendHtmlConstant("</table>");

            form.setFields(items.toArray(new FormItem[]{}));

        }

        @Override
        public InteractionUnit getInteractionUnit() {
            return interactionUnit;
        }

        @Override
        public void add(final ReificationWidget widget)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Widget asWidget()
        {

            VerticalPanel layout = new VerticalPanel();
            layout.setStyleName("fill-layout-width");
            layout.getElement().setAttribute("style", "margin-top:15px;margin-bottom:15px;");

            final FormToolStrip<ModelNode> tools = new FormToolStrip<ModelNode>(
                    form,
                    new FormToolStrip.FormCallback<ModelNode>() {
                        @Override
                        public void onSave(Map<String, Object> changeset) {

                            InteractionEvent saveEvent = new InteractionEvent(JBossQNames.SAVE_ID);
                            saveEvent.setPayload(form.getChangedValues());

                            coordinator.fireEventFromSource(
                                    saveEvent,
                                    interactionUnit.getId()
                            );
                        }

                        @Override
                        public void onDelete(ModelNode entity) {
                            // unsupported
                        }
                    });

            StaticHelpPanel help = new StaticHelpPanel(helpTexts.toSafeHtml());

            layout.add(tools.asWidget());
            layout.add(help.asWidget());
            layout.add(form.asWidget());

            // handle resets within this scope
            coordinator.addHandler(SystemEvent.TYPE, new SystemEvent.Handler() {
                @Override
                public boolean accepts(SystemEvent event) {
                    return event.getId().equals(CommonQNames.RESET_ID) ;
                }

                @Override
                public void onSystemEvent(SystemEvent event) {
                    form.clearValues();

                    tools.doCancel();

                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            // request loading of data
                            InteractionEvent loadEvent = new InteractionEvent(JBossQNames.LOAD_ID);

                            // update interaction units
                            coordinator.fireEventFromSource(
                                    loadEvent,
                                    interactionUnit.getId()
                            );
                        }
                    });
                }
            });


            // handle the results of function calls
            coordinator.addHandler(PresentationEvent.TYPE, new PresentationEvent.PresentationHandler()
            {
                @Override
                public boolean accepts(PresentationEvent event) {
                    boolean matchingId = event.getTarget().equals(getInteractionUnit().getId());

                    // only single resources accepted (might be collection, see LoadResourceProcedure)
                    boolean payloadMatches = event.getPayload() instanceof ModelNode;

                    return matchingId && payloadMatches;
                }

                @Override
                public void onPresentationEvent(PresentationEvent event) {

                    assert (event.getPayload() instanceof ModelNode) : "Unexpected type "+event.getPayload().getClass();
                    ModelNode payload = (ModelNode) event.getPayload();

                    form.edit(payload);
                }
            });


            // Register inputs and outputs

            Resource<ResourceType> update = new Resource<ResourceType>(getInteractionUnit().getId(), Presentation);

            getInteractionUnit().setOutputs(SAVE_EVENT, LOAD_EVENT);
            getInteractionUnit().setInputs(RESET, update);

            return layout;
        }
    }
}
