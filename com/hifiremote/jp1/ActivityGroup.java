package com.hifiremote.jp1;

import java.util.Properties;

public class ActivityGroup extends Highlight
{
  public static void store( PropertyWriter pw, ActivityGroup[] groups )
  {
    if ( groups == null )
    {
      return;
    }
    if ( groups[ 0 ].getSegmentFlags() != 0 )
    {
      pw.print( "GroupSegmentFlags", groups[ 0 ].getSegmentFlags() );
    }
    Hex hex = new Hex( groups.length );
    for ( ActivityGroup group : groups )
    {
      hex.set( ( short )group.getDeviceIndex(), group.getIndex() );
      String notes = group.getNotes();
      if ( notes != null && !notes.trim().isEmpty() )
      {
        pw.print( "GroupNotes" + group.getIndex(), notes );
      }
    }
    pw.print( "GroupSettings", hex.toString() );
  }
  
  public static void parse( Properties props, Activity activity )
  {
    int groupSegmentFlags = 0;
    String temp = props.getProperty( "GroupSegmentFlags" );
    if ( temp != null )
    {
      groupSegmentFlags = Integer.parseInt( temp );
    }
    
    temp = props.getProperty( "GroupSettings" );
    if ( temp != null )
    {
      Hex hex = new Hex( temp );
      ActivityGroup[] activityGroups = new ActivityGroup[ hex.length() ];
      for ( int index = 0; index < hex.length(); index++ )
      {
        activityGroups[ index ] = new ActivityGroup( index, hex.getData()[ index ] );
        activityGroups[ index ].setNotes( props.getProperty( "GroupNotes" + index ) );
        activityGroups[ index ].setSegmentFlags( groupSegmentFlags );
      }
      activity.setActivityGroups( activityGroups );
    }
  }
  
  public ActivityGroup( int index, int deviceIndex )
  {
    this.index = index;
    this.deviceIndex = deviceIndex;
  }
  
  public ActivityGroup( int index, Remote remote )
  {
    this.index = index;
    device = remote.usesEZRC() ? remote.getDeviceButtons()[ 0 ] : DeviceButton.noButton;
    deviceIndex = device.getButtonIndex() & 0xFF;
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
  
  public Button[] getButtonGroup()
  {
    return buttonGroup;
  }

//  public void setButtonGroup( Button[] buttonGroup )
//  {
//    this.buttonGroup = buttonGroup;
//  }

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

  public Segment getSoftNamesSegment()
  {
    return softNamesSegment;
  }

  public void setSoftNamesSegment( Segment softNamesSegment )
  {
    this.softNamesSegment = softNamesSegment;
  }

  private int index = 0;
  private Button[] buttonGroup = null;
  private DeviceButton device = DeviceButton.noButton;
  private String notes = null;
  private int deviceIndex = 0xFF;
  private Segment softNamesSegment = null;
}
