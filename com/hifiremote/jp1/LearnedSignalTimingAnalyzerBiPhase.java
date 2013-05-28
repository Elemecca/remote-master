package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.HashMap;

public class LearnedSignalTimingAnalyzerBiPhase extends LearnedSignalTimingAnalyzerBase
{
  private int _Unit;
  private String _PreferredName;

  public LearnedSignalTimingAnalyzerBiPhase( UnpackLearned u )
  {
    super( u );
  }

  @Override
  public String getName()
  {
    return "Bi-Phase";
  }

  private HashMap<Integer,Integer> getDurationHistogram( int roundTo )
  {
    int[] durations = getUnpacked().getDurations( roundTo, true );
    HashMap<Integer,Integer> hist = new HashMap<Integer,Integer>();

    int leadIn1 = durations[0];
    int leadIn2 = durations[1];
    for ( int i = 2; i < durations.length - 2; i++ )
    {
      int value = durations[i];
      int absValue = Math.abs( value );
      if ( !hist.containsKey( absValue ) )
      {
        // check for a lead out
        if ( value < 0 && durations[i+1] == leadIn1 && durations[i+2] == leadIn2 )
        {
          i += 2;
          continue;
        }
        hist.put( absValue, 1 );
      }
      else
        hist.put( absValue, hist.get( absValue ) + 1 );
    }

    return hist;
  }

  @Override
  protected int calcAutoRoundTo()
  {
    HashMap<Integer,Integer> hist = getDurationHistogram( 1 );

    int min = Integer.MAX_VALUE;
    for ( int k: hist.keySet() )
      if ( k < min )
        min = k;

    int limit = min + (int) ( Math.pow( 10, Math.floor( Math.log10( min ) ) ) / 2 );

    int roundTo = 0;
    while ( roundTo < limit )
    {
      roundTo += 10;
      if ( checkCandidacy( roundTo ) ) // this will trigger an analyze for biphase
        // found a working one, return it
        return roundTo;
    }

    return 0;
  }

  @Override
  protected int checkCandidacyImpl( int roundTo )
  {
    HashMap<Integer,Integer> hist = getDurationHistogram( roundTo );

    int min = Integer.MAX_VALUE;
    for ( int d: hist.keySet() )
      if ( d < min )
        min = d;

    if ( min <= 0 )
      return 0; // obviously no good

    for ( int d: hist.keySet() )
      if ( d % min != 0 )
        return 0;

    // so we might good...but we dunno until we try...
    return 1;
  }

  private int _SavedUnit;
  @Override
  public void saveState()
  {
    _SavedUnit = _Unit;
    super.saveState();
  }
  @Override
  public void restoreState()
  {
    super.restoreState();
    _Unit = _SavedUnit;
  }

  @Override
  protected void analyzeImpl()
  {
    HashMap<Integer,Integer> hist = getDurationHistogram( getRoundTo() );
    _Unit = Integer.MAX_VALUE;
    for ( int d: hist.keySet() )
      if ( d < _Unit )
        _Unit = d;

    System.err.println( "BiPhaseAnalyzer: (" + this.hashCode() +") Analyze beginning with rounding of " + getRoundTo() + " yielding unit size of " + _Unit + "..." );

    HashMap<String,int[][]> oneTime = AnalyzeDurationSet( getUnpacked().getOneTimeDurations( getRoundTo(), true ) );
    HashMap<String,int[][]> repeat = AnalyzeDurationSet( getUnpacked().getRepeatDurations( getRoundTo(), true ) );
    HashMap<String,int[][]> extra = AnalyzeDurationSet( getUnpacked().getExtraDurations( getRoundTo(), true ) );

    HashMap<String,Integer> codes = new HashMap<String,Integer>();
    if ( oneTime != null )
      for ( String k: oneTime.keySet() )
        codes.put( k, 0 );
    if ( repeat != null )
      for ( String k: repeat.keySet() )
        codes.put( k, 0 );
    if ( extra != null )
      for ( String k: extra.keySet() )
        codes.put( k, 0 );

    String preferredCode = null;
    String preferredName = null;

    // codes.keySet() is all the unique analysis codes
    for ( String code: codes.keySet() )
    {
      boolean valid = ( oneTime == null || oneTime.containsKey( code ) );
      valid = valid && ( repeat == null || repeat.containsKey( code ) );
      valid = valid && ( extra == null || extra.containsKey( code ) );

      if ( valid )
      {
        int[][] tempOneTime = ( oneTime == null ? null : oneTime.get( code ) );
        int[][] tempRepeat = ( repeat == null ? null : repeat.get( code ) );
        int[][] tempExtra = ( extra == null ? null : extra.get( code ) );

        String[] codeSplit = code.split( "," );

        String msg = "Bi-Phase unit size is " + _Unit + ".";
        String name = "LI " + codeSplit[0] + " LO " + codeSplit[2] + " " + ( codeSplit[1].equals("1") ? "ODD" : "EVEN" );

        addAnalysis( new LearnedSignalTimingAnalysis( name, getUnpacked().getBursts( getRoundTo() ), tempOneTime, tempRepeat, tempExtra, ";", ( codeSplit[1].equals("1") ? 1 : 2 ), 2, msg ) );

        if ( preferredCode == null || code.compareTo( preferredCode ) < 0 )
        {
          preferredCode = code;
          preferredName = name;
        }
      }
    }

    _PreferredName = preferredName;
    System.err.println( "BiPhaseAnalyzer: analyzeImpl complete yielding " + getAnalyses().size() + " analyses preferring '" + _PreferredName + "'." );    
  }

  // return is dictionary of analysis codes to a set of analyzed durations from the split signal
  // analysis codes are of form "i,s,o":
  //  i = number of units taken from lead in off time to produce first pair
  //  s = ( separateOdd ? 1 : 0 )
  //  o has following meaning:
  //    0 = analysis ended in complete pairs, lead out unchanged
  //    1 = off time for final pair was taken from lead out
  //    2 = final on time was used as part of lead out
  private HashMap<String,int[][]> AnalyzeDurationSet( int[] durations )
  {
    /*
    if ( durations == null || durations.length == 0 )
      System.err.println( "BiPhaseAnalyzer: AnalyzeDurationSet with " + ( durations == null ? "null" : "empty" ) + " set." );
    else if ( durations.length > 3 )
      System.err.println( "BiPhaseAnalyzer: AnalyzeDurationSet with set of " + durations.length + " durations... ( " + durations[0] + " " + durations[1] + " " + durations[2] + " " + durations[3] + " ... )" );
    else
      System.err.println( "BiPhaseAnalyzer: AnalyzeDurationSet with set of " + durations.length + " durations..." );
    */

    if ( durations == null || durations.length < 4 )
      return null;

    int[][] temp = splitDurationsBeforeLeadIn( durations );
    HashMap<String,int[][]> results = new HashMap<String,int[][]>();

    int i = 0;
    HashMap<String,int[]> tempResults = null;
    for ( int[] t: temp )
    {
      tempResults = AnalyzeDurations( t );
      // if we got no results for this split component, why bother with the rest
      if ( tempResults == null || tempResults.size() == 0 )
      {
        results.clear();
        return results;
      }
      for ( String k: tempResults.keySet() )
      {
        if ( !results.containsKey( k ) )
          results.put( k, new int[temp.length][] );
        results.get( k )[i] = tempResults.get( k );
      }
      i++;
    }

    return results;
  }

  // return is dictionary of analysis codes to durations for a single split component of the signal
  // analysis codes are of form "i,s,o":
  //  i = number of units taken from lead in off time to produce first pair
  //  s = ( separateOdd ? 1 : 0 )
  //  o has following meaning:
  //    0 = analysis ended in complete pairs, lead out unchanged
  //    1 = off time for final pair was taken from lead out
  //    2 = final on time was used as part of lead out
  private HashMap<String,int[]> AnalyzeDurations( int[] durations )
  {
    /*
    if ( durations == null || durations.length == 0 )
      System.err.println( "BiPhaseAnalyzer: AnalyzeDurations with " + ( durations == null ? "null" : "empty" ) + " set." );
    else if ( durations.length > 3 )
      System.err.println( "BiPhaseAnalyzer: AnalyzeDurations with set of " + durations.length + " durations... ( " + durations[0] + " " + durations[1] + " " + durations[2] + " " + durations[3] + " ... )");
    else
      System.err.println( "BiPhaseAnalyzer: AnalyzeDurations with set of " + durations.length + " durations...");
    */

    if ( durations == null || durations.length < 4 )
      return null;

    int[] leadIn = new int[2];
    leadIn[0] = durations[0];
    leadIn[1] = durations[1];

    int leadOut = durations[durations.length -1];

    // setup temp array used for analysis
    // we leave 0th spot blank to hold partial lead in off time later
    int[] temp = new int[durations.length - 2];
    for ( int i = 2; i < durations.length - 1; i++ )
      temp[i-1] = durations[i];

    HashMap<String,int[]> results = new HashMap<String,int[]>();
    HashMap<String,ArrayList<int[]>> tempResults = null;

    // we're going to try all possibilities for dividing up the off time of the lead in
    // first, let's find out how many iterations that will take
    int num = Math.abs( leadIn[1] ) / _Unit;
    if ( num > Math.abs( temp[1] ) / _Unit )
      num = Math.abs( temp[1] ) / _Unit;

    // now try them all
    for ( int n = 0; n <= num; n++ )
    {
      temp[0] = -1 * _Unit * n;
      tempResults = analyzeSignalData( temp, leadOut );
      if ( tempResults != null )
      {
        int leadIn1 = leadIn[1] + ( _Unit * n );
        // we analyzed successfully with at least 1 result, so append to results
        for ( String k: tempResults.keySet() )
        {
          String code = Integer.toString( n ) + "," + ( leadIn1 == 0 ? 1 : 0 ) + "," + k;
          results.put( code, mergeAnalysisResult( leadIn[0], leadIn1, tempResults.get( k ), k ) );
        }
      }
    }

    return results;
  }

  private int[] mergeAnalysisResult( int leadIn0, int leadIn1, ArrayList<int[]> pairs, String code )
  {
    int[] data = new int[pairs.size() * 2 + ( code == "2" ? 2 : 1 )];
    int i = 0;
    data[i++] = leadIn0;
    if ( leadIn1 != 0 )
      data[i++] = leadIn1;
    for ( int[] r: pairs )
    {
      if ( r[0] != 0 )
        data[i++] = r[0];
      if ( r[1] != 0 )
        data[i++] = r[1];
    }
    return data;
  }

  // return is dictionary of 'o' part of analysis codes to list of logical pairs for a single split component of the signal
  // analysis codes are of form "i,s,o":
  //  i = number of units taken from lead in off time to produce first pair
  //  s = ( separateOdd ? 1 : 0 )
  //  o has following meaning:
  //    0 = analysis ended in complete pairs, lead out unchanged
  //    1 = off time for final pair was taken from lead out
  //    2 = final on time was used as part of lead out
  private HashMap<String,ArrayList<int[]>> analyzeSignalData( int[] durations, int leadOut )
  {
    /*
    if ( durations == null || durations.length == 0 )
      System.err.println( "BiPhaseAnalyzer: analyzeSignalData with " + ( durations == null ? "null" : "empty" ) + " set." );
    else if ( durations.length > 3 )
      System.err.println( "BiPhaseAnalyzer: analyzeSignalData with set of " + durations.length + " durations... ( " + durations[0] + " " + durations[1] + " " + durations[2] + " " + durations[3] + " ... )" );
    else
      System.err.println( "BiPhaseAnalyzer: analyzeSignalData with set of " + durations.length + " durations..." );
    */

    ArrayList<int[]> result = new ArrayList<int[]>();

    int[] p = null;
    for ( int d: durations )
    {
      if ( d== 0 ) continue;

      //if ( p == null )
      //  System.err.println( "CurrentPair = (), Next = " + d );
      //else
      //  System.err.println( "CurrentPair = ( " + p[0] + ", ??? ), Next = " + d );

      // starting a new pair
      if ( p == null )
      {
        p = new int[2];
        p[0] = d;
      }
      // next finishes our current pair
      else if ( p[0] == -d )
      {
        p[1] = d;
        result.add( p );
        //System.err.println( "Adding pair ( " + p[0] + ", " + p[1] + " )" );
        p = null;
      }
      // next needs to be split to finish our pair and start the next
      else if ( Math.abs( p[0] ) < Math.abs( d ) && Math.abs( d ) % Math.abs( p[0] ) == 0 )
      {
        p[1] = -p[0];
        result.add( p );
        //System.err.println( "Adding pair ( " + p[0] + ", " + p[1] + " )" );
        d += p[0];
        p = new int[2];
        p[0] = d;
      }
      // error...unable to parse input durations as bi-phase
      else
      {
        return null;
      }
    }

    //System.err.println( "BiPhaseAnalyzer: Found " + result.size() + " result pairs..." );

    // if we ended on a complete pair, just tack on lead out and we're done
    if ( p == null )
    {
      p = new int[2];
      p[0] = leadOut;
      p[1] = 0;
      result.add( p );

      HashMap<String,ArrayList<int[]>> results = new HashMap<String,ArrayList<int[]>>();
      results.put( "0", result );
      return results;
    }

    // we have an unfinished pair, so we have two options:
    //  1) finish last pair from the lead out
    //  2) assume last + is part of lead out

    // clone is OK here because they will always and forever have the same pairs up to this point
    @SuppressWarnings( "unchecked" )
    ArrayList<int[]> result2 = (ArrayList<int[]>)result.clone();

    // result is finish last pair from the lead out
    p[1] = -p[0];
    result.add( p );
    int[] lo = new int[2];
    lo[0] = leadOut + p[0];
    lo[1] = 0;
    result.add( lo );

    // result2 is last + is part of lead out
    int[] lo2 = new int[2];
    lo2[0] = p[0];
    lo2[1] = leadOut;
    result2.add( lo2 );

    HashMap<String,ArrayList<int[]>> results = new HashMap<String,ArrayList<int[]>>();
    results.put( "1", result );
    results.put( "2", result2 );
    return results;
  }

  @Override
  protected String getPreferredAnalysisName()
  {
    return _PreferredName;
  }
}
