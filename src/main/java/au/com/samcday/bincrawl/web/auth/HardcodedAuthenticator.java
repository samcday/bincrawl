package au.com.samcday.bincrawl.web.auth;

import au.com.samcday.bincrawl.web.entities.Admin;
import com.google.common.base.Optional;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;

/**
 * Temporary authenticator
 */
public class HardcodedAuthenticator implements Authenticator<BasicCredentials, Admin> {
    private static final Admin ADMIN = new Admin() {{ this.setUsername("admin"); }};

    @Override
    public Optional<Admin> authenticate(BasicCredentials basicCredentials) throws AuthenticationException {
        if("admin".equals(basicCredentials.getUsername()) && "admin".equals(basicCredentials.getPassword())) {
            return Optional.of(ADMIN);
        }

        return Optional.absent();
    }
}
