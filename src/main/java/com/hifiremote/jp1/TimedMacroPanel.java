package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;

public class TimedMacroPanel extends RMTablePanel< TimedMacro >
{
  
  public TimedMacroPanel()
  {
    super( new TimedMacroTableModel() );
    footerPanel = new JPanel( new BorderLayout() );
    super.footerPanel.add( footerPanel, BorderLayout.PAGE_START );
    JPanel progressBarPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    progressBarPanel.add( new JLabel( "Timed Macro: " ) );
    timedMacroProgressBar = new JProgressBar();
    timedMacroProgressBar.setStringPainted( true );
    timedMacroProgressBar.setString( "N/A" );
    progressBarPanel.add( timedMacroProgressBar );
    footerPanel.add( progressBarPanel, BorderLayout.PAGE_START );
    timedMacroWarningPane = new JTextPane();
    footerPanel.add( timedMacroWarningPane, BorderLayout.PAGE_END );
    Font font = timedMacroWarningPane.getFont();
    Font font2 = font.deriveFont( Font.BOLD, 12 );
    timedMacroWarningPane.setFont( font2 );
    timedMacroWarningPane.setBackground( Color.RED );
    timedMacroWarningPane.setForeground( Color.YELLOW );
    String bugText = "NOTE:  After a one-time macro fires, all macros defined below it " +
      "will be deleted.  Position them accordingly.";
    timedMacroWarningPane.setText( bugText );
    timedMacroWarningPane.setEditable( false );
    timedMacroWarningPane.setVisible( false );
  }

  @Override
  protected TimedMacro createRowObject( TimedMacro baseTimedMacro )
  {
    return TimedMacroDialog.showDialog( this, baseTimedMacro, ( ( TimedMacroTableModel )model ).getRemoteConfig() );
  }

  @Override
  public void set( RemoteConfiguration config )
  {
    ( ( TimedMacroTableModel )model ).set( config );
    table.initColumns( model );
    if ( config != null )
    {  
      remote = config.getRemote();
      DayScheduleBox.macroCodingType = remote.getMacroCodingType();
      timedMacroWarningPane.setVisible( remote.hasTimedMacroWarning() );
    }
  }
  
  public Remote getRemote()
  {
    return remote;
  }

  private JPanel footerPanel = null;
  private JTextPane timedMacroWarningPane = null;
  protected JProgressBar timedMacroProgressBar = null;
  private Remote remote = null;

}
