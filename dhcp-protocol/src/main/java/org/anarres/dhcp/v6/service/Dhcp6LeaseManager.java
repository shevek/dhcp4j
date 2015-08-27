package org.anarres.dhcp.v6.service;

import org.anarres.dhcp.v6.messages.Dhcp6Message;

public interface Dhcp6LeaseManager {

    Dhcp6Message lease(Dhcp6Message incomingMsg, Dhcp6Message reply);

    Dhcp6Message release(Dhcp6Message options, Dhcp6Message reply);

}
