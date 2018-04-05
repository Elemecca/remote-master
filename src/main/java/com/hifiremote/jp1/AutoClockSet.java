package com.hifiremote.jp1;

import java.util.Calendar;
import java.util.StringTokenizer;

public class AutoClockSet extends RDFParameter
{
  public enum TimeFormat
  {
    HEX, BCD12, BCD24
  }
  
  /** The time address. */
  private int timeAddress = 0;
  private TimeFormat timeFormat = TimeFormat.HEX;
  private short savedTimeBytes[] = new short[3];
  
  
  public void saveTimeBytes( short[] data )
  {
    for ( int i = 0; i < 3; i++ )
    {
      savedTimeBytes[i] = data[ timeAddress + i ];
    }
  }
  
  public void restoreTimeBytes( short[] data )
  {
    for ( int i = 0; i < 3; i++ )
    {
      data[ timeAddress + i ] = savedTimeBytes[i];
    }
  }

  public void setTimeBytes( short[] data )
  {
    Calendar now = Calendar.getInstance();
    int minValue = now.get( Calendar.MINUTE );
    int hourValue = now.get( Calendar.HOUR );
    int pmValue = ( now.get(  Calendar.AM_PM ) == Calendar.PM ) ? 1 : 0;
    int dayValue = ( now.get( Calendar.DAY_OF_WEEK ) + 7 - Calendar.MONDAY ) % 7;

    switch ( timeFormat )
    {
      case HEX:
        data[ timeAddress ] = ( short )( minValue + 0x80 * pmValue );
        data[ timeAddress + 1 ] = ( short )hourValue;
        data[ timeAddress + 2 ] = ( short )( 1 << dayValue );
        break;
        
      case BCD12:
        data[ timeAddress ] = ( short )( ( ( minValue / 10 ) << 4 ) + ( minValue % 10 ) );
        data[ timeAddress + 1 ] = ( short )( ( ( hourValue / 10 ) << 4 ) + ( hourValue % 10 ) 
            + ( pmValue << 5 ));
        data[ timeAddress + 2 ] = ( short )( dayValue + 1 );
        break;
        
      case BCD24:
        hourValue = ( hourValue % 12 ) + 12 * pmValue;
        data[ timeAddress ] = ( short )( ( ( minValue / 10 ) << 4 ) + ( minValue % 10 ) );
        data[ timeAddress + 1 ] = ( short )( ( ( hourValue / 10 ) << 4 ) + ( hourValue % 10 ) );
        data[ timeAddress + 2 ] = ( short )( dayValue + 1 );
        break;

      default:
        break;
    }
  }
  
  @Override
  public void parse( String text, Remote remote ) throws Exception
  {
    StringTokenizer st = new StringTokenizer( text, ", " );
    timeAddress = RDFReader.parseNumber( st.nextToken() );
    if ( st.hasMoreTokens() )
      timeFormat = TimeFormat.valueOf( st.nextToken() );
    
  }
}
