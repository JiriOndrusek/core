package org.jboss.as.console.client.tools.modelling.workbench.repository;

import org.jboss.as.console.mbui.model.StereoTypes;
import org.jboss.as.console.mbui.model.mapping.DMRMapping;
import org.useware.kernel.model.Dialog;
import org.useware.kernel.model.mapping.Mapping;
import org.useware.kernel.model.structure.Container;
import org.useware.kernel.model.structure.InteractionUnit;
import org.useware.kernel.model.structure.QName;
import org.useware.kernel.model.structure.Select;
import org.useware.kernel.model.structure.Trigger;
import org.useware.kernel.model.structure.builder.Builder;

import static org.jboss.as.console.mbui.model.StereoTypes.*;
import static org.useware.kernel.model.structure.TemporalOperator.Choice;
import static org.useware.kernel.model.structure.TemporalOperator.Concurrency;

/**
 * @author Harald Pehl
 * @date 03/07/2013
 */
public class SecurityDomainsSample implements Sample
{
    private final Dialog dialog;

    public SecurityDomainsSample()
    {
        this.dialog = build();
    }

    @Override
    public String getName()
    {
        return "Security Domains";
    }

    @Override
    public Dialog getDialog()
    {
        return this.dialog;
    }

    private Dialog build()
    {
        String namespace = "org.jboss.security.domain";

        // Mappings
        DMRMapping securityDomainsCollection = new DMRMapping()
                .setAddress("/{selected.profile}/subsystem=security/security-domain=*");

        // maps to a specific security domain
        DMRMapping singleSecurityDomain = new DMRMapping()
                .setAddress("/{selected.profile}/subsystem=security/security-domain={selected.entity}");

        Mapping tableMapping = new DMRMapping()
                .addAttributes("entity.key");

        Mapping attributesMapping = new DMRMapping()
                .addAttributes("entity.key", "cache-type");

        // Interaction units
        InteractionUnit<StereoTypes> root = new Builder()
                .start(new Container(namespace, "securityDomains", "Security Domains", Choice, EditorPanel))
                    .mappedBy(securityDomainsCollection)

                    // The front "page"
                    .start(new Container(namespace, "availableDomains", "Available Domains", Concurrency))
                        .start(new Container(namespace, "tools", "Tools", Toolstrip))
                            .mappedBy(singleSecurityDomain)
                            .add(new Trigger(
                                    QName.valueOf(namespace + ":add"),
                                    QName.valueOf("org.jboss.as:resource-operation#add"),
                                    "Add"))
                                .mappedBy(securityDomainsCollection)
                            .add(new Trigger(
                                    QName.valueOf(namespace + ":remove"),
                                    QName.valueOf("org.jboss.as:resource-operation#remove"),
                                    "Remove"))
                        .end()

                        .add(new Select(namespace, "list", "Master"))
                            .mappedBy(tableMapping)

                        .start(new Container(namespace, "details", "Details", Choice))
                            .mappedBy(singleSecurityDomain)
                            .add(new Container(namespace, "details#attributes", "Attributes", Form))
                                .mappedBy(attributesMapping)
                        .end()
                    .end()

                    // The actual pages

                    .start(new Container(namespace, "domainConfiguration", "Domain Configuration", Concurrency))

                        .add(new Select(namespace, "domainSelection", "Select Domain", PullDown))
                            .mappedBy(tableMapping)

                        .start(new Container(namespace, "securityModules", "Security Modules", Choice, Pages))

                            // Authentication
                           .start(new Container(namespace + ".authentication", "authentication", "Authentication"))
                                .start(new Container(namespace + ".authentication", "tools", "Tools", Toolstrip))
                                    .add(new Trigger(
                                            QName.valueOf(namespace + ".authentication:add"),
                                            QName.valueOf("org.jboss.as:resource-operation#add"),
                                            "Add"))
                                    .add(new Trigger(
                                            QName.valueOf(namespace + ".authentication:remove"),
                                            QName.valueOf("org.jboss.as:resource-operation#remove"),
                                            "Remove"))
                                .end()
                                .add(new Select(namespace + ".authentication", "loginModules", "Login Modules"))
                                    .mappedBy(new DMRMapping()
                                            .setAddress("/{selected.profile}/subsystem=security/security-domain={selected.entity}/authentication=classic/login-module=*")
                                            .addAttributes("code", "flag"))

                                .start(new Container(namespace + ".authentication", "details", "Details", Choice))
                                    .add(new Container(namespace + ".authentication", "details#basicAttributers", "Attributes", Form))
                                       .mappedBy(new DMRMapping()
                                        .setAddress("/{selected.profile}/subsystem=security/security-domain={selected.entity}/authentication=classic/login-module={selected.entity}")
                                        .addAttributes("code", "flag", "module"))

                                    .add(new Select(namespace + ".authentication", "moduleOptions", "Module Options"))
                                .end()
                            .end()

                            // Authorization
                            .start(new Container(namespace + ".authorization", "authorization", "Authorization"))

                                .start(new Container<StereoTypes>(namespace + ".authorization", "tools", "Tools", Toolstrip))
                                    .add(new Trigger(
                                            QName.valueOf(namespace + ".authorization:add"),
                                            QName.valueOf("org.jboss.as:resource-operation#add"),
                                            "Add"))
                                    .add(new Trigger(
                                            QName.valueOf(namespace + ".authorization:remove"),
                                            QName.valueOf("org.jboss.as:resource-operation#remove"),
                                            "Remove"))
                                .end()
                                .add(new Select(namespace + ".authorization", "policies", "Policies"))
                                    .mappedBy(new DMRMapping()
                                            .setAddress("/{selected.profile}/subsystem=security/security-domain={selected.entity}/authorization=classic/policy-module=*")
                                            .addAttributes("code", "flag"))

                                .start(new Container(namespace + ".authorization", "details", "Details", Choice))

                                    .add(new Container(namespace + ".authorization", "details#basicAttributers", "Attributes", Form))
                                        .mappedBy(new DMRMapping()
                                                .setAddress("/{selected.profile}/subsystem=security/security-domain={selected.entity}/authorization=classic/policy-module={selected.entity}")
                                                .addAttributes("code", "flag", "module"))
                                    .add(new Select(namespace + ".authorization", "moduleOptions", "Module Options"))
                                .end()
                            .end()

                            // Mapping
                            /* .start(new Container(namespace + ".mapping", "mapping", "Mapping"))
                                .start(new Container(namespace + ".mapping", "tools", "Tools", Toolstrip))
                                    .add(new Trigger(
                                            QName.valueOf(namespace + ".mapping:add"),
                                            QName.valueOf("org.jboss.as:resource-operation#add"),
                                            "Add"))
                                    .add(new Trigger(
                                            QName.valueOf(namespace + ".mapping:remove"),
                                            QName.valueOf("org.jboss.as:resource-operation#remove"),
                                            "Remove"))
                                .end()
                                .add(new Select(namespace + ".mapping", "modules", "Modules"))
                                .start(new Container(namespace + ".mapping", "details", "Details", Choice))
                                    .add(new Container(namespace + ".mapping", "details#basicAttributers", "Attributes", Form))
                                    .add(new Container(namespace + ".mapping", "details#moduleAttributers", "Module Options", Form))
                                .end()
                            .end()*/


                        .end()

                    .end()
                .end()
                .build();

        Dialog dialog = new Dialog(QName.valueOf("org.jboss.as:security-domains"), root);
        return dialog;
    }
}
