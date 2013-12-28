/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.messages;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * https://www.iana.org/assignments/arp-parameters/arp-parameters.xhtml#arp-parameters-2
 *
 * @author shevek
 */
public enum HardwareAddressType {

    Reserved(0, "Reserved"),
    Ethernet(1, "Ethernet (10Mb)"),
    ExperimentalEthernet(2, "Experimental Ethernet (3Mb)"),
    AX25(3, "Amateur Radio AX.25"),
    Proteon(4, "Proteon ProNET Token Ring"),
    Chaos(5, "Chaos"),
    IEEE802(6, "IEEE 802 Networks"),
    ARCNET(7, "ARCNET"),
    Hyperchannel(8, "Hyperchannel"),
    Lanstar(9, "Lanstar"),
    Autonet(10, "Autonet Short Address"),
    LocalTalk(11, "LocalTalk"),
    LocalNet(12, "LocalNet (IBM PCNet or SYTEK LocalNET)"),
    Ultralink(13, "Ultra link"),
    SMDS(14, "SMDS"),
    FrameRelay(15, "Frame Relay"),
    ATM_0(16, "Asynchronous Transmission Mode (ATM)"),
    HDLC(17, "HDLC"),
    FibreChannel(18, "Fibre Channel"),
    ATM_1(19, "Asynchronous Transmission Mode (ATM)"),
    SerialLine(20, "Serial Line"),
    ATM_2(21, "Asynchronous Transmission Mode (ATM)"),
    MIL_STD_188_220(22, "MIL-STD-188-220"),
    Metricom(23, "Metricom"),
    IEEE1394(24, "IEEE 1394.1995"),
    MAPOS(25, "MAPOS"),
    Twinaxial(26, "Twinaxial"),
    EUI64(27, "EUI-64"),
    HIPARP(28, "HIPARP"),
    IPoverISO7816_3(29, "IP and ARP over ISO 7816-3"),
    ARPSec(30, "ARPSec"),
    IPsecTunnel(31, "IPsec tunnel"),
    InfiniBand(32, "InfiniBand (TM)"),
    TIA102(33, "TIA-102 Project 25 Common Air Interface (CAI)"),
    Wiegand(34, "Wiegand Interface"),
    PureIP(35, "Pure IP"),
    HW_EXP1(36, "HW_EXP1"),
    HFI(37, "HFI");

    private final short code;
    private final String description;

    private HardwareAddressType(int code, String description) {
        this.code = (short) code;
        this.description = description;
    }

    @Nonnegative
    public short getCode() {
        return code;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + "(" + getCode() + ": " + getDescription() + ")";
    }

    @Nonnull
    public static HardwareAddressType forCode(int code) {
        for (HardwareAddressType type : HardwareAddressType.values())
            if (type.code == code)
                return type;
        throw new IllegalArgumentException("No such hardware type " + code + ". Check "
                + "https://www.iana.org/assignments/arp-parameters/arp-parameters.xhtml#arp-parameters-2");
    }
}
