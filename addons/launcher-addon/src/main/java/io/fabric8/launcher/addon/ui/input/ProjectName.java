/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.fabric8.launcher.addon.ui.input;

import java.util.regex.Pattern;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.jboss.forge.addon.ui.input.AbstractUIInputDecorator;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;

/**
 * The project name input
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Typed(ProjectName.class)
@Deprecated
public class ProjectName extends AbstractUIInputDecorator<String> {
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[-a-z0-9]|[a-z0-9][-a-z0-9]*[a-z0-9]");

    @Inject
    @WithAttributes(label = "OpenShift Project name", required = true)
    @UnwrapValidatedValue
    @Length(min = 2, max = 63)
    private UIInput<String> named;

    @Override
    protected UIInput<String> createDelegate() {
        named.addValidator(context -> {
            if (named.getValue() != null
                    && !SPECIAL_CHARS.matcher(named.getValue()).matches())
                context.addValidationError(named,
                                           "Project name must not contain spaces or special characters.");
        }).setDescription("The following characters are accepted: -a-z0-9 and the name cannot start or end with a dash");
        return named;
    }

}
