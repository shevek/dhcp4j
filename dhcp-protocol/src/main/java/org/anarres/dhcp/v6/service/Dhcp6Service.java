package org.anarres.dhcp.v6.service;

import com.google.common.base.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.DHcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;

/**
 */
public interface Dhcp6Service {

    /**
     * Default DHCP client port
     */
    public static final int CLIENT_PORT = 546;
    /**
     * Default DHCP server port
     */
    public static final int SERVER_PORT = 547;

    /**
     * Retrieve the reply to a given message. The reply may be zero, if the
     * message should be ignored.
     */
    @CheckForNull
    public Optional<Dhcp6Message> getReplyFor(@Nonnull DHcp6RequestContext requestContext,
        @Nonnull Dhcp6Message request)
        throws Dhcp6Exception;
}
