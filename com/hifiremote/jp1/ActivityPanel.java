package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ActivityPanel extends RMPanel implements ChangeListener, ActionListener
{
  public ActivityPanel()
  {
    tabPanel = new JPanel( new BorderLayout() );
    JPanel panel = new JPanel( new BorderLayout() );
    panel.setBorder( BorderFactory.createTitledBorder( "Activity Functions" ) );
    activityFunctionTable = new JP1Table( activityFunctionModel );
    activityFunctionTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    JScrollPane scrollPane = new JScrollPane( activityFunctionTable );
    JPanel upper = new JPanel( new BorderLayout() );
    upper.add( scrollPane, BorderLayout.CENTER );
    String message = "Note:  When the activity has been set with the remote, \"Key\" is "
      + "the number key pressed to select the desired combination for the activity.  If "
      + "\"Key\" is blank, the activity has not been set.  The \"Key\" value has no "
      + "significance when the activity is set with RMIR, but some value has to be set "
      + "for it before a Power Macro can be entered.";
    JTextArea area = new JTextArea( message );
    JLabel label = new JLabel();
    area.setFont( label.getFont() );
    area.setBackground( label.getBackground() );
    area.setLineWrap( true );
    area.setWrapStyleWord( true );
    area.setEditable( false );
    area.setBorder( BorderFactory.createEmptyBorder( 5, 5, 10, 5 ) );
    upper.add( area, BorderLayout.PAGE_END );
    panel.add( upper, BorderLayout.CENTER );
    tabPanel.add( panel, BorderLayout.PAGE_START );
    panel = new JPanel( new BorderLayout() );
    panel.setBorder( BorderFactory.createTitledBorder( "Activity Group Assignments" ) );
    activityGroupTable = new JP1Table( activityGroupModel );
    activityGroupTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    scrollPane = new JScrollPane( activityGroupTable );
    panel.add( scrollPane, BorderLayout.CENTER );
    tabPanel.add( panel, BorderLayout.CENTER );
    panel = new JPanel();
    clearActivity = new JButton( "Clear Activity" );
    clearActivity.addActionListener( this );
    panel.add( clearActivity );
    tabPanel.add( panel, BorderLayout.PAGE_END );
    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener( this );    
    add( tabbedPane, BorderLayout.CENTER );
    Dimension d = activityFunctionTable.getPreferredSize();
    d.height = 2 * activityFunctionTable.getRowHeight();
    activityFunctionTable.setPreferredScrollableViewportSize( d );
    activityFunctionTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = activityFunctionTable;
        setHighlightAction( activityFunctionTable );
      }
    } );
    activityGroupTable.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( FocusEvent e )
      {
        activeTable = activityGroupTable;
        setHighlightAction( activityGroupTable );
      }
    } );
    activeTable = activityFunctionTable;
  }
  
  private void setHighlightAction( JP1Table table )
  {
    remoteConfig.getOwner().highlightAction.setEnabled( table.getSelectedRowCount() > 0 );
  }
  
  public JP1Table getActiveTable()
  {
    return activeTable;
  }
  
  @Override
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( listener != null )
    {
      if ( activityGroupModel != null )
      {
        activityGroupModel.addPropertyChangeListener( listener );
      }
      if ( activityFunctionModel != null )
      {
        activityFunctionModel.addPropertyChangeListener( listener );
      }
      propertyChangeSupport.addPropertyChangeListener( listener );
    }
  }
  
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );

  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = remoteConfig.getRemote();
    tabbedPane.removeAll();
    lastIndex = 0;
    for ( Button btn : remote.getButtonGroups().get( "Activity" ) )
    {
      tabbedPane.addTab( btn.getName(), null );
    }
  }

  private JP1Table activityGroupTable = null;
  private ActivityGroupTableModel activityGroupModel = new ActivityGroupTableModel();
  private JP1Table activityFunctionTable = null;
  private ActivityFunctionTableModel activityFunctionModel = new ActivityFunctionTableModel();
  private JPanel tabPanel = null;
  private JTabbedPane tabbedPane = null;
  private RemoteConfiguration remoteConfig = null;
  private int lastIndex = 0;
  private JP1Table activeTable = null;
  private JButton clearActivity = null;

  @Override
  public void stateChanged( ChangeEvent e )
  {
    if ( e.getSource() == tabbedPane )
    {
      int index = tabbedPane.getSelectedIndex();
      if ( index < 0 )
      {
        return;
      }

      tabbedPane.setComponentAt( lastIndex, null );

      if ( tabbedPane.getComponentAt( index ) == null )
      {
        tabbedPane.setComponentAt( index, tabPanel );
        lastIndex = index;
      }
      Button btn = remoteConfig.getRemote().getButtonGroups().get( "Activity" ).get( index );
      finishEditing();
      activityFunctionModel.set( btn, remoteConfig );
      activityFunctionTable.initColumns( activityFunctionModel );
      activityGroupModel.set( btn, remoteConfig );
      activityGroupTable.initColumns( activityGroupModel );
    }
  }
  
  public void finishEditing()
  {
    if ( activityFunctionTable.getCellEditor() != null )
    {
      activityFunctionTable.getCellEditor().stopCellEditing();
    }
    if ( activityGroupTable.getCellEditor() != null )
    {
      activityGroupTable.getCellEditor().stopCellEditing();
    }
  }

  @Override
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    Object source = e.getSource();
    if ( source == clearActivity )
    {
      Activity activity = activityFunctionModel.getRow( 0 );
      activity.setMacro( null );
      activity.setAudioHelp( 1 );
      activity.setVideoHelp( 1 );
      for ( ActivityGroup group : activity.getActivityGroups() )
      {
        group.setDevice( DeviceButton.noButton );
      }
    }
    activityFunctionModel.fireTableDataChanged();
    activityGroupModel.fireTableDataChanged();
    propertyChangeSupport.firePropertyChange( "data", null, null );
    
  }
  
}
