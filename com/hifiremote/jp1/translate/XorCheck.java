package com.hifiremote.jp1.translate;

import java.util.ArrayList;
import java.util.List;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class XorCheck.
 */
public class XorCheck extends Translate
{
  public XorCheck( String[] textParms )
  {
    super( textParms );
    
    // If semicolon present, split the parameter that contains it.
    List< String > parms = new ArrayList< String >();
    for ( String str : textParms )
    {
      int ndx = str.indexOf( ';' );
      if ( ndx >= 0 )
      {
        count = 0;  // change default count from 1 to 0 when semicolon present
        String part = str.substring( 0, ndx ).trim();
        if ( !part.isEmpty() )
        {
          parms.add( part );
        }
        parms.add( ";" );
        part = str.substring( ndx + 1 ).trim();
        if ( !part.isEmpty() )
        {
          parms.add( part );
        }
      }
      else
      {
        parms.add( str.trim() );
      }
    }

    int parmIndex = 0;
    boolean semicolon = false;
    for ( String text : parms )
    {
      if ( text.equalsIgnoreCase( "lsb" ) )
      {
        lsb = true;
      }
      else if ( text.equalsIgnoreCase( "comp" ) )
      {
        comp = true;
      }
      else if ( text.equals( ";" ) )
      {
        parmIndex = devCountIndex;
        semicolon = true;
      }
      else
      {
        if ( parmIndex >= devCountIndex && !semicolon )
        {
          // Parameter list should only continue to device parms after a semicolon
          break;
        }
        
        int val = Integer.parseInt( text );
        switch ( parmIndex )
        {
          case bitsIndex:
            bits = val;
            step = val;
            sourceOffset = 8 - val;
            devStep = val;
            break;
          case destOffsetIndex:
            destOffset = val;
            sourceOffset = val - bits;
            break;
          case seedIndex:
            seed = val;
            break;
          case countIndex:
            count = val;
            sourceOffset = destOffset - val * bits;
            break;
          case sourceOffsetIndex:
            sourceOffset = val;
            break;
          case stepIndex:
            step = val;
            devStep = val;
            break;
          case devCountIndex:
            devCount = val;
            break;
          case devSourceOffsetIndex:
            devSourceOffset = val;
            break;
          case devStepIndex:
            devStep = val;
            break;
          default:
            break;
        }
        parmIndex++ ;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#in(com.hifiremote.jp1.Value[], com.hifiremote.jp1.Hex,
   * com.hifiremote.jp1.DeviceParameter[], int)
   */
  @Override
  public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
  {
    // System.err.println("XorCheck(" + bits +","+ destOffset +","+ seed +","+ count +","+ sourceOffset +","+ step
    // +";"+ devCount +","+ devSourceOffset +","+ devStep +").in(" + hex.length +")");
    Hex devHex = null;
    if ( devCount > 0 )
    {
      devHex = new Hex( devParms.length );
      for ( int i = 0; i < devParms.length; i++ )
      {
        Object parmValue = devParms[ i ].getValueOrDefault();
        if ( parmValue == null || !( parmValue instanceof Number ) )
        {
          System.err.println( "XorCheck.in() device index=" + i + " missing parameter value" );
        }
        else
        {
          Number n = ( Number )parmValue;
          int w = n.intValue();
          if ( comp )
          {
            w = 0xFF - w;
          }
          if ( lsb )
          {
            w = reverse( w, 8 );
          }
          devHex.getData()[ i ] = ( short )w;
        }
      }
    }

    int v = seed;
    int s = sourceOffset;
    for ( int i = 0; i < count; i++ )
    {
      v ^= extract( hexData, s, bits );
      s += step;
    }
    s = devSourceOffset;
    for ( int i = 0; i < devCount; i++ )
    {
      v ^= extract( devHex, s, bits );
      s += devStep;
    }
    insert( hexData, destOffset, bits, v );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Translate#out(com.hifiremote.jp1.Hex, com.hifiremote.jp1.Value[],
   * com.hifiremote.jp1.DeviceParameter[])
   */
  @Override
  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {}

  private int bits = 8;
  private int destOffset = 8;
  private int seed = 0;
  private int count = 1;
  private int sourceOffset = 0;
  private int step = 8;
  private int devCount = 0;
  private int devSourceOffset = 0;
  private int devStep = 8;

  private boolean lsb = false;
  private boolean comp = false;
  
  private final static int bitsIndex = 0;
  private final static int destOffsetIndex = 1;
  private final static int seedIndex = 2;
  private final static int countIndex = 3;
  private final static int sourceOffsetIndex = 4;
  private final static int stepIndex = 5;
  private final static int devCountIndex = 6;
  private final static int devSourceOffsetIndex = 7;
  private final static int devStepIndex = 8;
}
