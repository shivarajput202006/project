package com.scamshield.scamdetector.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public interface HostResolver {
    List<InetAddress> resolve(String host) throws UnknownHostException;
}
