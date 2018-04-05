package com.hifiremote.jp1;

import java.util.Calendar;

public class DaySchedule
{
  private static final int DAY0 = Calendar.MONDAY;
  private static final String[] dayNames = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
  
  public DaySchedule()
  {
    map = 0;
  }
  
  public DaySchedule( int map )
  {
    this.map = map;
  }
  
  private int dayIndex( int day )
  {
    return ( ( day - DAY0 + 7 ) % 7 );
  }
  
  public boolean isSet( int day )
  {
    return ( ( map >> dayIndex( day ) ) & 1 ) == 1;
  }
  
  public boolean isSet7Days()
  {
    return ( map & 0x7F ) == 0x7F;
  }
  
  
  public void set( int day, boolean setSelected )
  {
    int mask = 1 << dayIndex( day );
    if ( setSelected )
    {  
      map |= mask;
    }
    else
    {
      map &= 0xFF ^ mask;
    }
  }
  
  public void set7Days()
  {
    map |= 0x7F;
  }
  
  public void setWeeklyRepeat( boolean weekly )
  {
    map &= 0x7F;
    if ( weekly )
    {
      map |= 0x80;
    }
  }
  
  public boolean isWeeklyRepeat()
  {
    return ( map & 0x80 ) == 0x80;
  }
  
  public boolean isDaily()
  {
    return ( map == 0xFF );
  }
  
  public int getFirstDay()
  {
    int days = map & 0x7F;
    int index = 0;
    while ( index < 7 && ( days & 1 ) == 0 )
    {
      days >>= 1;
      index++;
    }
    return Calendar.MONDAY + index;
  }
  
  public void clear()
  {
    map = 0;
  }
  
  @Override
  public String toString()
  {
    if ( map == 0xFF )
    {
      return "Daily";
    }
    
    StringBuilder buff = new StringBuilder();
    if ( isWeeklyRepeat() )
    {
      buff.append( "Every " );
    }
    else
    {
      buff.append( "Next " );
    }
    
    boolean first = true;
    for ( int i = 0; i < 7; i++ )
    {
      int mask = 1 << i;
      if ( ( map & mask ) > 0 )
      {
        buff.append( first ? dayNames[i] : ";" + dayNames[i] );
        first = false;
      }
    }
    
    return buff.toString();    
  }
  
  public int getMap()
  {
    return map;
  }

  public void setMap( int map )
  {
    this.map = map;
  }
  
  private int map = 0;
  
}
