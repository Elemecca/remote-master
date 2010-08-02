package com.hifiremote.jp1;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

public class DayScheduleBox extends Box implements ItemListener, RMSetter<DaySchedule>
{

  public DayScheduleBox()
  {
    super( BoxLayout.X_AXIS );
    JPanel daySetting = new JPanel();
    daySetting.setBorder( BorderFactory.createTitledBorder( "Day Setting" ) );
    add( daySetting );
    
    JPanel repeatSetting = new JPanel( new GridLayout( 3, 1, 0, 0 ) );
    repeatSetting.setBorder( BorderFactory.createTitledBorder( "Repeat" ) );
    
    ButtonGroup grpRpt = new ButtonGroup(); 
    
    if ( macroCodingType.hasTimedMacros() )
    {
      mon = new JRadioButton( "Mon" );
      tue = new JRadioButton( "Tue" );
      wed = new JRadioButton( "Wed" );
      thu = new JRadioButton( "Thu" );
      fri = new JRadioButton( "Fri" );
      sat = new JRadioButton( "Sat" );
      sun = new JRadioButton( "Sun" );
      
      ButtonGroup grpDay = new ButtonGroup();
      grpDay.add( mon );
      grpDay.add( tue );
      grpDay.add( wed );
      grpDay.add( thu );
      grpDay.add( fri );
      grpDay.add( sat );
      grpDay.add( sun );

      daySetting.setLayout( new GridLayout( 4, 2, 0, -5 ) );
      daySetting.add( mon );
      daySetting.add( fri );
      daySetting.add( tue );
      daySetting.add( sat );
      daySetting.add( wed );
      daySetting.add( sun );
      daySetting.add( thu );
      
      one = new JRadioButton( "Once" );
      rpt = new JRadioButton( "Weekly" );
      dly = new JRadioButton( "Daily" );
      dly.addItemListener( this );
      
      grpRpt.add( one );
      grpRpt.add( rpt );
      grpRpt.add( dly );
      
      repeatSetting.add( one );
      repeatSetting.add( rpt );
      repeatSetting.add( dly );
      
      add( repeatSetting );
    }
    else
    {
      mon = new JCheckBox( "Mon" );
      tue = new JCheckBox( "Tue" );
      wed = new JCheckBox( "Wed" );
      thu = new JCheckBox( "Thu" );
      fri = new JCheckBox( "Fri" );
      sat = new JCheckBox( "Sat" );
      sun = new JCheckBox( "Sun" );
      daySetting.setLayout( new GridLayout( 3, 3, 0, -5 ) );
      one = new JRadioButton( "One-time" );
      rpt = new JRadioButton( "Repeating" );
      dly = new JCheckBox();  // Not visible
      
      grpRpt.add( one );
      grpRpt.add( rpt );
      
      daySetting.add( mon );
      daySetting.add( thu );
      daySetting.add( sun );
      daySetting.add( tue );
      daySetting.add( fri );
      daySetting.add( one );
      daySetting.add( wed );
      daySetting.add( sat );
      daySetting.add( rpt );
    }
  }
  
  @Override
  public void setValue( DaySchedule daySchedule )
  {
    if ( macroCodingType.hasTimedMacros() && daySchedule.isSet7Days() 
        && daySchedule.isWeeklyRepeat())
    {
      dly.setSelected( true );
    }
    else
    {
      mon.setSelected( daySchedule.isSet( Calendar.MONDAY ) );
      tue.setSelected( daySchedule.isSet( Calendar.TUESDAY) );
      wed.setSelected( daySchedule.isSet( Calendar.WEDNESDAY ) );
      thu.setSelected( daySchedule.isSet( Calendar.THURSDAY) );
      fri.setSelected( daySchedule.isSet( Calendar.FRIDAY ) );
      sat.setSelected( daySchedule.isSet( Calendar.SATURDAY ) );
      sun.setSelected( daySchedule.isSet( Calendar.SUNDAY ) );
      one.setSelected( ! daySchedule.isWeeklyRepeat() );
      rpt.setSelected( daySchedule.isWeeklyRepeat() );
    }
  }

  @Override
  public DaySchedule getValue()
  {
    DaySchedule daySchedule = new DaySchedule();
    if ( dly.isSelected() )
    {
      daySchedule.set7Days();
      daySchedule.setWeeklyRepeat( true );
    }
    else
    {
      daySchedule.set( Calendar.MONDAY, mon.isSelected() );
      daySchedule.set( Calendar.TUESDAY, tue.isSelected() );
      daySchedule.set( Calendar.WEDNESDAY, wed.isSelected() );
      daySchedule.set( Calendar.THURSDAY, thu.isSelected() );
      daySchedule.set( Calendar.FRIDAY, fri.isSelected() );
      daySchedule.set( Calendar.SATURDAY, sat.isSelected() );
      daySchedule.set( Calendar.SUNDAY, sun.isSelected() );
      daySchedule.setWeeklyRepeat( rpt.isSelected() );
    }
    return daySchedule;
  }

  @Override
  public void itemStateChanged( ItemEvent e )
  {
    boolean b = ! dly.isSelected();
    mon.setEnabled( b );
    tue.setEnabled( b );
    wed.setEnabled( b );
    thu.setEnabled( b );
    fri.setEnabled( b );
    sat.setEnabled( b );
    sun.setEnabled( b );
  }
  
  private JToggleButton mon = null;  
  private JToggleButton tue = null;
  private JToggleButton wed = null;
  private JToggleButton thu = null;
  private JToggleButton fri = null;
  private JToggleButton sat = null;
  private JToggleButton sun = null;
  private JToggleButton dly = null;
  private JToggleButton one = null;
  private JToggleButton rpt = null;
  
  public static MacroCodingType macroCodingType = null;
}
