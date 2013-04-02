package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class TimedMacroDialog extends JDialog implements ActionListener, ButtonEnabler
{
  
  public static TimedMacro showDialog( Component locationComp, TimedMacro timedMacro, RemoteConfiguration config )
  {
    if ( dialog == null || config != dialog.config )
      dialog = new TimedMacroDialog( locationComp );

    dialog.setRemoteConfiguration( config );
    dialog.setTimedMacro( timedMacro );

    dialog.pack();
    dialog.setLocationRelativeTo( locationComp );
    dialog.setVisible( true );
    return dialog.timedMacro;
  }
  
  public static void reset()
  {
    if ( dialog != null )
    {
      dialog.dispose();
      dialog = null;
    }
  }
  
  private TimedMacroDialog( Component c )
  {
    super( ( JFrame )SwingUtilities.getRoot( c ) );
    setTitle( "Timed Macro" );
    setModal( true );
    
    JComponent contentPane = ( JComponent )getContentPane();
    contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    
    Box settingsBox = Box.createHorizontalBox();
    contentPane.add( settingsBox, BorderLayout.NORTH );
    
    // Add the day and time schedulers 
    dayScheduleBox = new DayScheduleBox();
    settingsBox.add( dayScheduleBox );
    timePanel = new RMTimePanel();
    settingsBox.add( timePanel );

    // Add the Macro definition controls
    macroBox = new MacroDefinitionBox();
    macroBox.setButtonEnabler( this );
    contentPane.add( macroBox, BorderLayout.CENTER );

    JPanel bottomPanel = new JPanel( new BorderLayout() );
    contentPane.add( bottomPanel, BorderLayout.SOUTH );
    
    // Add the notes
    JPanel panel = new JPanel( new BorderLayout() );
    bottomPanel.add( panel, BorderLayout.NORTH );
    panel.setBorder( BorderFactory.createTitledBorder( "Notes" ) );
    notes.setLineWrap( true );
    panel.add( new JScrollPane( notes, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ) );

    // Add the action buttons
    panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    bottomPanel.add( panel, BorderLayout.SOUTH );

    okButton.addActionListener( this );
    panel.add( okButton );

    cancelButton.addActionListener( this );
    panel.add( cancelButton );
  }
  
  private void setRemoteConfiguration( RemoteConfiguration config )
  {
    if ( this.config == config )
      return;

    this.config = config;    
    macroBox.setRemoteConfiguration( config );
  }
  
  private void setTimedMacro( TimedMacro timedMacro )
  {
    this.timedMacro = null;
       
    if ( timedMacro == null )
    {
      DaySchedule defaultDaySchedule = new DaySchedule();
      if ( config != null && config.getRemote().getMacroCodingType().hasTimedMacros() )
      {
        defaultDaySchedule.set( Calendar.MONDAY, true );
      }
      dayScheduleBox.setValue( defaultDaySchedule );
      timePanel.setValue( new RMTime() );
      macroBox.setValue( ( Hex )null );
      notes.setText( null );
    }
    else
    {  
      dayScheduleBox.setValue( timedMacro.getDaySchedule() );
      timePanel.setValue( timedMacro.getTime() );
      macroBox.setValue( timedMacro.getData() );
      notes.setText( timedMacro.getNotes() );
    }  

    macroBox.enableButtons();
  }
  
  @Override
  public void enableButtons( Button b, MacroDefinitionBox macroBox )
  {
    int limit = 15;
    if ( config.getRemote().getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG )
      limit = 255;
    boolean canAdd = ( b != null ) && macroBox.isMoreRoom( limit );

    macroBox.add.setEnabled( canAdd && b.canAssignToTimedMacro() );
    macroBox.insert.setEnabled( canAdd && b.canAssignToTimedMacro() );
    macroBox.addShift.setEnabled( canAdd && b.canAssignShiftedToTimedMacro() );
    macroBox.insertShift.setEnabled( canAdd && b.canAssignShiftedToTimedMacro() );
    boolean xShiftEnabled = config.getRemote().getXShiftEnabled();
    macroBox.addXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToTimedMacro() );
    macroBox.insertXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToTimedMacro() );
  }
  
  @Override
  public boolean isAvailable( Button b )
  {
    return  b.canAssignToTimedMacro() 
    || b.canAssignShiftedToTimedMacro() 
    || b.canAssignXShiftedToTimedMacro();
  }

  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    if ( source == okButton )
    {
      if ( macroBox.isEmpty() )
      {
        showWarning( "You haven't included any keys in your macro!" );
        return;
      }
      
      DaySchedule daySchedule = dayScheduleBox.getValue();
      RMTime time = timePanel.getValue();
      Hex data = macroBox.getValue();
      String notesStr = notes.getText();
      
      if ( time != null )
      {  
        timedMacro = new TimedMacro( daySchedule, time, data, notesStr );
        setVisible( false );
      }
      else
      {
        return;
      }
    }
    else if ( source == cancelButton )
    {
      timedMacro = null;
      setVisible( false );
    }
  }
  
  private void showWarning( String message )
  {
    JOptionPane.showMessageDialog( this, message, "Missing Information", JOptionPane.ERROR_MESSAGE );
  }
  
  private static TimedMacroDialog dialog = null;
  
  private TimedMacro timedMacro = null;
  
  private RemoteConfiguration config = null;
  
  private MacroDefinitionBox macroBox = null;
  
  private DayScheduleBox dayScheduleBox = null; 
  
  private JTextArea notes = new JTextArea( 2, 10 );
  
  /** The ok button. */
  private JButton okButton = new JButton( "OK" );

  /** The cancel button. */
  private JButton cancelButton = new JButton( "Cancel" );
  
  private RMTimePanel timePanel = null;
}
