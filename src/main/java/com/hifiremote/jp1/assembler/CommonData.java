package com.hifiremote.jp1.assembler;

import com.hifiremote.jp1.Hex;

public class CommonData
{
  public static final Integer[] to15 = {
    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13, 14, 15 };

  public static final Integer[] to8 = {
    0, 1, 2, 3, 4, 5, 6, 7, 8 };
  
  public static final String[] sigStructs012 = {
    "dev-cmd", "dev-cmd-!dev-!cmd", "dev-!dev-cmd-!cmd", "cmd-dev" };
   
  public static final String[] sigStructs34 = {
    "dev-cmd", "dev-cmd-!dev-!cmd", "dev-!dev-cmd-!cmd", "cmd-!dev", 
    "cmd-dev2", "cmd", "cmd-!cmd", "cmd-!dev-!cmd", "cmd-!dev-dev2", 
    "cmd-!dev-dev2-!cmd", "cmd-dev2-!cmd", "!cmd", "dev", "dev-!cmd", 
    "dev-!dev", "dev-!dev-!cmd", "dev-!dev-cmd", "dev-!dev-dev2", 
    "dev-!dev-dev2-!cmd", "dev-!dev-dev2-cmd", "dev-!dev-dev2-cmd-!cmd", 
    "dev-cmd-!cmd", "dev-cmd-!dev", "dev-cmd-!dev-dev2", 
    "dev-cmd-!dev-dev2-!cmd", "dev-cmd-dev2", "dev-cmd-dev2-!cmd", 
    "dev-dev2", "dev-dev2-!cmd", "dev-dev2-cmd", "dev-dev2-cmd-!cmd", 
    "!dev", "!dev-!cmd", "!dev-cmd", "!dev-cmd-!cmd", "!dev-dev2", 
    "!dev-dev2-!cmd", "!dev-dev2-cmd", "!dev-dev2-cmd-!cmd", "dev2", 
    "dev2-!cmd", "dev2-cmd", "dev2-cmd-!cmd", "devs", "devs-!cmd", 
    "devs-!dev", "devs-!dev-!cmd", "devs-!dev-cmd", "devs-!dev-cmd-!cmd", 
    "devs-!dev-dev2", "devs-!dev-dev2-!cmd", "devs-!dev-dev2-cmd", 
    "devs-!dev-dev2-cmd-!cmd", "devs-cmd", "devs-cmd-!cmd", "devs-cmd-!dev", 
    "devs-cmd-!dev-!cmd", "devs-cmd-!dev-dev2", "devs-cmd-!dev-dev2-!cmd", 
    "devs-cmd-dev2", "devs-cmd-dev2-!cmd", "devs-dev2", "devs-dev2-!cmd", 
    "devs-dev2-cmd", "devs-dev2-cmd-!cmd" }; 


  public static final String[] bitDouble012 = {
    "None", "'0' after every bit", "'1' after every bit", "Double every bit" };

  public static final String[] bitDouble34 = {
    "None", "Bit Double On" };

  public static final String[] repeatType = {
    "Forced", "Minimum" };
  
  public static final String[] repeatHeld012 = {
    "No", "Yes", "Ch+/-, Vol+/-, FF, Rew", "No data bits in repeat" };
  
  public static final String[] noYes = {
    "No", "Yes" };
  
  public static final String[] leadInStyle = {
    "None", "Same every frame", "1st frame only", "Half-size after 1st" };
  
  public static final String[] leadOutStyle = {
    "0 = [-LO]", "1 = [LI], [-LO]", "2 = [OneOn, -LO]", "3 = [LI], [OneOn, -LO]" };

  public static final int[] burstOFFoffsets34 = { 52, 57 };
  
  public static final int[] leadinOFFoffsets34 = { 45, 45 };
  
  public static final int[] altLeadoutOffsets34 = { 45, - 40 };
  
  public static String[][][] pfData = {
      { // PF0
        { "2", "0-1",
          "Words to send in device code portion of IR signal",
          "0 = nothing\n" + 
          "1 = single word of PD00 bits\n" +  
          "2 = word of PD00 bits followed by word of PD10 bits\n" +  
          "3 = all protocol's fixed parameters (PD00 bits each)",
          "First byte is the R01-th (S3C80) / $AC-th (HCS08) in all cases" 
        },
        { "2", "2-3",
          "Words to send in command code portion of IR signal",
          "0 = nothing\n" +  
          "1 = single word of PD01 bits\n" +  
          "2 = word of PD01 bits followed by word of PD12 bits\n" +  
          "3 = all protocol's variable parameters (PD01 bits each)"
        },
        { "2", "4-5",
          "How to compose the signal (! = complement)",
          "0 = device - command\n" +  
          "1 = device - command - !device - !command\n" +  
          "2 = device - !device - command - !command\n" +  
          "3 = command - device"
        },
        { "1", "6",
          "Is Lead Out gap adjusted for total frame length (Off is Total)",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "7",
          "Is PF1 present?",
          "0 = no\n" +
          "1 = yes"
        }
      },
      { // PF1
        { "2", "0-1",
          "Does the signal repeat while button is held down?",
          "0 = no\n" + 
          "1 = yes\n" + 
          "2 = Ch+/-, Vol+/-, FF, Rew\n" + 
          "3 = no data bits in repeat"
        },
        { "2", "2-3",
          "How to send lead-in burst pair",
          "0 = nothing\n" + 
          "1 = always PD0A/PD0D\n" + 
          "2 = PD0A/PD0D the first time only, nothing afterwards\n" + 
          "3 = PD0A/PD0D the first time, no data in repeats",
          "In case 3, in repeat lead-ins the OFF-time is halved."
        },
        { "1", "4",
          "Is number of repeats taken from PD11?",
          "0 = no\n" +
          "1 = yes" 
        },
        { "2", "5-6",
          "Lead Out On style",
          "0 = [-LO]\n" +
          "1 = [LI], [-LO]\n" +
          "2 = [One On, -LO]\n" +
          "3 = [LI], [One On, -LO]"
        },
        { "1", "7",
          "Is PF2 present?",
          "0 = no\n" +
          "1 = yes"
        }
      },
      { // PF2
        { "2", "0-1",
          "How to send data for device bytes",
          "0 = send data as-is\n" + 
          "1 = send 0 after every bit\n" + 
          "2 = send 1 after every bit\n" + 
          "3 = send every bit twice"
        },
        { "2", "2-3",
          "How to send data for command bytes",
          "0 = send data as-is\n" + 
          "1 = send 0 after every bit\n" + 
          "2 = send 1 after every bit\n" + 
          "3 = send every bit twice"
        },
        { "1", "4",
          "Send zero backwards? (bi-phase)",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "5",
          "For 0-bit that is in even position (2nd, 4th bits sent etc) " +
          "send burst pair PD06/(PD02+PD04+PD08) instead of PD06/PD08?",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "6",
          "Use Extended Lead-Out OFF time, adding 0xFFFF to value in PD0A/PD0B?",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "7",
          "Is PF3 present?",
          "0 = no\n" +
          "1 = yes"
        }
      },
      { // PF3
        { "3", "0-2",
          "Determines encoding scheme (translation from data bytes to bit sequence).  " +
          "Except for the cases specified, each bit of a data byte is sent as itself, " +
          "starting at most significant bit, unless bit 6 of PF4 is set (see PF4).",
          "1 = value sent is device byte XORed with #$78\n" +
          "3 = use four bits to send two bits\n" +
          "4 + use variable number of bits to send two bits\n" +
          "other = normal bitwise encoding, see above",
          "Case 3 encoding is 1000 = 0, 0100 = 1, 0010 = 2, 0001 = 3\n" +
          "Case 4 encoding is 1 = 0, 10 = 1, 100 = 2, 1000 = 3\n" +
          "In cases 3 and 4, if an odd number of bits send msb as itself.  Case 1 is used only " +
          "for one device byte, e.g. in Protocol 002A."
        },
        { "1", "3",
          "For first Tx use 2nd set of device and command bytes following first in buffer, " +
          "created by protocol executor (repeat Tx always uses first set)?",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "4",
          "After all repeats, send one Tx of 2nd set of device and command bytes?",
          "0 = no\n" +
          "1 = yes"
        },
        {
          "1", "5",
          "Use alt leadout from PD13/PD14 instead of PD0A/PD0B?",
          "0 = no\n" +
          "1 = yes"
        },
        {
          "1", "6",
          "Send 0-bursts with alt frequency and duty cycle from PD13/PD14?",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "7",
          "Is PF4 present?",
          "0 = no\n" +
          "1 = yes"
        }
      },
      { // PF4
        { "1", "0",
          "Immediate repeat for held keypress, skipping minimum hold time?",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "1",
          "Immediate action on change of keypress, skipping full keypad scan?",
          "0 = no\n" +
          "1 = yes"
        },
        { "2", "2-3",
          "Number of stop bits (0-bits) for asynchronous encoding (see bit 6)",
          "0 = No stop bits\n" +
          "1 = One stop bit\n" +
          "other = Two stop bits"
        },
        { "2", "4-5",
          "Parity bit for asynchronous encoding (see bit 6)",
          "0 = no parity bit\n" +
          "1 = one parity bit, odd parity\n" +
          "other = one parity bit, even parity"
        },
        { "1", "6",
          "When bits 0-2 of PF3 are other than 3 or 4, use asynchronous encoding: " +
          "prepend data bits with one start bit (1-bit), optionally append parity and " +
          "stop bits according to bits 2-5 above?",
          "0 = no\n" +
          "1 = yes"
        },
        { "1", "7",
          "Must be 0",
          "0 = required\n" +
          "1 = error"
        },
      }      
   };
  
  public static String[][] pdData = {
    { "0", "0", "Many data values translate to times in microseconds (uSec).  For S3C80 and HCS08 " +
      "a 2-byte burst value N translates to 2*N uSec except for the OFF time of an ON/OFF " +
      "burst pair for S3C80, which is (2*N+40) uSec.  A 1-byte carrier value N translates " +
      "to (N+2)/8 uSec for S3C80, N/4 uSec for HCS08.  Individual PD values as used by the IR " +
      "engine are described below, but be aware that protocol code may use PD values in other ways."
    },
    { "1", "1", "number of bits to be used from each device byte (but see PD10)", "bits" },
    { "1", "1", "number of bits to be used from each command byte (but see PD12)", "bits" },
    { "2", "2", "ON time for logical ONE burst pair", "uSec" },
    { "2", "3", "OFF time for logical ONE burst pair", "uSec" },
    { "2", "2", "ON time for logical ZERO burst pair", "uSec" },
    { "2", "3", "OFF time for logical ZERO burst pair", "uSec" },
    { "2", "2", "LEAD-OUT time (or TOTAL TIME if PF1.6 set", "uSec" },
    { "2", "2", "ON time for lead-in burst pair", "uSec" },
    { "2", "3", "OFF time for lead-in burst pair", "uSec" },
    { "1", "1", "number of bits to be used from 2nd device byte [only when exactly 2 device bytes, value 255 means disabled]", "bits" },
    { "1", "1", "minimum times to repeat the signal [only when PF1.4 is set]", "repeats" },
    { "1", "1", "number of bits to be used from 2nd command byte [only when exactly 2 command bytes, value 255 means disabled]", "bits" },
    { "2", "2", "variable significance:\n" +
      "if PF3.5 set, alternate LEAD-OUT time", "uSec;" }, 
    { null, "4", "if PF3.6 set, alternate carrier ON/OFF times", "uSec ON", "uSec OFF;" },
    { null, "1", "if alternate entry point used for IR engine, mid-frame burst sent after PD13 bits", "bits" }
  };
  
  public static final short[][] pdDefaults = {
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 0, 0 },
    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 255, 0, 0, 0, 0 },
    { 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    { 0, 0, 0, 1, 1, 1, 1, 0, 10, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
  };
  
  public static final Hex[] forcedRptCode = { 
    new Hex( new short[]{ 0xE6, 0x0D, 0 } ),
    new Hex( new short[]{ 0x6E, 0, 0xB2 } ),
    new Hex( new short[]{ 0xA6, 0, 0xB7, 0x80 } ),
    new Hex( new short[]{ 0xAD, 0 } ),
    new Hex( new short[]{ 0x3C, 0, 0x83 } ),
  };
  
  public static String[][] fnData = {
    { "",
      "The predefined constants for register (1 byte) and function addresses (2 bytes) are listed below, " +
      "together with a brief description.  Those used in the protocol head the list and are starred, " +
      "even if use of predefined constants is deselected in the disassembly."
    },
    { "XmitIR",
      "Generate IR signal for data in 10-byte buffer DCBUF according to parameters in PFn and PDnn " +
      "(= EncodeDCBuf + XmitIRNoEncode)."
    },
    { "TestRptReqd",
      "Test if repeat required due either to repeat count or key held, return with Carry bit set " +
      "if yes, clear if no."
    },
    { "EncodeDCBuf",
      "Translate data in 10-byte buffer DCBUF into bit sequence according to parameters in PFn and PDnn."
    },
    { "XmitIRNoEncode",
      "Generate IR signal from bit sequence already created."
    },
    { "SetupXmitIR",
      "Set carrier and burst timings from protocol data then call XmitIR." 
    },
    { "IRMarkByPtr",
      "Send IR Mark (ON burst) for time pointed at by register HX (HCS08) or W1 (S3C80)."     
    },
    { "IRMarkSpaceByPtr",
      "Send ON/OFF burst pair for times pointed at by register HX (HCS08) or W1 (S3C80)."
    },
    { "IRSpaceByReg",
      "Send IR Space (OFF burst) for time contained in register HX (HCS80) or RF8/RF8 (S3C80)."
    },
    { "XmitSplitIR",
      "As XmitIR but split after PD13 bits by a mid-frame burst with ON-time that of a 1-burst " +
      "and OFF-time that of a Lead-In burst."
    },
    { "ChkPowerKey",
      "Test if pressed key is Power, return with Carry bit set if yes, clear if no."
    },
    { "ChkRecordKey",
      "Test if pressed key is Record, return with Carry bit set if yes, clear if no."
    },
    { "ChkVolKeys",
      "Test if pressed key is Vol+/-, return with Carry bit set if yes, clear if no."
    },
    { "ChkPwrRecVol",
      "Test if pressed key is Power, Record or Vol+/-, return with Carry bit set if yes, clear if no."
    },
    { "ChkVolChFFKeys",
      "Test if pressed key is one of Vol+/-, Ch+/-, FF, Rew, return with Carry bit set if yes, clear if no."
    },
    { "SetCarrier",
      "Set carrier generator timing from $A7/$A8 (HCS08) or R0E/R0F (S3C80), initialized from protocol data."
    },
    { "ChkLowBattery",
      "Test if battery low, return with Carry bit set if yes, clear if no."
    },
    {
      "DCBUF",
      "Start of a 10-byte buffer that holds the device and command bytes constructed from the fixed and "+
      "variable data in accordance with bits 4-5 of PF0."
    },
    {
      "DCNDX",
      "An index into DCBUF, initialized to zero, that points to the first device byte to be encoded."
    },
    { "DBYTES",
      "The number of device bytes to be encoded, starting from DCBUF, initialized from the protocol data."
    },
    { "CBYTES",
      "The number of command bytes to be encoded, starting from the last device byte, initialized " +
      "from the protocol data."
    },
    { "FLAGS",
      "The upper bits are flags used during processing, the lower bits (5 for the HCS08) are a counter " +
      "that is incremented on each keypress.  So in particular, bit 0 alternates on each keypress and " +
      "may be used to implement a protocol toggle."
    },
    {
      "CARRIER",
      "The first byte of two timing values that are written to the carrier generator, initialized " +
      "from protocol data."
    },
    {
      "RPT",
      "Number of repeats required."
    }
    
  };
}
