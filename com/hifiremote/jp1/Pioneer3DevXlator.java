package com.hifiremote.jp1;

import javax.swing.*;

public class Pioneer3DevXlator
  extends Translator
{
  private static int devIndex = 0;
  private static int obcIndex = 1;
  private static int obc2Index = 2;

  public Pioneer3DevXlator( String[] textParms )
  {
    super( textParms );
  }

  private int getDevice( Hex hex )
  {
    return reverse( extract( hex, 8, 2 ), 2 ) - 1;
  }

  private int getDevice( Value[] parms )
  {
    if (( parms[ devIndex ] == null ) ||
        ( parms[ devIndex ].getValue() == null ))
      return 0;
    return (( Integer )parms[ devIndex ].getValue()).intValue();
  }

  private void setDevice( int device, Hex hex )
  {
    insert( hex, 8, 2, reverse( device + 1, 2 ));
  }

  private int getObc( Hex hex )
  {
    return reverse( extract( hex, 0, 8 ));
  }

  private int getObc( Value[] parms )
  {
    return (( Integer )parms[ obcIndex ].getValue()).intValue();
  }

  private void setObc( int obc, Hex hex )
  {
    insert( hex, 0, 8, reverse( obc ));
  }

  private int adjust( int obc, int obc2 )
  {
    if (( obc & 0x80 ) != 0 )
      obc2 += 0x80;
    if (( obc & 0x40 ) == 0 )
      obc2 += 0x40;
    return obc2;
  }

  private Integer getObc2( Hex hex )
  {
    int obc2 = reverse( extract( hex, 11, 5 ), 5 );
    if ( obc2 == 0 )
      return null;
    else
    {
      int obc = getObc( hex );
      return new Integer( adjust( obc, obc2 ));
    }
  }

  private Integer getObc2( Value[] parms )
  {
    if (( parms[ obc2Index ] == null ) ||
        ( parms[ obc2Index ].getValue() == null ))
      return null;
    return ( Integer )parms[ obc2Index ].getValue();
  }

  private void setObc2( Integer obc2, Hex hex )
  {
    if ( obc2 != null )
    {
      int val = obc2.intValue();
      insert( hex, 11, 5, reverse( val, 5 ));
      Integer temp = getObc2( hex );
      if ( !obc2.equals( temp ))
        JOptionPane.showMessageDialog( KeyMapMaster.getKeyMapMaster(),
                                       "The combination of OBC and OBC2 values you have entered " +
                                       "are not supported by the Pioneer 3DEV protocol.  You " +
                                       "should use the Pioneer 4DEV protocol instead.",
                                       "Value not supported.",
                                       JOptionPane.ERROR_MESSAGE );
    }
    else
      insert( hex, 11, 5, 0 );
  }

  public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
  {
    boolean doAll = ( onlyIndex < 0 );
    if (( onlyIndex == devIndex ) || doAll )
    {
      setDevice( getDevice( parms ), hex );
    }
    if (( onlyIndex == obcIndex ) || doAll )
    {
      int obc = getObc( parms );
      setObc( obc, hex );
    }
    if (( onlyIndex == obc2Index ) || doAll )
    {
      setObc2( getObc2( parms ), hex );
    }
  }

  public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
  {
    parms[ devIndex ] = new Value( new Integer( getDevice( hex )));
    int obc = getObc( hex );
    parms[ obcIndex ] = new Value( new Integer( obc ));
    Integer obc2 = getObc2( hex );
    parms[ obc2Index ] = new Value( obc2 );
  }

}
