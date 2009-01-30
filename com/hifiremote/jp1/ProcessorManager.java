package com.hifiremote.jp1;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ProcessorManager.
 */
public class ProcessorManager
{
  
  /**
   * Instantiates a new processor manager.
   */
  private ProcessorManager()
  {
    processors = new LinkedHashMap< String, Processor >();
    add( new S3C80Processor());

    Processor p = new BigEndianProcessor( "6805", "C9" );
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

    p = new BigEndianProcessor( "6805", "RC16/18" );
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
    add( new LittleEndianProcessor( "740" ));
    add( new BigEndianProcessor( "HCS08" ));
    add( new S3F80Processor());
  }


  /**
   * Gets the processor.
   * 
   * @param name the name
   * @param version the version
   * 
   * @return the processor
   */
  public static Processor getProcessor( String name, String version )
  {
    String lookup = name;
    if ( version != null )
      lookup = name + '-' + version;
    return processorManager.processors.get( lookup );
  }

  /**
   * Gets the processor.
   * 
   * @param text the text
   * 
   * @return the processor
   */
  public static Processor getProcessor( String text )
  {
    String name = "";
    if ( text.startsWith( "S3C8" ))
      name = "S3C80";
    else if ( text.startsWith( "M6805" ))
      name = text.substring( 1 );
    else if ( text.equals( "P8/740" ))
      name = "740";
    else
      name = text;
    return processorManager.processors.get( name );
  }

  /**
   * Gets the processor names.
   * 
   * @return the processor names
   */
  public static Set< String > getProcessorNames()
  {
    return processorManager.processors.keySet();
  }

  /**
   * Adds the.
   * 
   * @param p the p
   */
  private void add( Processor p )
  {
    processors.put( p.getFullName(), p );
  }

  /**
   * Gets the processors.
   * 
   * @return the processors
   */
  public static Processor[] getProcessors()
  {
    Collection< Processor > procs = processorManager.processors.values();
    Processor[] rc = new Processor[ procs.size()];
    rc = ( Processor[] )procs.toArray( rc );
    return rc;
  }

  /** The processor manager. */
  private static ProcessorManager processorManager = new ProcessorManager();
  
  /** The processors. */
  private LinkedHashMap< String, Processor > processors = null;
}
