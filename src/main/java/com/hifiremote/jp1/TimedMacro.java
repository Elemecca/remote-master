package com.hifiremote.jp1;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class TimedMacro extends AdvancedCode
{
  public TimedMacro( DaySchedule daySchedule, RMTime time, Hex hex, String notes )
  {
    super( 0, hex, notes );
    this.daySchedule = daySchedule;
    this.time = time;
  }
  
  public TimedMacro( int dayHour, int minuteByte, Hex hex, String notes )
  {
    // Used when Timed Macros are in the Advanced Codes section.
    super( 0, hex, notes );
    
    Calendar c = Calendar.getInstance();
    c.set( Calendar.HOUR_OF_DAY, dayHour & 0x1F );
    c.set( Calendar.MINUTE, minuteByte & 0x3F );
    time.set( c.getTime() );
    
    dayHour >>= 5;  
    if ( dayHour == 0 )
    {
      daySchedule.set7Days();
      daySchedule.setWeeklyRepeat( true );
    }
    else
    {
      daySchedule.set( Calendar.MONDAY + dayHour - 1, true );
      daySchedule.setWeeklyRepeat( ( minuteByte & 0x40 ) == 0x40 );
    }    
  }

  public TimedMacro( Properties props )
  {
    super( props );
    daySchedule.setMap( Integer.parseInt( props.getProperty( "DaySchedule" ) ) );
    try
    {
      time.set( timeFormat.parse( props.getProperty( "Time" ) ) );
    }
    catch ( ParseException pe )
    {
      pe.printStackTrace( System.err );
    }
  }
  
  public static TimedMacro read( HexReader reader, Remote remote )
  {
    // Used when Timed Macros are in a section of their own.
    if ( ( reader.available() < 4 ) || ( reader.peek() == remote.getSectionTerminator() ) )
    {
      return null;
    }
    int minuteByte = reader.read();
    int hourLength = reader.read();
    int dayByte = reader.read();
    Hex hex = new Hex( reader.read( hourLength & 0x0F ) );
    boolean isPM = ( ( minuteByte & 0x80 ) == 0x80 );
    hourLength >>= 4;
    minuteByte &= 0x3F;
    
    Calendar c = Calendar.getInstance();
    c.set( Calendar.HOUR, hourLength % 12 ); // Convert hour 12 to 0
    c.set( Calendar.AM_PM, ( isPM ) ? Calendar.PM : Calendar.AM );
    c.set( Calendar.MINUTE, minuteByte );
    
    
    DaySchedule daySchedule = new DaySchedule( dayByte );
    RMTime time = new RMTime( c.getTime() );
    TimedMacro timedMacro = new TimedMacro( daySchedule, time, hex, null );

    return timedMacro;
  }  

  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    short[] keys = data.getData();
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
        buff.append( remote.getButtonName( keys[ i ] ) );
    }
    return buff.toString();
  }

  @Override
  public int store( short[] buffer, int offset, Remote remote )
  {
    Calendar c = Calendar.getInstance();
    c.setTime( time.get() );
    int minuteByte = c.get( Calendar.MINUTE );
    int dataLength = data.length();
    
    if ( remote.getTimedMacroAddress() != null )
    {      
      if ( c.get( Calendar.AM_PM ) == Calendar.PM )
      {
        minuteByte |= 0x80;
      }
      int hourLength = c.get( Calendar.HOUR );
      if ( hourLength == 0 )
      {
        hourLength = 12;
      }
      
      hourLength = ( hourLength << 4 ) + dataLength;
      
      buffer[ offset++ ] = ( short )( minuteByte | 0x40 );
      buffer[ offset++ ] = ( short )hourLength;
      buffer[ offset++ ] = ( short )daySchedule.getMap();
      Hex.put( data, buffer, offset );
      return offset + dataLength;
    }
    else if ( remote.getMacroCodingType().hasTimedMacros() )
    {
      int dayHour = 0;
      if ( !daySchedule.isDaily() )
      {
        dayHour = ( ( daySchedule.getFirstDay() - Calendar.MONDAY + 7 ) % 7 ) + 1;
      }
      if ( daySchedule.isWeeklyRepeat() )
      {
        minuteByte |= 0x40;
      }
      dayHour <<= 5;      
      dayHour |= c.get( Calendar.HOUR_OF_DAY );
      
      buffer[ offset++ ] = ( short )dayHour;
      buffer[ offset++ ] = ( short )( minuteByte | 0x80 );
      buffer[ offset++ ] = ( short )dataLength;
      Hex.put( data, buffer, offset );
      return offset + dataLength;
    }
    
    return offset;
  }
  
  @Override
  public void store( PropertyWriter pw )
  {
    pw.print( "DaySchedule", daySchedule.getMap() );
    pw.print( "Time", timeFormat.format( time.get() ) );
    pw.print( "Data", data );
    if ( ( notes != null ) && ( notes.length() > 0 ) )
      pw.print( "Notes", notes );
  }
  
  @Override
  public int getSize( Remote remote )
  {
    // Timed macros always have 3-byte header.
    return data.length() + 3;
  }
  
  public RMTime getTime()
  {
    return time;
  }

  public void setTime( RMTime time )
  {
    this.time = time;
  }

  public DaySchedule getDaySchedule()
  {
    return daySchedule;
  }
  
  public void setDaySchedule( DaySchedule daySchedule )
  {
    this.daySchedule = daySchedule;
  }

  private RMTime time = new RMTime();
  private DaySchedule daySchedule = new DaySchedule();
  private DateFormat timeFormat = new SimpleDateFormat( "HH:mm" );
}
