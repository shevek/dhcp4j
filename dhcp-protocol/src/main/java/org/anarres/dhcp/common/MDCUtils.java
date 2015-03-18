/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common;

import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.slf4j.MDC;

/**
 *
 * @author shevek
 */
public class MDCUtils {

    public static final String MDC_DHCP_MESSAGE_TYPE = "dhcp.messageType";
    public static final String MDC_DHCP_CLIENT_HARDWARE_ADDRESS = "dhcp.clientHardwareAddress";
    public static final String MDC_DHCP_SERVER_INTERFACE_ADDRESS = "dhcp.serverInterfaceAddress";

    public static void init(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request) {
        MDC.put(MDC_DHCP_MESSAGE_TYPE, String.valueOf(request.getMessageType()));
        MDC.put(MDC_DHCP_CLIENT_HARDWARE_ADDRESS, String.valueOf(request.getHardwareAddress()));
        MDC.put(MDC_DHCP_SERVER_INTERFACE_ADDRESS, String.valueOf(context));
    }

    public static void fini() {
        MDC.remove(MDC_DHCP_SERVER_INTERFACE_ADDRESS);
        MDC.remove(MDC_DHCP_CLIENT_HARDWARE_ADDRESS);
        MDC.remove(MDC_DHCP_MESSAGE_TYPE);
    }

    private MDCUtils() {
    }
}
