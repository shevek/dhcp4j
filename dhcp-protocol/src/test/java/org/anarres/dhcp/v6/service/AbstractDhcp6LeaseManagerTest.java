package org.anarres.dhcp.v6.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.IaNaOption;
import org.anarres.dhcp.v6.options.ServerIdOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.junit.Before;
import org.junit.Test;

/**
 * A base class for lease managers tests.
 *
 * @author marekgr
 *
 */
public abstract class AbstractDhcp6LeaseManagerTest {

    protected ClientIdOption clientId;
    protected ServerIdOption serverId;
    protected Dhcp6LeaseManager leaseManager;
    protected Dhcp6RequestContext requestContext;
    protected InetAddress localHost;

    @Before
    public void setUp() throws Exception {
        leaseManager = getLeaseManagerInstance();
        clientId = new ClientIdOption();
        clientId.setDuid(new byte[] { 1, 2, 3, 4 });
        serverId = new ServerIdOption();
        serverId.setDuid(new byte[] { 1, 2 });
        localHost = InetAddress.getLocalHost();
        requestContext = new Dhcp6RequestContext(localHost);
    }

    protected abstract Dhcp6LeaseManager getLeaseManagerInstance() throws UnknownHostException;

    protected <T extends Dhcp6Option> void assertSingle(@Nonnull Dhcp6Options options, @Nonnull final Class<T> type) {
        assertEquals(1, Iterables.size(options.getAll(type)));
    }

    protected void assertStatusEquals(@Nonnull Dhcp6Options options, short statusCode) {
        assertEquals(1, Iterables.size(options.getAll(StatusCodeOption.class)));
        assertEquals(statusCode, options.get(StatusCodeOption.class).getStatusCode());
    }

    protected <T extends Dhcp6Option> void assertAppearedNTimes(Dhcp6Options options, @Nonnull final Class<T> type, final int N) {
        assertEquals(N, Iterables.size(options.getAll(type)));
    }

    protected Dhcp6Message createDhcp6Message(@Nonnull Dhcp6MessageType messageType, Dhcp6Option... options) {
        Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(messageType);

        Dhcp6Options o = new Dhcp6Options();
        o.addAll(Arrays.asList(options));
        request.setOptions(o);
        return request;
    }

    @Test
    public void testReleaseResponseOptionsBinding() throws Dhcp6Exception {
        IaNaOption iaNa1 = IaNaOption.create(1, 2, 3, Optional.of(new Dhcp6Options()));
        Dhcp6Message request = createDhcp6Message(Dhcp6MessageType.DHCP_REQUEST, clientId, iaNa1);

        Dhcp6Message response = leaseManager.lease(requestContext, request, new Dhcp6Message());
        Dhcp6Options responseOptions = response.getOptions();

        final IaNaOption leasedIAOption = responseOptions.get(IaNaOption.class);

        request = createDhcp6Message(Dhcp6MessageType.DHCP_RELEASE, clientId, serverId, leasedIAOption);
        response = leaseManager.release(requestContext, request, new Dhcp6Message());

        responseOptions = response.getOptions();
        assertStatusEquals(response.getOptions(), StatusCodeOption.SUCCESS);
    }

    @Test
    public void testReleaseResponseOptionsWithoutBinding() throws Dhcp6Exception {
        IaNaOption iaNa1 = IaNaOption.create(123, 0, 0, Optional.of(new Dhcp6Options()));
        Dhcp6Message request = createDhcp6Message(Dhcp6MessageType.DHCP_REQUEST, clientId, iaNa1);
        leaseManager.lease(requestContext, request, new Dhcp6Message());

        final int iaId2 = 456;
        final IaNaOption iaNa2 = IaNaOption.create(iaId2, 0, 0, Optional.of(new Dhcp6Options()));
        request = createDhcp6Message(Dhcp6MessageType.DHCP_RELEASE, clientId, serverId, iaNa2);
        Dhcp6Message response = leaseManager.release(requestContext, request, new Dhcp6Message());

        Dhcp6Options responseOptions = response.getOptions();
        assertStatusEquals(response.getOptions(), StatusCodeOption.SUCCESS);

        final IaNaOption iaNaOption = responseOptions.get(IaNaOption.class);
        assertNotNull(iaNaOption);
        assertEquals(iaId2, iaNaOption.getIAID());
        assertStatusEquals(iaNaOption.getOptions(), StatusCodeOption.NO_BINDING);
    }

}
