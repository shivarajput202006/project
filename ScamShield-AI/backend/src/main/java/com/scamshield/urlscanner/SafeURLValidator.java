package com.scamshield.urlscanner;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class SafeURLValidator {
    public ValidatedUrl validate(String value) {
        if (value == null || value.isBlank() || value.length() > 2048) {
            throw new InvalidUrlException("Enter a valid HTTP or HTTPS URL no longer than 2048 characters.");
        }
        if (value.codePoints().anyMatch(Character::isISOControl) || value.indexOf('\\') >= 0) {
            throw new InvalidUrlException("The URL contains unsupported control characters.");
        }
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new InvalidUrlException("Only HTTP and HTTPS URLs are supported.");
            }
            if (uri.getRawUserInfo() != null || uri.getRawAuthority() == null) {
                throw new InvalidUrlException("Enter an absolute URL without embedded credentials.");
            }
            Authority authority = parseAuthority(uri.getRawAuthority());
            String host = normalizeHost(authority.host());
            if (isLocalHostname(host)) {
                throw new InvalidUrlException("Local and internal network hostnames cannot be scanned.");
            }
            InetAddress literal = parseAddressLiteral(host);
            if (literal != null && isUnsafeAddress(literal)) {
                throw new InvalidUrlException("Local, private, and reserved IP addresses cannot be scanned.");
            }
            if (literal == null && isNonStandardNumericAddress(host)) {
                throw new InvalidUrlException("The URL contains an unsupported numeric IP address.");
            }
            int requiredPort = scheme.equals("https") ? 443 : 80;
            if (authority.port() != -1 && authority.port() != requiredPort) {
                throw new InvalidUrlException("Only standard HTTP (80) and HTTPS (443) ports are supported.");
            }
            return new ValidatedUrl(
                    value.trim(),
                    scheme,
                    host,
                    authority.host(),
                    literal == null ? "DOMAIN" : literal instanceof Inet6Address ? "IPV6_ADDRESS" : "IPV4_ADDRESS");
        } catch (URISyntaxException exception) {
            throw new InvalidUrlException("Enter a valid absolute HTTP or HTTPS URL.");
        }
    }

    private Authority parseAuthority(String authority) {
        if (authority.isBlank() || authority.indexOf('@') >= 0 || authority.indexOf('%') >= 0) {
            throw new InvalidUrlException("The URL hostname is invalid or contains credentials.");
        }
        String host;
        int port = -1;
        if (authority.startsWith("[")) {
            int close = authority.indexOf(']');
            if (close < 0) {
                throw new InvalidUrlException("The IPv6 hostname is invalid.");
            }
            host = authority.substring(1, close);
            String suffix = authority.substring(close + 1);
            if (!suffix.isEmpty()) {
                port = parsePort(suffix);
            }
            if (host.indexOf(':') < 0 || host.contains("%")) {
                throw new InvalidUrlException("Enter a valid IPv6 hostname.");
            }
        } else {
            int firstColon = authority.indexOf(':');
            int lastColon = authority.lastIndexOf(':');
            if (firstColon != lastColon) {
                throw new InvalidUrlException("IPv6 addresses must be enclosed in brackets.");
            }
            host = firstColon >= 0 ? authority.substring(0, firstColon) : authority;
            if (firstColon >= 0) {
                port = parsePort(authority.substring(firstColon));
            }
        }
        if (host.isBlank()) {
            throw new InvalidUrlException("The URL hostname is missing.");
        }
        return new Authority(host, port);
    }

    private int parsePort(String suffix) {
        if (!suffix.matches(":[0-9]{1,5}")) {
            throw new InvalidUrlException("The URL port is invalid.");
        }
        try {
            int port = Integer.parseInt(suffix.substring(1));
            if (port < 1 || port > 65535) {
                throw new InvalidUrlException("The URL port is invalid.");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new InvalidUrlException("The URL port is invalid.");
        }
    }

    private String normalizeHost(String host) {
        if (host.indexOf(':') >= 0) {
            return host.toLowerCase(Locale.ROOT);
        }
        String normalized = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        try {
            String ascii = IDN.toASCII(normalized, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
            if (ascii.length() > 253) {
                throw new IllegalArgumentException();
            }
            for (String label : ascii.split("\\.", -1)) {
                if (label.isBlank() || label.length() > 63) {
                    throw new IllegalArgumentException();
                }
            }
            return ascii;
        } catch (IllegalArgumentException exception) {
            throw new InvalidUrlException("The URL hostname is invalid.");
        }
    }

    private boolean isLocalHostname(String host) {
        return host.equals("localhost")
                || host.endsWith(".localhost")
                || host.endsWith(".local")
                || host.endsWith(".internal")
                || host.endsWith(".test")
                || host.endsWith(".invalid")
                || host.endsWith(".lan")
                || host.endsWith(".intranet")
                || host.equals("home.arpa")
                || host.endsWith(".home.arpa");
    }

    private InetAddress parseAddressLiteral(String host) {
        if (host.indexOf(':') >= 0) {
            try {
                return InetAddress.getByName(host);
            } catch (UnknownHostException exception) {
                throw new InvalidUrlException("The IPv6 address is invalid.");
            }
        }
        if (!host.matches("[0-9.]+")) {
            return null;
        }
        String[] parts = host.split("\\.", -1);
        if (parts.length != 4) {
            throw new InvalidUrlException("The IPv4 address is invalid.");
        }
        byte[] address = new byte[4];
        try {
            for (int index = 0; index < parts.length; index++) {
                if (parts[index].isEmpty() || parts[index].length() > 1 && parts[index].startsWith("0")) {
                    throw new NumberFormatException();
                }
                int octet = Integer.parseInt(parts[index]);
                if (octet > 255) {
                    throw new NumberFormatException();
                }
                address[index] = (byte) octet;
            }
            return InetAddress.getByAddress(address);
        } catch (NumberFormatException | UnknownHostException exception) {
            throw new InvalidUrlException("The IPv4 address is invalid.");
        }
    }

    private boolean isNonStandardNumericAddress(String host) {
        return host.matches("[0-9]+") || host.matches("(0x[0-9a-f]+\\.?)+");
    }

    private boolean isUnsafeAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }
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
                    || bytes[0] == 0x20 && bytes[1] == 0x01
                            && bytes[2] == 0x0d && bytes[3] == (byte) 0xb8;
        }
        return true;
    }

    public record ValidatedUrl(
            String submittedUrl,
            String scheme,
            String normalizedDomain,
            String originalHostname,
            String hostnameType) {
    }

    private record Authority(String host, int port) {
    }

    public static class InvalidUrlException extends RuntimeException {
        public InvalidUrlException(String message) {
            super(message);
        }
    }
}
