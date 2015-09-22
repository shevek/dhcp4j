package org.anarres.dhcp.v6.options;

/**
 * Timed option blueprint e.g. IaNaOption
 */
public interface TimedOption {

    int getT1();

    void setT1(int t1);

    void setT2(int t2);

    int getT2();
}
