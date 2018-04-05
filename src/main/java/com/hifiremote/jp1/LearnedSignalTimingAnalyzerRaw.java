package com.hifiremote.jp1;

public class LearnedSignalTimingAnalyzerRaw extends LearnedSignalTimingAnalyzerBase
{
  public LearnedSignalTimingAnalyzerRaw( UnpackLearned u )
  {
    super( u );
  }

  @Override
  public String getName()
  {
    return "Raw Data";
  }

  @Override
  protected int calcAutoRoundTo()
  {
    return 1;
  }

  @Override
  public int checkCandidacyImpl( int roundTo )
  {
    // 2 means rounding is for sure acceptable
    return 2;
  }

  @Override
  protected void analyzeImpl()
  {
    System.err.println( "RawAnalyzer: (" + this.hashCode() +") Analyze" );
    addAnalysis(
        new LearnedSignalTimingAnalysis(
            "Even",
            getUnpacked().getBursts(),
            new int[][] { getUnpacked().getOneTimeDurations( getRoundTo(), true ) },
            new int[][] { getUnpacked().getRepeatDurations( getRoundTo(), true ) },
            new int[][] { getUnpacked().getExtraDurations( getRoundTo(), true ) },
            ";",
            2,
            2,
            ""
        )
    );
    addAnalysis(
        new LearnedSignalTimingAnalysis(
            "Odd",
            getUnpacked().getBursts(),
            new int[][] { getUnpacked().getOneTimeDurations( getRoundTo(), true ) },
            new int[][] { getUnpacked().getRepeatDurations( getRoundTo(), true ) },
            new int[][] { getUnpacked().getExtraDurations( getRoundTo(), true ) },
            ";",
            1,
            2,
            ""
        )
    );
  }

  @Override
  protected String getPreferredAnalysisName()
  {
    return "Even";
  }
}
