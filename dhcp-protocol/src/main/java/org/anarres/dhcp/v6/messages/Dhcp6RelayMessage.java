/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.messages;

import com.google.common.base.Preconditions;
import java.net.InetAddress;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.options.Dhcp6Options;

/**
 * https://tools.ietf.org/html/rfc3315#section-7
 *
 *  @author marosmars
 */
public class Dhcp6RelayMessage extends Dhcp6Message {

    private Dhcp6MessageType messageType;
    private byte hopCount;
    private InetAddress linkAddress;
    private InetAddress peerAddress;

    private Dhcp6Options options = new Dhcp6Options();

    @Nonnull
    public Dhcp6MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(@Nonnull Dhcp6MessageType messageType) {
        Preconditions.checkArgument(messageType == Dhcp6MessageType.DHCP_RELAY_FORW ||
            messageType == Dhcp6MessageType.DHCP_RELAY_REPL);
        this.messageType = messageType;
    }

    public int getTransactionId() {
        throw new UnsupportedOperationException("Relayed message has no transaction ID");
    }

    public void setTransactionId(int transactionId) {
        throw new UnsupportedOperationException("Relayed message has no transaction ID");
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(final byte hopCount) {
        this.hopCount = hopCount;
    }

    public InetAddress getLinkAddress() {
        return linkAddress;
    }

    public void setLinkAddress(final InetAddress linkAddress) {
        this.linkAddress = linkAddress;
    }

    public InetAddress getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(final InetAddress peerAddress) {
        this.peerAddress = peerAddress;
    }

    @Nonnull
    public Dhcp6Options getOptions() {
        return options;
    }

    public void setOptions(@Nonnull Dhcp6Options options) {
        this.options = options;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("Dhcp6RelayMessage{");
        sb.append("hopCount=").append(hopCount);
        sb.append(", messageType=").append(messageType);
        sb.append(", linkAddress=").append(linkAddress);
        sb.append(", peerAddress=").append(peerAddress);
        sb.append(", options=").append(options);
        sb.append('}');
        return sb.toString();
    }

    /**
     *
     * @return total length of this message (34 + options length)
     */
    public int getLength() {
        return 2 /* type + hop count */ + 16 /*link-addr*/ + 16/*peer-addr*/ + getOptions().getLength();
    }
}
