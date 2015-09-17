package org.anarres.dhcp.v6.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;

import com.google.common.base.Optional;
import java.net.InetAddress;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.ServerIdOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class LeaseManagerDhcp6ServiceTest {

    @Mock
    Dhcp6LeaseManager leaseManager;

    private DuidOption.Duid serverDuid = new DuidOption.Duid(new byte[]{1,2});
    private LeaseManagerDhcp6Service leaseManagerDhcp6Service;
    private ClientIdOption clientId;
    private ServerIdOption serverId;
    private Dhcp6RequestContext requestContext;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        leaseManagerDhcp6Service = new LeaseManagerDhcp6Service(leaseManager, serverDuid);

        clientId = new ClientIdOption();
        clientId.setDuid(new byte[] { 1, 2, 3, 4 });
        serverId = new ServerIdOption();
        serverId.setDuid(serverDuid);

        requestContext = new Dhcp6RequestContext(InetAddress.getByName("fe80::a00:27ff:fe4f:7b7e"));
    }

    @Test(expected = Dhcp6Exception.InvalidMsgException.class)
    public void testSolicitInvalid() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_SOLICIT);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);
    }

    @Test(expected = Dhcp6Exception.InvalidMsgException.class)
    public void testRequestInvalid() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_REQUEST);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);
    }

    @Test
    public void testSolicit() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_SOLICIT);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);

        Mockito.verify(leaseManager).lease(same(requestContext), same(request), any(Dhcp6Message.class));
        Mockito.verify(leaseManager).requestInformation(same(requestContext), same(request), any(Dhcp6Message.class));
    }

    @Test
    public void testRequest() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_REQUEST);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        options.add(serverId);
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);

        Mockito.verify(leaseManager).lease(same(requestContext), same(request), any(Dhcp6Message.class));
        Mockito.verify(leaseManager).requestInformation(same(requestContext), same(request), any(Dhcp6Message.class));
    }

    @Test
    public void testRenew() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_RENEW);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        options.add(serverId);
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);

        Mockito.verify(leaseManager).renew(same(requestContext), same(request), any(Dhcp6Message.class));
        Mockito.verify(leaseManager).requestInformation(same(requestContext), same(request), any(Dhcp6Message.class));
    }

    @Test(expected = Dhcp6Exception.InvalidMsgException.class)
    public void testRebindInvalid() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_REBIND);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        // No server ID
        options.add(serverId);
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);
    }

    @Test
    public void testRebind() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_REBIND);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);

        Mockito.verify(leaseManager).rebind(same(requestContext), same(request), any(Dhcp6Message.class));
        Mockito.verify(leaseManager).requestInformation(same(requestContext), same(request), any(Dhcp6Message.class));
    }

    @Test
    public void testRelease() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_RELEASE);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        options.add(serverId);
        request.setOptions(options);
        leaseManagerDhcp6Service.getReplyFor(requestContext, request);

        Mockito.verify(leaseManager).release(same(requestContext), same(request), any(Dhcp6Message.class));
    }

    @Test
    public void testUnicastRefuse() throws Exception {
        final Dhcp6Message request = new Dhcp6Message();
        request.setMessageType(Dhcp6MessageType.DHCP_REQUEST);
        request.setTransactionId(22);
        final Dhcp6Options options = new Dhcp6Options();
        options.add(clientId);
        options.add(serverId);
        request.setOptions(options);
        final Optional<Dhcp6Message> rejectReply = leaseManagerDhcp6Service
            .getReplyFor(new Dhcp6RequestContext(InetAddress.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334")),
                request);

        assertEquals(Dhcp6MessageType.DHCP_REPLY, rejectReply.get().getMessageType());
        final StatusCodeOption status = rejectReply.get().getOptions().get(StatusCodeOption.class);
        assertNotNull(status);
        assertEquals(StatusCodeOption.USE_MULTICAST, status.getStatusCode());
    }

}