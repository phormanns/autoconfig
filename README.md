autoconfig
==========

Simple implementation of the Thunderbird-autoconfig-feature in Java.

A java servlet responds to requests on   
http://autoconfig.example.orgmail/config-v1.1.xml?emailaddress=hugo.schuman@example.org

It exposes an xml file like

    <clientConfig version="1.1">
    	 <emailProvider id="hostsharing.net">
    	   <domain>example.org</domain>
    	   <incomingServer type="imap">
    	     <hostname>xyz00.hostsharing.net</hostname>
    	     <port>993</port>
    	     <socketType>SSL</socketType>
    	     <username>xyz00-hugo</username>
    	     <authentication>password-cleartext</authentication>
    	   </incomingServer>
    	   <outgoingServer type="smtp">
    	     <hostname>xyz00.hostsharing.net</hostname>
    	     <port>465</port>
    	     <socketType>SSL</socketType>
    	     <username>xyz00-hugo</username>
    	     <authentication>password-cleartext</authentication>
    	     <addThisServer>true</addThisServer>
    	     <useGlobalPreferredServer>true</useGlobalPreferredServer>
    	   </outgoingServer>
    	 </emailProvider> 
    </clientConfig>

to configure an IMAP mailbox in thunderbird.
