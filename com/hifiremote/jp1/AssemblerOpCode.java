package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;

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
    public int absMap = 0;        // Which data bytes start a 2-byte absolute address
    public int zeroMap = 0;       // Which data bytes are zero-page or register addresses
    public int nibbleBytes = 0;   // (Calculated) number or arg bytes split into nibbles
    public String format = "";    // Print format
    
    public AddressMode(){};

    public AddressMode( String[] parms )
    {
      List< Integer > keyPositions = new ArrayList< Integer >(); 
      name = parms[ 0 ];
      String s = parms[ 1 ];
      for ( int i = 0; i < s.length(); i++ )
      {
        if ( !Character.isDigit( s.charAt( i ) ) )
        {
          keyPositions.add( i );
        }
      }
      keyPositions.add(  s.length() );
      for ( int i = 0; i < keyPositions.size() - 1; i++ )
      {
        String key = s.substring( keyPositions.get( i ), keyPositions.get( i ) + 1 );
        String val = s.substring( keyPositions.get( i ) + 1, keyPositions.get( i + 1 ) );
        if ( key.equals( "C" ) )
        {
          ccMap = Integer.parseInt( val );
        }
        else if ( key.equals( "N" ) )
        {
          nibbleMap = Integer.parseInt( val );
          for ( int n = 2; nibbleMap >> n != 0; n += 2 )
          {
            nibbleBytes++;
          }
          length += nibbleBytes;
        }
        else if ( key.equals( "B" ) )
        {
          length += Integer.parseInt( val );
        }
        else if ( key.equals( "R" ) )
        {
          relMap = Integer.parseInt( val );
        }
        else if ( key.equals( "A" ) )
        {
          absMap = Integer.parseInt( val );
        }
        else if ( key.equals( "Z" ) )
        {
          zeroMap = Integer.parseInt( val );
        }
      }
      format = parms[ 2 ];
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

