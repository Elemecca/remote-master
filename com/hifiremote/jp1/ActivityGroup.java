package com.hifiremote.jp1;

public class ActivityGroup extends Highlight
{
  public ActivityGroup( int index, Remote remote )
  {
    this.index = index;
    buttonGroup = remote.getActivityButtonGroups()[ index ];
  }
  
  public String getButtons()
  {
    String str = "";
    for ( int i = 0; i < buttonGroup.length; i++ )
    {
      if ( i > 0 )
      {
        str += ", ";
      }
      str += buttonGroup[ i ].getName();
    }
    return str;
  }
  
  
  
  public DeviceButton getDevice()
  {
    return device;
  }

  public void setDevice( DeviceButton device )
  {
    this.device = device;
  }

  public String getNotes()
  {
    return notes;
  }

  public void setNotes( String notes )
  {
    this.notes = notes;
  }

  private int index = 0;
  private Button[] buttonGroup = null;
  private DeviceButton device = null;
  private String notes = null;
}
