package com.hifiremote.jp1;

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



  ActivityGroup[] activityGroups = null;
  Button button = null;
}
