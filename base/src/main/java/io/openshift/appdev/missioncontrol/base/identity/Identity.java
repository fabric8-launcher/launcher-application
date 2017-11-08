package io.openshift.appdev.missioncontrol.base.identity;

import java.util.function.Consumer;

/**
 * Represents an identity used by authentication engines.
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public interface Identity extends Consumer<IdentityVisitor> {
}