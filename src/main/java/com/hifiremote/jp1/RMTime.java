package com.hifiremote.jp1;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RMTime
{
   public RMTime()
  {  
    try
    {
      date = RMTime.timeFormat.parse( "00:00" );
    }
    catch ( ParseException e )
    {
      e.printStackTrace();
    }
  }
  
  public RMTime( Date date )
  {
    this.date = date;
  }
  
  public Date get()
  {
    return date;
  }

  public void set( Date date )
  {
    this.date = date;
  }

  public String toString()
  {
    return timeFormat.format( date );
  }
  
  public static final DateFormat timeFormat = new RMTimeFormat( "HH:mm" );
  
  private static final class RMTimeFormat extends SimpleDateFormat
  {
    RMTimeFormat( String pattern )
    {
      super( pattern );
      setLenient( false );
    }
  }

  Date date = null;

}
