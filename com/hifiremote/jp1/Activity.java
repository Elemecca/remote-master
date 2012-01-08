package com.hifiremote.jp1;

import java.awt.Color;

public class Activity extends Highlight
{
  public Activity( Button button, Remote remote )
  {
    this.button = button;
    activityGroups = new ActivityGroup[ remote.getActivityButtonGroups().length ];
    for ( int i = 0; i < activityGroups.length; i++ )
    {
      activityGroups[ i ] = new ActivityGroup( i, remote );
    }
  }
  
  public ActivityGroup[] getActivityGroups()
  {
    return activityGroups;
  }

  public Button getButton()
  {
    return button;
  }

  public Macro getMacro()
  {
    return macro;
  }

  public void setMacro( Macro macro )
  {
    this.macro = macro;
  }

  public int getAudioHelp()
  {
    return audioHelp;
  }

  public void setAudioHelp( int audioHelp )
  {
    this.audioHelp = audioHelp;
  }

  public int getVideoHelp()
  {
    return videoHelp;
  }

  public void setVideoHelp( int videoHelp )
  {
    this.videoHelp = videoHelp;
  }
  
  @Override
  public void setHighlight( Color color )
  {
    super.setHighlight( color );
    if ( macro != null )
    {
      macro.setHighlight( color );
    }
  }

  public Segment getHelpSegment()
  {
    return helpSegment;
  }

  public void setHelpSegment( Segment helpSegment )
  {
    this.helpSegment = helpSegment;
  }

  public int getHelpSegmentFlags()
  {
    return helpSegmentFlags;
  }

  public void setHelpSegmentFlags( int helpSegmentFlags )
  {
    this.helpSegmentFlags = helpSegmentFlags;
  }

  Segment helpSegment = null;
  ActivityGroup[] activityGroups = null;
  Button button = null;
  Macro macro = null;
  int audioHelp = 1;
  int videoHelp = 1;
  int helpSegmentFlags = 0;
}
