package com.hifiremote.jp1;

public class AssemblerOpCode implements Cloneable
{
  private String name = null;
  private AddressMode mode = null;
  private int index = 0;       // Index to next list if multi-byte opcode
  
  public AssemblerOpCode(){};
  
  public AssemblerOpCode( Processor p, String[] parms )
  {
    name = parms[ 0 ];
    mode = p.getAddressModes().get( parms[ 1 ] );
    if ( mode == null )
    {
      mode = new AddressMode();
    }
    if ( parms.length > 2 )
    {
        index = Integer.parseInt( parms[ 2 ] ); 
    }             
  }
  
  public static class AddressMode
  {
    public String name = "";
    public int length = 0;        // Number of argument bytes   
    public int relMap = 0;        // Which arg bytes are relative addresses
    public int nibbleMap = 0;     // Which data nibbles are used as args
    public int ccMap = 0;         // Which data nibbles are mapped to condition codes
    public int nibbleBytes = 0;   // (Calculated) number or arg bytes split into nibbles
    public String format = "";    // Print format
    
    public AddressMode(){};

    public AddressMode( String[] parms )
    {
      name = parms[ 0 ];
      String s = parms[ 1 ];
      int posB = s.indexOf( "B" );
      int posN = s.indexOf( "N" );
      if ( s.startsWith( "C" ) )
      {
        // N must also be present
        ccMap = Integer.parseInt( s.substring( 1, posN ) );
      }
      if ( posN >= 0 )
      {
        nibbleMap = ( posB < 0 ) ? Integer.parseInt( parms[ 1 ].substring( posN + 1 ) ) :
          Integer.parseInt( parms[ 1 ].substring( posN + 1, posB ) );

        int i = 2;
        while ( nibbleMap >> i != 0 )
        {
          nibbleBytes++;
          i += 2;
        }
        length = nibbleBytes;
      }
      if ( posB >= 0 )
      {
        length += Integer.parseInt( parms[ 1 ].substring( posB + 1 ) );
      }
      format = parms[ 2 ];
      if ( parms.length > 3 )
      {
        relMap = Integer.parseInt( parms[ 3 ] );
      }
    }
  }
  
  @Override
  public AssemblerOpCode clone()
  {
    AssemblerOpCode opCode = new AssemblerOpCode();
    opCode.name = this.name;
    opCode.mode = this.mode;
    opCode.index = this.index;
    return opCode;
  }
  
  
  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public AddressMode getMode()
  {
    return mode;
  }

  public void setMode( AddressMode mode )
  {
    this.mode = mode;
  }

  public int getIndex()
  {
    return index;
  }

  public void setIndex( int index )
  {
    this.index = index;
  }

}

