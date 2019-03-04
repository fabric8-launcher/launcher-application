package io.fabric8.launcher.base.identity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
class IdentityVisitorTest {

    @Test
    void testTokenIdentity() {
        final AtomicBoolean test = new AtomicBoolean();
        TokenIdentity identity = TokenIdentity.of("FOO");
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                test.set(true);
            }
        });
        Assert.assertTrue(test.get());
    }

    @Test
    void testUserPasswordIdentity() {
        final AtomicBoolean test = new AtomicBoolean();
        UserPasswordIdentity identity = ImmutableUserPasswordIdentity.of("USER", "PASS");
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(UserPasswordIdentity userPasswordIdentity) {
                test.set(true);
            }
        });
        Assert.assertTrue(test.get());
    }

    @Test
    void testTokenIdentityNotSupported() {
        assertThrows(UnsupportedOperationException.class, () -> {
            TokenIdentity identity = TokenIdentity.of("FOO");
            identity.accept(new IdentityVisitor() {
                @Override
                public void visit(UserPasswordIdentity userPassword) {
                    fail("visit(UserPasswordIdentity) should have never been called");
                }
            });
        });
    }

    @Test
    void testUserPasswordIdentityNotSupported() {
        assertThrows(UnsupportedOperationException.class, () -> {
            UserPasswordIdentity identity = ImmutableUserPasswordIdentity.of("USER", "PASS");
            identity.accept(new IdentityVisitor() {
                @Override
                public void visit(TokenIdentity tokenIdentity) {
                    fail("visit(TokenIdentity) should have never been called");
                }
            });
        });
    }
}