/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.messages;

import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.options.Dhcp6Options;

/**
 *
 * @author shevek
 */
public class Dhcp6Message {

    private Dhcp6MessageType messageType;
    private int transactionId;
    private Dhcp6Options options = new Dhcp6Options();

    @Nonnull
    public Dhcp6MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(@Nonnull Dhcp6MessageType messageType) {
        this.messageType = messageType;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    @Nonnull
    public Dhcp6Options getOptions() {
        return options;
    }

    public void setOptions(@Nonnull Dhcp6Options options) {
        this.options = options;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(messageType).append(": tx=").append(transactionId).append(", options=").append(options);
        return sb.toString();
    }


}
