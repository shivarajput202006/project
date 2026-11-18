package com.scamshield.scamdetector.service;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class EvidenceVerificationService {
    private final HostResolver hostResolver;

    public EvidenceVerificationService(HostResolver hostResolver) {
        this.hostResolver = hostResolver;
    }

    public UrlVerification verifyUrl(String value) {
        URI uri = parse(value);
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if ((!scheme.equals("http") && !scheme.equals("https"))
                || uri.getRawUserInfo() != null
                || uri.getHost() == null) {
            throw new UnsafeUrlException("Only regular HTTP or HTTPS links without credentials are supported.");
        }
        int expectedPort = scheme.equals("https") ? 443 : 80;
        if (uri.getPort() != -1 && uri.getPort() != expectedPort) {
            throw new UnsafeUrlException("The URL uses a port that is not supported.");
        }

        String host = normalizeHost(uri.getHost());
        if (isLocalHostname(host)) {
            throw new UnsafeUrlException("Local and internal network URLs cannot be analyzed.");
        }

        InetAddress literalAddress = parseAddressLiteral(host);
        if (literalAddress != null) {
            rejectUnsafeAddress(literalAddress);
            return new UrlVerification(uri.toASCIIString(), true, false, List.of(
                    "The link was not opened. No trusted domain reputation source is configured."));
        }

        try {
            List<InetAddress> addresses = hostResolver.resolve(host);
            if (addresses.isEmpty()) {
                return dnsUnavailable(uri);
            }
            for (InetAddress address : addresses) {
                rejectUnsafeAddress(address);
            }
            return new UrlVerification(uri.toASCIIString(), true, false, List.of(
                    "The link was not opened. DNS was checked for private addresses, but no trusted reputation source is configured."));
        } catch (UnknownHostException exception) {
            return dnsUnavailable(uri);
        }
    }

    private URI parse(String value) {
        try {
            if (value == null || value.isBlank()) {
                throw new UnsafeUrlException("Enter a valid HTTP or HTTPS URL.");
            }
            URI uri = new URI(value.trim());
            if (uri.getScheme() == null) {
                throw new UnsafeUrlException("Enter a valid HTTP or HTTPS URL.");
            }
            return uri;
        } catch (URISyntaxException exception) {
            throw new UnsafeUrlException("Enter a valid HTTP or HTTPS URL.");
        }
    }

    private String normalizeHost(String host) {
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        if (host.indexOf(':') >= 0) {
            return host.toLowerCase(Locale.ROOT);
        }
        String normalized = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        try {
            return IDN.toASCII(normalized, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new UnsafeUrlException("The URL hostname is invalid.");
        }
    }

    private boolean isLocalHostname(String host) {
        return host.equals("localhost")
                || host.endsWith(".localhost")
                || host.endsWith(".local")
                || host.endsWith(".internal")
                || host.endsWith(".test");
    }

    private InetAddress parseAddressLiteral(String host) {
        if (host.indexOf(':') >= 0) {
            try {
                return InetAddress.getByName(host);
            } catch (UnknownHostException exception) {
                throw new UnsafeUrlException("The URL IP address is invalid.");
            }
        }
        if (host.matches("[0-9.]+")) {
            String[] octets = host.split("\\.", -1);
            if (octets.length != 4) {
                throw new UnsafeUrlException("The URL IP address is invalid.");
            }
            byte[] address = new byte[4];
            try {
                for (int index = 0; index < octets.length; index++) {
                    int octet = Integer.parseInt(octets[index]);
                    if (octet < 0 || octet > 255 || octets[index].length() > 1 && octets[index].startsWith("0")) {
                        throw new NumberFormatException();
                    }
                    address[index] = (byte) octet;
                }
                return InetAddress.getByAddress(address);
            } catch (NumberFormatException | UnknownHostException exception) {
                throw new UnsafeUrlException("The URL IP address is invalid.");
            }
        }
        return null;
    }

    private void rejectUnsafeAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || isUnsafeReservedAddress(address)) {
            throw new UnsafeUrlException("This URL resolves to a private or reserved network address.");
        }
    }

    private boolean isUnsafeReservedAddress(InetAddress address) {
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = bytes[0] & 0xff;
            int second = bytes[1] & 0xff;
            int third = bytes[2] & 0xff;
            return first == 0
                    || first == 100 && second >= 64 && second <= 127
                    || first == 192 && (second == 0 || second == 168)
                    || first == 198 && (second == 18 || second == 19 || second == 51)
                    || first == 203 && second == 0 && third == 113
                    || first >= 224;
        }
        if (address instanceof Inet6Address) {
            return (bytes[0] & 0xfe) == 0xfc
                    || bytes[0] == 0x20 && bytes[1] == 0x01 && bytes[2] == 0x0d && bytes[3] == (byte) 0xb8;
        }
        return true;
    }

    private UrlVerification dnsUnavailable(URI uri) {
        return new UrlVerification(uri.toASCIIString(), false, false, List.of(
                "The domain could not be resolved for private-address checks. The link was not opened and its safety is unverified."));
    }

    public record UrlVerification(
            String normalizedUrl,
            boolean dnsResolved,
            boolean externallyVerified,
            List<String> limitations) {
        public UrlVerification {
            limitations = List.copyOf(limitations);
        }
    }

    public static class UnsafeUrlException extends RuntimeException {
        public UnsafeUrlException(String message) {
            super(message);
        }
    }
}
