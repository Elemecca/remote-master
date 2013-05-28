package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class LearnedSignalTimingAnalyzerBase
{
  // collection of successful analyses
  private HashMap<String,LearnedSignalTimingAnalysis> _Analyses;

  // data from the learned signal
  private UnpackLearned _Unpacked;
  protected UnpackLearned getUnpacked() { return _Unpacked; }

  // rounding info...rounding can be set or it can be automatically determined by the analyzer
  private int _RoundTo = -1;
  public int getRoundTo()
  {
    if ( _RoundTo == -1 )
    {
      _RoundTo = calcAutoRoundTo(); // blind acceptance
      _Analyses = null; // force reanalyze on next access
    }
    return _RoundTo;
  }
  public void setRoundTo( int roundTo )
  {
    if ( !_IsRoundingLocked && roundTo > 0 && _RoundTo != roundTo && checkCandidacy( roundTo ) )
    {
        _RoundTo = roundTo;
        _Analyses = null; // force reanalyze on next access
    }
  }

  private boolean _IsRoundingLocked = false;
  public boolean getIsRoundingLocked() { return _IsRoundingLocked; }
  public void lockRounding() { _IsRoundingLocked = true; }
  public void unlockRounding() { _IsRoundingLocked = false; }

  private int _SavedRoundTo = -1;
  private HashMap<String,LearnedSignalTimingAnalysis> _SavedAnalyses;
  public void saveState()
  {
    _SavedRoundTo = _RoundTo;
    _SavedAnalyses = _Analyses;
  }
  public void restoreState()
  {
    if (_RoundTo != _SavedRoundTo)
    {
      _Analyses = _SavedAnalyses; // must reanalyze if restore changes rounding
      _RoundTo = _SavedRoundTo;
    }
  }

  // simple constructor
  public LearnedSignalTimingAnalyzerBase( UnpackLearned u )
  {
    _Unpacked = u;
  }

  // provide a name for the analyzer
  public abstract String getName();
  // calculate a preferred optimal rounding
  protected abstract int calcAutoRoundTo();
  // do a quick check if the signal can be analyzed with the given rounding
  //  0 = fails check, 1 = must try to analyze to know for sure, 2 = passes check
  protected abstract int checkCandidacyImpl( int roundTo );
  // analyze the symbol
  protected abstract void analyzeImpl();
  // get the preferred analysis that is the "best match"
  protected abstract String getPreferredAnalysisName();

  protected boolean checkCandidacy( int roundTo )
  {
    int c = checkCandidacyImpl( roundTo );

    // check if for sure fail
    if ( c == 0 ) return false;
    // check if for sure success
    if ( c == 2 ) return true;
    // check for insanity
    if ( c != 1 )
    {
      System.err.println( "The checkCandidacyImpl method for analyzer " + this.getClass().getName() + " returned invalid value " + c + ".");
      return false;
    }

    // handle "must analyze" case
    //  we need to save state to "try" to analyze, but we can't use saveState/restoreState because they're being used by the UI
    //  we also have no clue what additional state the analyzer may have which could get destroyed in this...so we just make a new instance
    try
    {
      // getting crazy now...
      LearnedSignalTimingAnalyzerBase a = this.getClass().getConstructor( UnpackLearned.class ).newInstance( _Unpacked );
      // can't use setRoundTo lest we spiral quickly towards a stack overflow
      a._RoundTo = roundTo;
      a.analyze();
      return a.hasAnalyses();
      // It'd be nice if we could steal the analyses here if it was successful, but again we have no idea what
      // state the subclass has... Maybe there could be a "copyInternalState" abstract that they would implement?
      // That seems overkill since most (hopefully?) won't have to use this "analyze to be sure" methodology.
      // If most end up using it, then that should be done so subclasses can maintain their internal state
      // while doing this but don't cause an extra re-analyze.
    }
    // swallowing exceptions...they should never happen, right?   Sorry in advance.
    catch ( Exception e ) { }

    // should never get here... famous last words
    return false;
  }

  private void analyze()
  {
    synchronized (this)
    {
      if ( _Analyses != null ) return; // another thread did it
      int r = getRoundTo(); // force rounding calculation since it may set _Analyses to null
      _Analyses = new HashMap<String,LearnedSignalTimingAnalysis>();
      if ( r == 0 ) return; // no way we can analyze with a 0 rounding
      analyzeImpl();
    }
  }

  public boolean hasAnalyses()
  {
    return ( getAnalyses().size() > 0 );
  }
  public LearnedSignalTimingAnalysis getPreferredAnalysis()
  {
    return getAnalysis( getPreferredAnalysisName() );
  }
  public LearnedSignalTimingAnalysis getAnalysis( String name )
  {
    return getAnalyses().get( name );
  }
  public String[] getAnalysisNames()
  {
    String[] names = new String[getAnalyses().size()];
    return getAnalyses().keySet().toArray( names );
  }
  public HashMap<String,LearnedSignalTimingAnalysis> getAnalyses()
  {
    synchronized (this)
    {
      if ( _Analyses == null )
        analyze();
      return _Analyses;
    }
  }
  protected void addAnalysis( LearnedSignalTimingAnalysis analysis )
  {
    if ( _Analyses == null ) return; // how did this even happen?  should only be called from within analyze()
    _Analyses.put( analysis.getName(), analysis );
  }

  protected static int[] arrayListToArray( ArrayList<Integer> data )
  {
    int r = 0;
    int[] result = new int[data.size()];
    for ( int d: data )
      result[r++] = d;
    return result;
  }

  public static int[][] splitDurationsBeforeLeadIn( int[] durations )
  {
    int[][] seps = new int[1][];
    seps[0] = new int[2];
    seps[0][0] = durations[0];
    seps[0][1] = durations[1];
    return splitDurations( durations, seps, false );
  }
  public static int[][] splitDurations( int[] durations, int[][] separators, boolean splitAfter )
  {
    ArrayList<int[]> results = new ArrayList<int[]>();
    ArrayList<Integer> list = new ArrayList<Integer>();

    int i = 0;
    while ( i < durations.length )
    {
      // look ahead for our separators
      int[] separator = null;
      for ( int[] tempSeparator: separators )
      {
        separator = tempSeparator;
        boolean found = false;
        if ( separator != null && separator.length > 0 && durations[i] == separator[0] )
        {
          found = true;
          for ( int s = 1; s < separator.length; s++ )
            found = ( found && i+s < durations.length && durations[i+s] == separator[s] );
        }
        if ( found )
          break;
        separator = null;
      }

      // if no separator, just add to list and move on
      if ( separator == null || ( i == 0 && !splitAfter ) )
      {
        list.add( durations[i++] );
      }
      else
      {
        if ( splitAfter )
        {
          // split after the separator, so fill out list with separator
          for ( int s = 0; s < separator.length; s++ )
            list.add( durations[i++] );
          // add completed split component to results
          results.add( arrayListToArray( list ) );
          // clear for next
          list.clear();
        }
        else
        {
          // split before the separator, so list is a complete split component
          results.add( arrayListToArray( list ) );
          // clear for next
          list.clear();
          // we know we can add all the separator pieces, so do that now to skip past them
          for ( int s = 0; s < separator.length; s++ )
            list.add( durations[i++] );
        }
      }
    }

    // add final list
    if ( !list.isEmpty() )
      results.add( arrayListToArray( list ) );

    i = 0;
    int[][] data = new int[results.size()][];
    for ( int[] r: results )
      data[i++] = r;

    return data;
  }
}
