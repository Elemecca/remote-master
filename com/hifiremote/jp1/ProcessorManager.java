package com.hifiremote.jp1;

import java.util.*;

public class ProcessorManager
{
  private ProcessorManager()
  {
    processors = new Hashtable();
    add( new S3C80Processor());
    
    Processor p = new Processor( "6805", "C9" );
    int[] opcodes = { 0xCC, 0xCD };
    int[] addresses = 
    {
      0x0180,
      0x0183, 
      0x0186,
      0x0189,
      0x018C,
      0x018F,
      0x0192,
      0x0195,
      0x0198,
      0x019B,
      0x019E,
      0x019F,
      0x01A6,
      0x01A9,
      0x01AD,
      0x01B2,
      0x01B4,
      0x01BC
    };
    p.setVectorEditData( opcodes, addresses );
    add( p );
    
    p = new Processor( "6805", "RC16/18" );
    int[] moreAddresses = 
    {
      0x0180,
      0x0183,
      0x0186,
      0x0189,
      0x018C,
      0x018F,
      0x0192,
      0x0195,
      0x0196,
      0x019D,
      0x01A1,
      0x01A5,
      0x01AF,
      0x01B2,
      0x01B5,
      0x01B8,
      0x01BB,
      0x01BE,
      0x01C1,
      0x01C4,
      0x01C7,
      0x01CA
    };
    p.setVectorEditData( opcodes, moreAddresses );
    add( p );
    add( new Processor( "740" ));
    add( new Processor( "HCS08" ));
  }
  

  public static Processor getProcessor( String name, String version )
  {
    String lookup = name;
    if ( version != null )
      lookup = name + '-' + version;
    return ( Processor )processorManager.processors.get( lookup );
  }

  public static Processor getProcessor( String text )
  {
    String name = "";
    if ( text.startsWith( "S3C8" ))
      name = "S3C80";
    else if ( text.startsWith( "M6805" ))
      name = text.substring( 1 );
    else if ( text.equals( "P8/740" ))
      name = "740";
    return ( Processor )processorManager.processors.get( name );
  }

  public static Enumeration getProcessorNames()
  {
    return processorManager.processors.keys();
  }

  private void add( Processor p )
  {
    processors.put( p.getFullName(), p ); 
  }

  private static ProcessorManager processorManager = new ProcessorManager();
  private Hashtable processors = null;
}
