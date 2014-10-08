/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.options.dhcp.BootfileName;
import org.apache.directory.server.dhcp.options.dhcp.ClientIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.ClientNetworkInterface;
import org.apache.directory.server.dhcp.options.dhcp.ClientSystemArchitecture;
import org.apache.directory.server.dhcp.options.dhcp.DhcpMessageType;
import org.apache.directory.server.dhcp.options.dhcp.IpAddressLeaseTime;
import org.apache.directory.server.dhcp.options.dhcp.MaximumDhcpMessageSize;
import org.apache.directory.server.dhcp.options.dhcp.Message;
import org.apache.directory.server.dhcp.options.dhcp.OptionOverload;
import org.apache.directory.server.dhcp.options.dhcp.ParameterRequestList;
import org.apache.directory.server.dhcp.options.dhcp.RebindingTimeValue;
import org.apache.directory.server.dhcp.options.dhcp.RelayAgentInformation;
import org.apache.directory.server.dhcp.options.dhcp.RenewalTimeValue;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.options.dhcp.ServerIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.TftpServerName;
import org.apache.directory.server.dhcp.options.dhcp.UUIDClientIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.UnrecognizedOption;
import org.apache.directory.server.dhcp.options.dhcp.VendorClassIdentifier;
import org.apache.directory.server.dhcp.options.linklayer.ArpCacheTimeout;
import org.apache.directory.server.dhcp.options.linklayer.EthernetEncapsulation;
import org.apache.directory.server.dhcp.options.linklayer.TrailerEncapsulation;
import org.apache.directory.server.dhcp.options.misc.DefaultFingerServers;
import org.apache.directory.server.dhcp.options.misc.DefaultIrcServers;
import org.apache.directory.server.dhcp.options.misc.DefaultWwwServers;
import org.apache.directory.server.dhcp.options.misc.MobileIpHomeAgents;
import org.apache.directory.server.dhcp.options.misc.NbddServers;
import org.apache.directory.server.dhcp.options.misc.NetbiosNameServers;
import org.apache.directory.server.dhcp.options.misc.NetbiosNodeType;
import org.apache.directory.server.dhcp.options.misc.NetbiosScope;
import org.apache.directory.server.dhcp.options.misc.NisDomain;
import org.apache.directory.server.dhcp.options.misc.NisPlusDomain;
import org.apache.directory.server.dhcp.options.misc.NisPlusServers;
import org.apache.directory.server.dhcp.options.misc.NisServers;
import org.apache.directory.server.dhcp.options.misc.NntpServers;
import org.apache.directory.server.dhcp.options.misc.NtpServers;
import org.apache.directory.server.dhcp.options.misc.Pop3Servers;
import org.apache.directory.server.dhcp.options.misc.SmtpServers;
import org.apache.directory.server.dhcp.options.misc.StdaServers;
import org.apache.directory.server.dhcp.options.misc.StreetTalkServers;
import org.apache.directory.server.dhcp.options.misc.UserClass;
import org.apache.directory.server.dhcp.options.misc.VendorSpecificInformation;
import org.apache.directory.server.dhcp.options.misc.XWindowDisplayManagers;
import org.apache.directory.server.dhcp.options.misc.XWindowFontServers;
import org.apache.directory.server.dhcp.options.perhost.DefaultIpTimeToLive;
import org.apache.directory.server.dhcp.options.perhost.IpForwarding;
import org.apache.directory.server.dhcp.options.perhost.MaximumDatagramSize;
import org.apache.directory.server.dhcp.options.perhost.NonLocalSourceRouting;
import org.apache.directory.server.dhcp.options.perhost.PathMtuAgingTimeout;
import org.apache.directory.server.dhcp.options.perhost.PathMtuPlateauTable;
import org.apache.directory.server.dhcp.options.perhost.PolicyFilter;
import org.apache.directory.server.dhcp.options.perinterface.AllSubnetsAreLocal;
import org.apache.directory.server.dhcp.options.perinterface.BroadcastAddress;
import org.apache.directory.server.dhcp.options.perinterface.InterfaceMtu;
import org.apache.directory.server.dhcp.options.perinterface.MaskSupplier;
import org.apache.directory.server.dhcp.options.perinterface.PerformMaskDiscovery;
import org.apache.directory.server.dhcp.options.perinterface.PerformRouterDiscovery;
import org.apache.directory.server.dhcp.options.perinterface.RouterSolicitationAddress;
import org.apache.directory.server.dhcp.options.perinterface.StaticRoute;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved128;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved129;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved130;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved131;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved132;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved133;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved134;
import org.apache.directory.server.dhcp.options.pxe.PxeReservedOption.PxeReserved135;
import org.apache.directory.server.dhcp.options.tcp.TcpDefaultTimeToLive;
import org.apache.directory.server.dhcp.options.tcp.TcpKeepaliveGarbage;
import org.apache.directory.server.dhcp.options.tcp.TcpKeepaliveInterval;
import org.apache.directory.server.dhcp.options.vendor.BootFileSize;
import org.apache.directory.server.dhcp.options.vendor.ClientFQDN;
import org.apache.directory.server.dhcp.options.vendor.CookieServers;
import org.apache.directory.server.dhcp.options.vendor.DirectoryAgent;
import org.apache.directory.server.dhcp.options.vendor.DomainName;
import org.apache.directory.server.dhcp.options.vendor.DomainNameServers;
import org.apache.directory.server.dhcp.options.vendor.DomainSearch;
import org.apache.directory.server.dhcp.options.vendor.ExtensionsPath;
import org.apache.directory.server.dhcp.options.vendor.HostName;
import org.apache.directory.server.dhcp.options.vendor.ImpressServers;
import org.apache.directory.server.dhcp.options.vendor.LogServers;
import org.apache.directory.server.dhcp.options.vendor.LprServers;
import org.apache.directory.server.dhcp.options.vendor.MeritDumpFile;
import org.apache.directory.server.dhcp.options.vendor.NameServers;
import org.apache.directory.server.dhcp.options.vendor.NetwareDomainName;
import org.apache.directory.server.dhcp.options.vendor.NetwareOptions;
import org.apache.directory.server.dhcp.options.vendor.ResourceLocationServers;
import org.apache.directory.server.dhcp.options.vendor.RootPath;
import org.apache.directory.server.dhcp.options.vendor.Routers;
import org.apache.directory.server.dhcp.options.vendor.ServiceScope;
import org.apache.directory.server.dhcp.options.vendor.SubnetMask;
import org.apache.directory.server.dhcp.options.vendor.SwapServer;
import org.apache.directory.server.dhcp.options.vendor.TimeOffset;
import org.apache.directory.server.dhcp.options.vendor.TimeServers;

/**
 * See http://www.iana.org/assignments/bootp-dhcp-parameters/bootp-dhcp-parameters.xhtml#options
 *
 * @author shevek
 */
public class DhcpOptionsRegistry {

    private static class Inner {

        private static final DhcpOptionsRegistry INSTANCE = new DhcpOptionsRegistry();
        /**
         * An array of concrete implementations of DhcpOption.
         */
        private static final Class OPTION_CLASSES[] = {
            AllSubnetsAreLocal.class,
            ArpCacheTimeout.class,
            BootfileName.class,
            BootFileSize.class,
            BroadcastAddress.class,
            ClientIdentifier.class,
            ClientFQDN.class,
            ClientNetworkInterface.class,
            ClientSystemArchitecture.class,
            CookieServers.class,
            DefaultFingerServers.class,
            DefaultIpTimeToLive.class,
            DefaultIrcServers.class,
            DefaultWwwServers.class,
            DhcpMessageType.class,
            DirectoryAgent.class,
            DomainName.class,
            DomainNameServers.class,
            DomainSearch.class,
            EthernetEncapsulation.class,
            ExtensionsPath.class,
            HostName.class,
            ImpressServers.class,
            InterfaceMtu.class,
            IpAddressLeaseTime.class,
            IpForwarding.class,
            LogServers.class,
            LprServers.class,
            MaskSupplier.class,
            MaximumDatagramSize.class,
            MaximumDhcpMessageSize.class,
            MeritDumpFile.class,
            Message.class,
            MobileIpHomeAgents.class,
            NameServers.class,
            NbddServers.class,
            NetbiosNameServers.class,
            NetbiosNodeType.class,
            NetbiosScope.class,
            NetwareDomainName.class,
            NetwareOptions.class,
            NisDomain.class,
            NisPlusDomain.class,
            NisPlusServers.class,
            NisServers.class,
            NntpServers.class,
            NonLocalSourceRouting.class,
            NtpServers.class,
            OptionOverload.class,
            ParameterRequestList.class,
            PathMtuAgingTimeout.class,
            PathMtuPlateauTable.class,
            PerformMaskDiscovery.class,
            PerformRouterDiscovery.class,
            PolicyFilter.class,
            Pop3Servers.class,
            PxeReserved128.class,
            PxeReserved129.class,
            PxeReserved130.class,
            PxeReserved131.class,
            PxeReserved132.class,
            PxeReserved133.class,
            PxeReserved134.class,
            PxeReserved135.class,
            RebindingTimeValue.class,
            RelayAgentInformation.class,
            RenewalTimeValue.class,
            RequestedIpAddress.class,
            ResourceLocationServers.class,
            RootPath.class,
            Routers.class,
            RouterSolicitationAddress.class,
            ServerIdentifier.class,
            ServiceScope.class,
            SmtpServers.class,
            StaticRoute.class,
            StdaServers.class,
            StreetTalkServers.class,
            SubnetMask.class,
            SwapServer.class,
            TcpDefaultTimeToLive.class,
            TcpKeepaliveGarbage.class,
            TcpKeepaliveInterval.class,
            TftpServerName.class,
            TimeOffset.class,
            TimeServers.class,
            TrailerEncapsulation.class,
            UnrecognizedOption.class,
            UserClass.class,
            UUIDClientIdentifier.class,
            VendorClassIdentifier.class,
            VendorSpecificInformation.class,
            XWindowDisplayManagers.class,
            XWindowFontServers.class,

        };

        static {
            for (Class<? extends DhcpOption> optionType : OPTION_CLASSES) {
                INSTANCE.addOptionType(optionType);
            }
        }
    }

    @Nonnull
    public static DhcpOptionsRegistry getInstance() {
        return Inner.INSTANCE;
    }
    private final Map<Byte, Class<? extends DhcpOption>> optionTypes = new HashMap<Byte, Class<? extends DhcpOption>>();
    private final Map<Class<? extends DhcpOption>, Byte> optionTags = new WeakHashMap<Class<? extends DhcpOption>, Byte>();

    @Nonnull
    public static <T extends DhcpOption> T newInstance(@Nonnull Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate " + type, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot instantiate " + type, e);
        }
    }

    private byte getTagFrom(@Nonnull Class<? extends DhcpOption> type) {
        DhcpOption o = newInstance(type);
        byte tag = o.getTag();
        optionTags.put(type, tag);
        return tag;
    }

    public void addOptionType(@Nonnull Class<? extends DhcpOption> type) {
        byte tag = getTagFrom(type);
        if (optionTypes.put(tag, type) != null)
            throw new IllegalArgumentException("Duplicate tag: " + type);
        optionTags.put(type, tag);
    }

    @CheckForNull
    public Class<? extends DhcpOption> getOptionType(byte tag) {
        return optionTypes.get(tag);
    }

    @Nonnull
    public byte getOptionTag(@Nonnull Class<? extends DhcpOption> type) {
        Byte tag = optionTags.get(type);
        if (tag != null)
            return tag.byteValue();
        // TODO: Warn about unregistered option.
        return getTagFrom(type);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + optionTypes + ")";
    }
}
