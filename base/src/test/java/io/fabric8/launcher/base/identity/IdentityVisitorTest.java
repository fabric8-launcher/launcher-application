package io.fabric8.launcher.base.identity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.base.identity.IdentityVisitor;
import io.fabric8.launcher.base.identity.TokenIdentity;
import io.fabric8.launcher.base.identity.UserPasswordIdentity;

import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class IdentityVisitorTest {

    @Test
    public void testTokenIdentity() {
        final AtomicBoolean test = new AtomicBoolean();
        TokenIdentity identity = IdentityFactory.createFromToken("FOO");
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity token) {
                test.set(true);
            }
        });
        Assert.assertTrue(test.get());
    }

    @Test
    public void testUserPasswordIdentity() {
        final AtomicBoolean test = new AtomicBoolean();
        UserPasswordIdentity identity = IdentityFactory.createFromUserPassword("USER", "PASS");
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(UserPasswordIdentity userPasswordIdentity) {
                test.set(true);
            }
        });
        Assert.assertTrue(test.get());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTokenIdentityNotSupported() {
        TokenIdentity identity = IdentityFactory.createFromToken("FOO");
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(UserPasswordIdentity userPassword) {
                fail("visit(UserPasswordIdentity) should have never been called");
            }
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUserPasswordIdentityNotSupported() {
        UserPasswordIdentity identity = IdentityFactory.createFromUserPassword("USER", "PASS");
        identity.accept(new IdentityVisitor() {
            @Override
            public void visit(TokenIdentity tokenIdentity) {
                fail("visit(TokenIdentity) should have never been called");
            }
        });
    }
}