dhcp4j
======

A Java DHCP server and protocol handler.

This code originally came from the ApacheDS project, and the objective is to give it a complete
overhaul into a modern, correct and complete DHCP implementation.

The [JavaDoc API](http://shevek.github.io/dhcp4j/docs/javadoc/)
is available.

For UDP:
	sudo setcap 'cap_net_bind_service=+ep' `readlink -f /usr/bin/java`
For raw:
	sudo setcap 'cap_net_bind_service,cap_net_raw+ep' `readlink -f /usr/bin/java`

