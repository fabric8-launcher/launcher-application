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

import org.jboss.forge.addon.parser.java.ui.validators.PackageUIValidator;
import org.jboss.forge.addon.ui.input.AbstractUIInputDecorator;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;

/**
 * The project top level package
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Typed(TopLevelPackage.class)
@Deprecated
public class TopLevelPackage extends AbstractUIInputDecorator<String> {
    private static final Pattern SPECIAL_CHARS = Pattern.compile(".*[^-_.a-zA-Z0-9].*");

    @Inject
    @WithAttributes(label = "Top level package", required = true, defaultValue = "com.example")
    private UIInput<String> topLevelPackage;

    @Override
    protected UIInput<String> createDelegate() {
        topLevelPackage.addValidator(new PackageUIValidator()).addValidator(context -> {
            if (topLevelPackage.getValue() != null
                    && SPECIAL_CHARS.matcher(topLevelPackage.getValue()).matches()) {
                context.addValidationError(topLevelPackage,
                                           "Top level package must not contain spaces or special characters.");
            }

        }).setDescription("The following characters are accepted: -_.a-zA-Z0-9");
        return topLevelPackage;
    }
}
