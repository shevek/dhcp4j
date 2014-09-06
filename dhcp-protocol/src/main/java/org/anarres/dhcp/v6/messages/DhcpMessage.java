/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.messages;

import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.options.DhcpOptions;

/**
 *
 * @author shevek
 */
public class DhcpMessage {

    private DhcpMessageType messageType;
    private int transactionId;
    private DhcpOptions options = new DhcpOptions();

    @Nonnull
    public DhcpMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(@Nonnull DhcpMessageType messageType) {
        this.messageType = messageType;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    @Nonnull
    public DhcpOptions getOptions() {
        return options;
    }

    public void setOptions(@Nonnull DhcpOptions options) {
        this.options = options;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(messageType).append(": tx=").append(transactionId).append(", options=").append(options);
        return sb.toString();
    }
}
