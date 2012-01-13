package com.hifiremote.jp1;

public class ActivityGroup extends Highlight
{
  public ActivityGroup( int index, int deviceIndex )
  {
    this.index = index;
    this.deviceIndex = deviceIndex;
  }
  
  public ActivityGroup( int index, Remote remote )
  {
    this.index = index;
    buttonGroup = remote.getActivityButtonGroups()[ index ];
  }
  
  public void set( Remote remote )
  {
    device = deviceIndex == 0xFF ? DeviceButton.noButton : remote.getDeviceButton( deviceIndex );
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
  
  public void setButtonGroup( Button[] buttonGroup )
  {
    this.buttonGroup = buttonGroup;
  }

  public int getIndex()
  {
    return index;
  }

  public void setIndex( int index )
  {
    this.index = index;
  }

  public DeviceButton getDevice()
  {
    return device;
  }

  public int getDeviceIndex()
  {
    return deviceIndex;
  }

  public void setDevice( DeviceButton device )
  {
    this.device = device;
    deviceIndex = device == null ? 0xFF : device.getButtonIndex() & 0xFF;
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
  private DeviceButton device = DeviceButton.noButton;
  private String notes = null;
  private int deviceIndex = 0xFF;
  
}
