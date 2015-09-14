package org.anarres.dhcp.v6;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.IaNaOption;
import org.anarres.dhcp.v6.options.IaTaOption;
import org.anarres.dhcp.v6.options.RelayMessageOption;
import org.anarres.dhcp.v6.options.ServerIdOption;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * Created by marosmars
 */
@Beta
public class Dhcp6Exception extends DhcpException {

    public Dhcp6Exception() {
        super();
    }

    public Dhcp6Exception(@Nonnull final String description) {
        super(description);
    }

    public Dhcp6Exception(@Nonnull final String description, @Nonnull final Exception e) {
        super(description, e);
    }

    public static final class UnknownMsgException extends Dhcp6Exception {

        private byte msgType;

        public UnknownMsgException(byte msgType) {
            super("Unrecognised msg type " + msgType);
            this.msgType = msgType;
        }

        public byte getMsgType() {
            return msgType;
        }

        /**
         *
         * Check if the message type is equal to DHCP_UNRECOGNIZED
         *
         * @throws UnknownMsgException in case the message is of unknown type
         */
        public static void check(@Nonnull final Dhcp6MessageType dhcp6MessageType, final byte type) throws UnknownMsgException {
            if(dhcp6MessageType == Dhcp6MessageType.DHCP_UNRECOGNIZED) {
                throw new UnknownMsgException(type);
            }
        }
    }

    public static final class InvalidMsgException extends Dhcp6Exception {

        public InvalidMsgException(@Nonnull final String message) {
            super(message);
        }

        public InvalidMsgException(@Nonnull final Dhcp6MessageType msgType) {
            this("Invalid DHCP message: " + msgType);
        }

        /**
         * Check parsed DHCP_SOLICIT message for correct type and options
         *
         * https://tools.ietf.org/html/rfc3315#section-15.2
         *
         * @throws org.anarres.dhcp.v6.Dhcp6Exception.InvalidMsgException in case the message is invalid and should be discarded
         */
        public static void checkSolicit(@Nonnull final Dhcp6Message msg) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_SOLICIT);
            checkOption(msg, ClientIdOption.class);
            if(hasOption(msg, ServerIdOption.class)) {
                throw new InvalidMsgException(String.format("Unexpected option: %s", ServerIdOption.class));
            }
        }

        private static void checkOption(@Nonnull final Dhcp6Message msg, @Nonnull final Class<? extends Dhcp6Option> type) throws InvalidMsgException {
            if(noOption(msg, type)) {
                throw new InvalidMsgException(String.format("Missing option: %s", type));
            }
        }

        private static void checkNoOption(@Nonnull final Dhcp6Message msg, @Nonnull final Class<? extends Dhcp6Option> type) throws InvalidMsgException {
            if(!noOption(msg, type)) {
                throw new InvalidMsgException(String.format("Unexpected option: %s", type));
            }
        }

        private static void checkMsgType(@Nonnull final Dhcp6Message msg, @Nonnull final Dhcp6MessageType expected) throws InvalidMsgException {
            if(!isMsgType(msg, expected)) {
                throw new InvalidMsgException(String.format("Incorrect type, expected: %s, but was: %s", expected, msg.getMessageType()));
            }
        }

        private static boolean noOption(@Nonnull final Dhcp6Message msg, @Nonnull final Class<? extends Dhcp6Option> type) {
            return msg.getOptions().get(type) == null;
        }

        private static boolean hasOption(@Nonnull final Dhcp6Message msg, @Nonnull final Class<? extends Dhcp6Option> type) {
            return msg.getOptions().get(type) != null;
        }

        private static boolean isMsgType(@Nonnull final Dhcp6Message msg, @Nonnull final Dhcp6MessageType type) {
            return msg.getMessageType() == type;
        }

        /**
         * Check parsed DHCP_REQUEST message for correct type and options
         *
         * https://tools.ietf.org/html/rfc3315#section-15.4
         *
         * @throws org.anarres.dhcp.v6.Dhcp6Exception.InvalidMsgException in case the message is invalid and should be discarded
         */
        public static void checkRequest(@Nonnull final Dhcp6Message msg, @Nonnull final DuidOption.Duid duid) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_REQUEST);
            checkOption(msg, ServerIdOption.class);
            checkOptionValue(msg, ServerIdOption.class, duid.getData());
            checkOption(msg, ClientIdOption.class);
        }

        private static void checkOptionValue(@Nonnull final Dhcp6Message msg,
            @Nonnull final Class<? extends Dhcp6Option> optionType, @Nonnull final byte[] expectedValue)
            throws InvalidMsgException {
            if (!isOptionValue(msg, optionType, expectedValue)) {
                throw new InvalidMsgException(String
                    .format("Unexpected option value: %s, should be: %s", ServerIdOption.class,
                        Arrays.toString(expectedValue)));
            }
        }

        private static boolean isOptionValue(@Nonnull final Dhcp6Message msg, @Nonnull final Class<? extends Dhcp6Option> optionType,
            @Nonnull final byte[] expectedValue) {
            final Dhcp6Option dhcp6Option = Preconditions.checkNotNull(msg.getOptions().get(optionType));
            return Arrays.equals(dhcp6Option.getData(), expectedValue);
        }

        /**
         * Check parsed DHCP_REQUEST message for correct type and options
         *
         * https://tools.ietf.org/html/rfc3315#section-15.9
         *
         * @throws org.anarres.dhcp.v6.Dhcp6Exception.InvalidMsgException in case the message is invalid and should be discarded
         */
        public static void checkRelease(final Dhcp6Message msg, final DuidOption.Duid duid) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_RELEASE);
            checkOption(msg, ServerIdOption.class);
            checkOptionValue(msg, ServerIdOption.class, duid.getData());
            checkOption(msg, ClientIdOption.class);
        }

        /**
         * https://tools.ietf.org/html/rfc3315#section-7.1
         */
        public static void checkRelayForward(@Nonnull final Dhcp6Message msg) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_RELAY_FORW);
            checkOption(msg, RelayMessageOption.class);
        }

        /**
         *
         * https://tools.ietf.org/html/rfc3315#section-15.12
         */
        public static void checkInformationRequest(final Dhcp6Message msg, final DuidOption.Duid duid)
            throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_INFORMATION_REQUEST);
            if(msg.getOptions().contains(ServerIdOption.class)) {
                checkOptionValue(msg, ServerIdOption.class, duid.getData());
            }
            checkNoOption(msg, IaNaOption.class);
            checkNoOption(msg, IaTaOption.class);
        }

        /**
         * https://tools.ietf.org/html/rfc3315#section-15.5
         */
        public static void checkConfirm(final Dhcp6Message msg) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_CONFIRM);
            checkOption(msg, ClientIdOption.class);
            checkNoOption(msg, ServerIdOption.class);
        }

        /**
         * https://tools.ietf.org/html/rfc3315#section-15.7
         */
        public static void checkRebind(final Dhcp6Message msg) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_REBIND);
            checkNoOption(msg, ServerIdOption.class);
            checkOption(msg, ClientIdOption.class);
        }

        /**
         * https://tools.ietf.org/html/rfc3315#section-15.6
         */
        public static void checkRenew(final Dhcp6Message msg, final DuidOption.Duid duid) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_RENEW);
            checkOption(msg, ServerIdOption.class);
            checkOptionValue(msg, ServerIdOption.class, duid.getData());
            checkOption(msg, ClientIdOption.class);
        }

        /**
         * https://tools.ietf.org/html/rfc3315#section-15.8
         */
        public static void checkDecline(final Dhcp6Message msg, final DuidOption.Duid duid) throws InvalidMsgException {
            checkMsgType(msg, Dhcp6MessageType.DHCP_RENEW);
            checkOption(msg, ServerIdOption.class);
            checkOptionValue(msg, ServerIdOption.class, duid.getData());
            checkOption(msg, ClientIdOption.class);
        }
    }

    /**
     * Exception indicating the inability of replying to some DHCP client message
     */
    public static final class UnableToAnswerException extends Dhcp6Exception {

        public UnableToAnswerException() {
            super();
        }

        public UnableToAnswerException(@Nonnull final String description) {
            super(description);
        }

        public UnableToAnswerException(@Nonnull final String description, @Nonnull final Exception e) {
            super(description, e);
        }
    }
}
