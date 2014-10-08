/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.pxe;

import org.apache.directory.server.dhcp.options.DhcpOption;

/**
 * These options are reserved in RFC4578 for PXE.
 *
 * @author shevek
 */
public abstract class PxeReservedOption extends DhcpOption {

    public static class PxeReserved128 extends PxeReservedOption {

        public PxeReserved128() {
            super((byte) 128);
        }
    }

    public static class PxeReserved129 extends PxeReservedOption {

        public PxeReserved129() {
            super((byte) 129);
        }
    }

    public static class PxeReserved130 extends PxeReservedOption {

        public PxeReserved130() {
            super((byte) 130);
        }
    }

    public static class PxeReserved131 extends PxeReservedOption {

        public PxeReserved131() {
            super((byte) 131);
        }
    }

    public static class PxeReserved132 extends PxeReservedOption {

        public PxeReserved132() {
            super((byte) 132);
        }
    }

    public static class PxeReserved133 extends PxeReservedOption {

        public PxeReserved133() {
            super((byte) 133);
        }
    }

    public static class PxeReserved134 extends PxeReservedOption {

        public PxeReserved134() {
            super((byte) 134);
        }
    }

    public static class PxeReserved135 extends PxeReservedOption {

        public PxeReserved135() {
            super((byte) 135);
        }
    }
    private final byte tag;

    public PxeReservedOption(byte tag) {
        this.tag = tag;
    }

    @Override
    public byte getTag() {
        return tag;
    }
}
