package org.anarres.dhcp.v6.options;

/**
 * <a href="http://tools.ietf.org/html/rfc4833">RFC 4833</a> defines new timezone options for DHCPv6. Use
 * NewTZDBTimezone class together with {@link NewPOSIXTimezone} instead of deprecated
 * {@link org.apache.directory.server.dhcp.options.vendor.TimeOffset TimeOffset}. <br>
 * <br>
 * POSIX provides a standard for how to express timezone information in a character string. However, for accuracy over
 * longer periods that involve daylight- saving rule changes or other irregular changes, a more detailed mechanism is
 * necessary. <br>
 * <br>
 * The <a href="http://www.twinsun.com/tz/tz-link.htm">TZ Database</a> that is used in many operating systems provides
 * backwards consistency and accuracy for almost all real-world locations since 1970. The TZ database also attempts to
 * provide a stable set of human readable timezone identifiers. <br>
 * <br>
 * TZ Name is the name of a Zone entry in the database commonly referred to as the TZ database. Specifically, in the
 * database's textual form, the string refers to the name field of a zone line. In order for this option to be useful,
 * the client must already have a copy of the database. This string is NOT terminated with an ASCII NULL.
 *
 * An example string is Europe/Zurich.
 *
 * @author marekgr
 * @see <a href="http://tools.ietf.org/html/rfc4833#section-5">RFC 4833 (section 5)</a>
 */
public class NewTZDBTimezone extends StringOption {

    // OPTION_NEW_TZDB_TIMEZONE(42)
    private static final short TAG = 42;

    public NewTZDBTimezone() {
    }

    /**
     *
     * @param tZName
     *            name of Zone entry in TZ Database
     */
    public NewTZDBTimezone(String tZName) {
        setString(tZName);
    }

    @Override
    public short getTag() {
        return TAG;
    }

}
