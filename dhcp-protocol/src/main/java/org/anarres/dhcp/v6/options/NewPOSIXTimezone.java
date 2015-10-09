package org.anarres.dhcp.v6.options;

/**
 * <a href="http://tools.ietf.org/html/rfc4833">RFC 4833</a> defines new timezone options for DHCPv6. Use
 * NewPOSIXTimezone class together with {@link NewTZDBTimezone} instead of deprecated
 * {@link org.apache.directory.server.dhcp.options.vendor.TimeOffset TimeOffset} . <br>
 * <br>
 * POSIX provides a standard for how to express timezone information in a character string. However, for accuracy over
 * longer periods that involve daylight- saving rule changes or other irregular changes, a more detailed mechanism is
 * necessary. <br>
 * <br>
 * The <a href="http://www.twinsun.com/tz/tz-link.htm">TZ Database</a> that is used in many operating systems provides
 * backwards consistency and accuracy for almost all real-world locations since 1970. The TZ database also attempts to
 * provide a stable set of human readable timezone identifiers. <br>
 * <br>
 * TZ POSIX string is a string suitable for the TZ variable as specified by section 8.3 of POSIX standard, with the
 * exception that a string may not begin with a colon (":"). This string is NOT terminated by an ASCII NULL. Here is an
 * example: <br>
 * <br>
 * EST5EDT4,M3.2.0/02:00,M11.1.0/02:00
 *
 * @author marekgr
 * @see <a href="http://tools.ietf.org/html/rfc4833#section-4">RFC 4833 (section 4)</a>
 */
public class NewPOSIXTimezone extends StringOption {

    // OPTION_NEW_POSIX_TIMEZONE(41)
    private static final short TAG = 41;

    public NewPOSIXTimezone() {
    }

    /**
     *
     * @param tZPOSIXString
     *            TZ POSIX String
     */
    public NewPOSIXTimezone(String tZPOSIXString) {
        setString(tZPOSIXString);
    }

    @Override
    public short getTag() {
        return TAG;
    }

}
