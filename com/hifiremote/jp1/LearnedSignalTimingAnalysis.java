package com.hifiremote.jp1;

public class LearnedSignalTimingAnalysis
{
  private String _Name;
  private String _Message;
  private int[] _Bursts;
  private int[][] _OneTimeDurations;
  private int[][] _RepeatDurations;
  private int[][] _ExtraDurations;
  private String _Separator;
  private int _SeparatorFirst;
  private int _SeparatorInterval;

  public String getName() { return _Name; }
  public String getMessage() { return _Message; }
  public int[] getBursts() { return _Bursts; }
  public int[][] getOneTimeDurations() { return _OneTimeDurations; }
  public int[][] getRepeatDurations() { return _RepeatDurations; }
  public int[][] getExtraDurations() { return _ExtraDurations; }

  public LearnedSignalTimingAnalysis( String name, int[] bursts, int[][] oneTime, int[][] repeat, int[][] extra, String sep, int sepFirst, int sepInterval, String message )
  {
    _Name = name;
    _Bursts = bursts;
    _OneTimeDurations = oneTime;
    _RepeatDurations = repeat;
    _ExtraDurations = extra;
    _Separator = sep;
    _SeparatorFirst = sepFirst;
    _SeparatorInterval = sepInterval;
    _Message = message;
  }

  private String[] makeDurationStringList( int[][] durations )
  {
    int r = 0;
    String[] results = new String[durations.length];
    for ( int[] d: durations )
      results[r++] = durationsToString( d, _Separator, _SeparatorFirst, _SeparatorInterval );
    return results;
  }
  public String[] getOneTimeDurationStringList()
  {
    return makeDurationStringList( getOneTimeDurations() );
  }
  public String[] getRepeatDurationStringList()
  {
    return makeDurationStringList( getRepeatDurations() );
  }
  public String[] getExtraDurationStringList()
  {
    return makeDurationStringList( getExtraDurations() );
  }

  public String getBurstString()
  {
    return durationsToString( getBursts(), _Separator, _SeparatorFirst, _SeparatorInterval );
  }
  public String getOneTimeDurationString()
  {
    return durationsToString( joinDurations( getOneTimeDurations() ), _Separator, _SeparatorFirst, _SeparatorInterval );
  }
  public String getRepeatDurationString()
  {
    return durationsToString( joinDurations( getRepeatDurations() ), _Separator, _SeparatorFirst, _SeparatorInterval );
  }
  public String getExtraDurationString()
  {
    return durationsToString( joinDurations( getExtraDurations() ), _Separator, _SeparatorFirst, _SeparatorInterval );
  }

  public static int[] joinDurations( int[][] durations )
  {
    if ( durations == null || durations.length == 0 )
      return null;

    int num = 0;
    for ( int[] d: durations )
      num += d.length;

    int r = 0;
    int[] result = new int[num];
    for ( int[] duration: durations )
      for ( int d: duration )
        result[r++] = d;

    return result;
  }

  public static String durationsToString( int[] data, String sep, int sepFirst, int sepInterval )
  {
    StringBuilder str = new StringBuilder();
    if ( data != null && data.length != 0 )
    {
      boolean isSigned = false;
      for ( int d: data )
        if ( d < 0 )
        {
          isSigned = true;
          break;
        }

      for ( int i = 0; i < data.length; i++ )
      {
        if ( i > 0 )
          str.append( ' ' );
        if ( !isSigned )
          str.append( ( i & 1 ) == 0 ? "+" : "-" );
        else if ( data[i] > 0 )
            str.append( '+' );
        str.append( data[i] );
        if ( (i+1) == sepFirst || ( (i+1-sepFirst) % sepInterval ) == 0 )
          str.append( sep );
      }
    }
    if ( str.length() == 0 )
      return "** No signal **";

    return str.toString();
  }
}
