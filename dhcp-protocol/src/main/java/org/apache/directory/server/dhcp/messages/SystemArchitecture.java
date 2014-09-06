/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.messages;

import javax.annotation.Nonnull;

/**
 * https://tools.ietf.org/html/rfc4578
 * @author shevek
 */
public enum SystemArchitecture {

    Intel_x86PC(0, "Intel x86PC"),
    NEC_PC98(1, "NEC/PC98"),
    EFI_Itanium(2, "EFI Itanium"),
    DEC_Alpha(3, "DEC Alpha"),
    Arc_x86(4, "Arc x86"),
    Intel_Lean_Client(5, "Intel Lean Client"),
    EFI_IA32(6, "EFI IA32"),
    EFI_BC(7, "EFI BC"),
    EFI_Xscale(8, "EFI Xscale"),
    EFI_x86_64(9, "EFI x86-64"),
    UNKNOWN(-1, "Unknown");
    private final int id;
    private final String description;
    /* pp */ SystemArchitecture(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + "(" + getId() + ": " + getDescription() + ")";
    }

    @Nonnull
    public static SystemArchitecture forTypeCode(int code) {
        for (SystemArchitecture type : SystemArchitecture.values())
            if (type.getId() == code)
                return type;
        return UNKNOWN;
    }
}
