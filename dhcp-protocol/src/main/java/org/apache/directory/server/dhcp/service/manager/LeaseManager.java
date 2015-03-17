/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import java.net.InetAddress;
import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;

/**
 *
 * @author shevek
 */
public interface LeaseManager {

    /**
     * Determine a lease to offer in response to a DHCPDISCOVER message.
     * <p>
     * When a server receives a DHCPDISCOVER message from a client, the server
     * chooses a network address for the requesting client. If no address is
     * available, the server may choose to report the problem to the system
     * administrator. If an address is available, the new address SHOULD be
     * chosen as follows:
     * <ul>
     * <li> The client's current address as recorded in the client's current
     * binding, ELSE
     * <li> The client's previous address as recorded in the client's (now
     * expired or released) binding, if that address is in the server's pool of
     * available addresses and not already allocated, ELSE
     * <li> The address requested in the 'Requested IP Address' option, if that
     * address is valid and not already allocated, ELSE
     * <li> A new address allocated from the server's pool of available
     * addresses; the address is selected based on the subnet from which the
     * message was received (if 'giaddr' is 0) or on the address of the relay
     * agent that forwarded the message ('giaddr' when not 0).
     * </ul>
     */
    @CheckForNull
    public DhcpMessage leaseOffer(@Nonnull InterfaceAddress[] localAddresses,
            @Nonnull DhcpMessage request,
            @CheckForNull InetAddress clientRequestedAddress, @CheckForSigned long clientRequestedExpirySecs)
            throws DhcpException;

    @CheckForNull
    public DhcpMessage leaseRequest(@Nonnull InterfaceAddress[] localAddresses,
            @Nonnull DhcpMessage request,
            @Nonnull InetAddress clientRequestedAddress, @CheckForSigned long clientRequestedExpirySecs)
            throws DhcpException;

    public boolean leaseDecline(@Nonnull InterfaceAddress[] localAddresses,
            @Nonnull DhcpMessage request,
            @Nonnull InetAddress clientAddress)
            throws DhcpException;

    public boolean leaseRelease(@Nonnull InterfaceAddress[] localAddresses,
            @Nonnull DhcpMessage request,
            @Nonnull InetAddress clientAddress)
            throws DhcpException;

}
