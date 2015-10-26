package org.anarres.dhcp.v6.service;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;

/**
 * DHCPv6 server side service responsible for handling DHCP client requests
 */
@Beta
public interface Dhcp6Service {

    /**
     * Default DHCP client port
     */
    public final int CLIENT_PORT = 546;
    /**
     * Default DHCP server port
     */
    public final int SERVER_PORT = 547;

    /**
     * Try to reply to client's request. Its possible to return absent response, which means no response will be
     * sent to the client
     */
    @Nonnull
    public Optional<Dhcp6Message> getReplyFor(@Nonnull Dhcp6RequestContext requestContext, @Nonnull Dhcp6Message request)
            throws Dhcp6Exception;
}
