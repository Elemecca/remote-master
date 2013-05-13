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
    Hex indices = new Hex( groups.length );
    boolean printIndices = false;
    for ( int i = 0; i < groups.length; i++ )
//    for ( ActivityGroup group : groups )
    {
      ActivityGroup group = groups[ i ];
//      hex.set( ( short )group.getDeviceIndex(), group.getIndex() );
      hex.set( ( short )group.getDeviceIndex(), i );
      indices.set( ( short )group.getIndex(), i );
      if ( group.getIndex() != i )
      {
        printIndices = true;
      }
      String notes = group.getNotes();
      if ( notes != null && !notes.trim().isEmpty() )
      {
        pw.print( "GroupNotes" + i, notes );
      }
    }
    if ( printIndices )
    {
      pw.print( "GroupIndices", indices.toString() );
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
      Hex indices = null;
      temp = props.getProperty( "GroupIndices" );
      if ( temp != null )
      {
        indices = new Hex( temp );
      }
      else
      {
        indices = new Hex( hex.length() );
        for ( int i = 0; i < indices.length(); i++ )
        {
          indices.set( ( short )i, i );
        }
      }
      ActivityGroup[] activityGroups = new ActivityGroup[ hex.length() ];
      for ( int i = 0; i < hex.length(); i++ )
      {
        activityGroups[ i ] = new ActivityGroup( indices.getData()[ i ], hex.getData()[ i ] );
        activityGroups[ i ].setNotes( props.getProperty( "GroupNotes" + i ) );
        activityGroups[ i ].setSegmentFlags( groupSegmentFlags );
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
  private int deviceIndex = 0xFF;
  private Segment softNamesSegment = null;
}
