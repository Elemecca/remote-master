package com.hifiremote.jp1;

import java.util.Iterator;

public class LearnedSignalTimingAnalyzer
{
  private LearnedSignalTimingAnalyzerBase[] _Analyzers;
  private String[] _AnalyzerNames;
  private UnpackLearned _Data;

  public LearnedSignalTimingAnalyzer( UnpackLearned u )
  {
    _Data = u;
    _Analyzers = new LearnedSignalTimingAnalyzerBase[]
        {
          new LearnedSignalTimingAnalyzerBiPhase( u ),
          new LearnedSignalTimingAnalyzerRaw( u )
        };
    _AnalyzerNames = new String[_Analyzers.length];
    for ( int n = 0; n < _Analyzers.length; n++ )
      _AnalyzerNames[n] = _Analyzers[n].getName();
  }

  public boolean getIsValid()
  {
    return _Data.ok;
  }

  public String[] getAnalyzerNames()
  {
    return _AnalyzerNames;
  }

  public LearnedSignalTimingAnalyzerBase getAnalyzer( String name )
  {
    for ( int i = 0; i < _AnalyzerNames.length; i++ )
      if ( _AnalyzerNames[i].equals( name ) )
        return getAnalyzer( i );
    // no match
    return null;
  }

  public LearnedSignalTimingAnalyzerBase getAnalyzer( int i )
  {
    return _Analyzers[i];
  }

  public LearnedSignalTimingAnalyzerBase getPreferredAnalyzer()
  {
    for ( LearnedSignalTimingAnalyzerBase a: _Analyzers )
      if ( a.hasAnalyses() )
        return a;
    // can't actually happen since raw analyzer will always have one
    return null;
  }

  public int getNumAnalyzers()
  {
    return _Analyzers.length;
  }

  public Iterator<LearnedSignalTimingAnalyzerBase> getAnalyzers()
  {
    return new Iterator<LearnedSignalTimingAnalyzerBase>() 
    {
      private int i = 0;
      @Override
      public boolean hasNext() { return (i+1 < _Analyzers.length); }
      @Override
      public LearnedSignalTimingAnalyzerBase next() { return _Analyzers[++i]; }
      @Override
      public void remove() { }
    };
  }

  // all of this is purely for UI persistence
  private int _SelectedAnalyzer = -1;
  private String _SelectedAnalysisName = null;
  public void setSelectedAnalyzer( String name )
  {
    for ( int i = 0; i < _AnalyzerNames.length; i++ )
      if ( _AnalyzerNames[i].equals( name ) )
      {
        _SelectedAnalyzer = i;
        setSelectedAnalysisName( getSelectedAnalyzer().getPreferredAnalysis().getName() );
        return;
      }
  }
  public void setSelectedAnalysisName( String name )
  {
    for ( String n: getSelectedAnalyzer().getAnalysisNames() )
      if ( n.equals( name ) )
      {
        _SelectedAnalysisName = name;
        return;
      }
  }
  public LearnedSignalTimingAnalyzerBase getSelectedAnalyzer()
  {
    initSelectedToPreferred();
    return getAnalyzer( _SelectedAnalyzer );
  }
  public String getSelectedAnalysisName()
  {
    initSelectedToPreferred();
    return _SelectedAnalysisName;
  }
  public LearnedSignalTimingAnalysis getSelectedAnalysis()
  {
    initSelectedToPreferred();
    return getSelectedAnalyzer().getAnalysis( getSelectedAnalysisName() );
  }
  private void initSelectedToPreferred()
  {
    if ( _SelectedAnalyzer > -1 )
      return;
    setSelectedAnalyzer( getPreferredAnalyzer().getName() );
    setSelectedAnalysisName( getSelectedAnalyzer().getPreferredAnalysis().getName() );
  }

  private int _SavedAnalyzer = -1;
  private String _SavedAnalysisName = null;
  public void saveState()
  {
    _SavedAnalyzer = _SelectedAnalyzer;
    _SavedAnalysisName = _SelectedAnalysisName;
    for ( LearnedSignalTimingAnalyzerBase a: _Analyzers )
      a.saveState();
  }
  public void restoreState()
  {
    _SelectedAnalyzer = _SavedAnalyzer;
    _SelectedAnalysisName = _SavedAnalysisName;
    for ( LearnedSignalTimingAnalyzerBase a: _Analyzers )
      a.restoreState();
  }
}
