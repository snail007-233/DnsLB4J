package com.snail.dnslb4j.dns.response;

import java.io.IOException;

public class DnsException extends IOException {
    public DnsException(String domain, String message) {
        super(domain + ": " + message);
    }
}