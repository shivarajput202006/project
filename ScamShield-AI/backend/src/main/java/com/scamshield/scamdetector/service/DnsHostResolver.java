package com.scamshield.scamdetector.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;
import org.xbill.DNS.TextParseException;

@Component
public class DnsHostResolver implements HostResolver {
    @Override
    public List<InetAddress> resolve(String host) throws UnknownHostException {
        SimpleResolver resolver = new SimpleResolver();
        resolver.setTimeout(Duration.ofSeconds(2));
        List<InetAddress> addresses = new ArrayList<>();
        resolveRecords(host, Type.A, resolver, addresses);
        resolveRecords(host, Type.AAAA, resolver, addresses);
        if (addresses.isEmpty()) {
            throw new UnknownHostException("No A or AAAA records were found.");
        }
        return List.copyOf(addresses);
    }

    private void resolveRecords(String host, int type, SimpleResolver resolver, List<InetAddress> addresses)
            throws UnknownHostException {
        Lookup lookup;
        try {
            lookup = new Lookup(host, type);
        } catch (TextParseException exception) {
            throw new UnknownHostException("The hostname could not be queried.");
        }
        lookup.setResolver(resolver);
        Record[] records = lookup.run();
        if (records == null) {
            if (lookup.getResult() == Lookup.TRY_AGAIN) {
                throw new UnknownHostException("DNS lookup timed out.");
            }
            return;
        }
        for (Record record : records) {
            InetAddress address = record instanceof ARecord aRecord
                    ? aRecord.getAddress()
                    : record instanceof AAAARecord aaaaRecord ? aaaaRecord.getAddress() : null;
            if (address != null) {
                addresses.add(address);
            }
        }
    }
}
