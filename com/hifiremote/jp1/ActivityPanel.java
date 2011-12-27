package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ActivityPanel extends RMPanel implements ChangeListener
{
  public ActivityPanel()
  {
    activityGroupPanel = new JPanel( new BorderLayout() );
    activityGroupPanel.setBorder( BorderFactory.createTitledBorder( "Activity Group Assignments" ) );
    activityGroupTable = new JP1Table( activityGroupModel );
    activityGroupTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    activityGroupTable.initColumns( activityGroupModel );
    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener( this );
    JScrollPane activityScrollPane = new JScrollPane( activityGroupTable );
    activityGroupPanel.add( activityScrollPane, BorderLayout.CENTER );
    add( tabbedPane, BorderLayout.CENTER );
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

  private JPanel activityGroupPanel = null;
  private JP1Table activityGroupTable = null;
  private ActivityGroupTableModel activityGroupModel = new ActivityGroupTableModel();
  private JTabbedPane tabbedPane = null;
  private RemoteConfiguration remoteConfig = null;
  private int lastIndex = 0;

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
        tabbedPane.setComponentAt( index, activityGroupPanel );
        lastIndex = index;
      }
      Button btn = remoteConfig.getRemote().getButtonGroups().get( "Activity" ).get( index );
      activityGroupModel.set( btn, remoteConfig );
      activityGroupTable.initColumns( activityGroupModel );
    }
  }
  
}
