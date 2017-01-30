//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.


package com.hpl.hp.utils;

/**
 * @author marco casassa mont
 *
 */

public class ParserOutcome {
	
	public boolean valid = true;
	public int num_COLUMNS = 10; 
	public String formattedDate = " ";
	public String client = " ";
	public String domain = " ";
	public String dn[] = new String[num_COLUMNS];
    public String overflow_Column = " ";
    public int overflowFlag =0;
    public int numDNComp =0;
    
    
    public boolean isDR = false;
    public boolean isBDR = false;
    public boolean isStat = false;
    
    public String isBlacklisted ="false";
    
    public String rt = " ";
    public String cat = " ";
    public String proto = " ";
    public String src = " ";
    public String dst = " ";
    public String spt = " ";
    public String dpt = " ";
    
    public String request = "";
    
    public String externalID = " ";
    public String cs1 = " ";   // DNS Stats ONLY
    public String cs2 = " ";
    public String cs3 = " ";   // DNS Stats ONLY
    public String cs4 = " ";
    public String cs5 = " ";
    public String cs6 = " ";
    public String cs6a2 = " ";
    
    public String cn1 = " ";
    public String cn2 = " ";
    public String cn3 = " ";
    public String cn4 = " ";
    
    public String cfp1 = " ";  // DNS Stats ONLY
    public String cfp2 = " ";  // DNS Stats ONLY
    public String cfp3 = " ";  // DNS Stats ONLY
    public String cfp4 = " ";  // DNS Stats ONLY
    public String TXTRecords = " ";  // DNS Stats ONLY  
    public String TotalAdditionalRecords = " ";  // DNS Stats ONLY
    public String TotalWellformedDNSPackets = " ";  // DNS Stats ONLY 
    public String UnhandledLargePacketsOverTCP = " ";  // DNS Stats ONLY 
    public String PTRRecords = " ";  // DNS Stats ONLY
    public String MXRecords = " ";  // DNS Stats ONLY 
    public String TotalAuthorityRecords = " ";  // DNS Stats ONLY
    public String AAAARecords = " ";  // DNS Stats ONLY
    public String NonPort53Packets = " ";  // DNS Stats ONLY 
    public String SOARecords = " ";  // DNS Stats ONLY 
    public String NXDOMAIN = " ";  // DNS Stats ONLY
    public String NotIPv4Packets = " ";  // DNS Stats ONLY 
    public String SyslogMessagesSent = " ";  // DNS Stats ONLY
    public String QnameLargerThan256B = " ";  // DNS Stats ONLY 
    public String FragmentedPackets = " ";  // DNS Stats ONLY 
    public String NotTCPorUDPPackets = " ";  // DNS Stats ONLY
    public String TotalQuestions = " ";  // DNS Stats ONLY 
    public String OtherRecords = " ";  // DNS Stats ONLY 
    public String TCPPackets = " ";  // DNS Stats ONLY
    public String TotalAnswerRecords = " ";  // DNS Stats ONLY
    public String MalformedPackets = " ";  // DNS Stats ONLY 
    public String NonRecursiveQueries = " ";  // DNS Stats ONLY 
    public String ParsingPastEndofPacket = " ";  // DNS Stats ONLY
    public String BlackResponses = " ";  // DNS Stats ONLY 
    public String SRVRecords = " ";  // DNS Stats ONLY8 
    public String UDPPackets = " ";  // DNS Stats ONLY 
    public String CNAMERecords = " ";  // DNS Stats ONLY 
    public String BlackQueries = " ";  // DNS Stats ONLY 
    public String ARecords = " ";  // DNS Stats ONLY 
    public String OtherParsingErrors = " ";  // DNS Stats ONLY 
    public String UnsupportedPackets = " ";  // DNS Stats ONLY 
    public String InverseQueries = " ";  // DNS Stats ONLY 
    public String NSRecords = " ";  // DNS Stats ONLY 
    public String PacketsWithInvalidPointersInNames = " ";  // DNS Stats ONLY
    
    
    public String HPDNSLogQR = " ";
    public String HPDNSLogOpcode = " ";
    public String HPDNSLogAA = " ";
    public String HPDNSLogTC = " ";
    public String HPDNSLogRD = " ";
    public String HPDNSLogRA = " ";
    public String HPDNSLogRCode = " ";
    public String HPDNSLogSerial = " ";
    public String HPDNSLogRefresh = " ";
    public String HPDNSLogRetry = " ";
    public String HPDNSLogExpire = " ";
    public String HPDNSLogMinimum = " ";
    public String HPDNSLogPriorty = " ";
    public String HPDNSLogWeight = " ";
    public String HPDNSLogPort = " ";
    
};
