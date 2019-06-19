package com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.validators;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressValidator {
    private static Long toNumeric(String ip) {
        Scanner sc = new Scanner(ip).useDelimiter("\\.");
        return
                (sc.nextLong() << 24) +
                        (sc.nextLong() << 16) +
                        (sc.nextLong() << 8) +
                        (sc.nextLong());
    }
//    public static boolean addressInNetwork(String address, String cidr) {
//        String[] ip = cidr.split("/");
//        Long cidrAddress = toNumeric(ip[0]);
//        Long userAddress = toNumeric(address);
//        //System.out.println((userAddress & ~cidrAddress) +  ": " + (Math.pow(2, 32 - Integer.parseInt(ip[1]))-1));
//        return (userAddress - cidrAddress) <= Math.pow(2, 32 - Integer.parseInt(ip[1]))-1 && userAddress >= cidrAddress;
//    }

    private static final Pattern addressPattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
    private static final Pattern cidrPattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})" + "/(\\d{1,2})");
    private static final int NBITS = 32;
    private static final long UNSIGNED_INT_MASK = 0x0FFFFFFFFL;

    public static boolean addressInNetwork(String address, String cidr) {
        Matcher matcher = cidrPattern.matcher(cidr);
        int cidrAddress = matchAddress(matcher);
        int userAddress = toInteger(address);

        if(matcher.matches() || userAddress == 0)
            return false;
        int trailingZeroes = NBITS - rangeCheck(Integer.parseInt(matcher.group(5)), 0, NBITS);
        int mask = (int) (UNSIGNED_INT_MASK << trailingZeroes );
        int network = (cidrAddress & mask);
        int broadcast = network | ~(mask);

        long addLong = userAddress & UNSIGNED_INT_MASK;
        long lowLong = low(broadcast, network) & UNSIGNED_INT_MASK;
        long highLong = high(broadcast, network) & UNSIGNED_INT_MASK;
        return addLong >= lowLong && addLong <= highLong;
    }
    private static int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return matchAddress(matcher);
        } else {
            throw new IllegalArgumentException("Could not parse [" + address + "]");
        }
    }
    private static int matchAddress(Matcher matcher) {
        int addr = 0;
        for (int i = 1; i <= 4; ++i) {
            int n = (rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255));
            addr |= ((n & 0xff) << 8*(4-i));
        }
        return addr;
    }
    private static int rangeCheck(int value, int begin, int end) {
        if (value >= begin && value <= end) { // (begin,end]
            return value;
        }
        throw new IllegalArgumentException("Value [" + value + "] not in range ["+begin+","+end+"]");
    }
    private static int low(int broadcast, int network) {
        return ((broadcast &  UNSIGNED_INT_MASK) - (network &  UNSIGNED_INT_MASK) > 1 ? network + 1 : 0);
    }

    private static int high(int broadcast, int network) {
        return ((broadcast &  UNSIGNED_INT_MASK)- (network &  UNSIGNED_INT_MASK) > 1 ? broadcast -1  : 0);
    }

}
