package com.hifiremote.jp1;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMovePanel.
 */
public class KeyMovePanel extends RMTablePanel< KeyMove >
{
  /**
   * Instantiates a new key move panel.
   */
  public KeyMovePanel()
  {
    super( new KeyMoveTableModel() );
    table.removeMouseListener( openEditor );
    table.getSelectionModel().removeListSelectionListener( this );
    thisPanel = this;

    table.addMouseListener( new MouseAdapter()
    {
      @Override
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
        {
          return;
        }
        int row = table.getSelectedRow();
        if ( row != -1 )
        {
          row = sorter.modelIndex( row );
        }
        if ( row == -1 || row >= remoteConfig.getKeyMoves().size() )
        {
          return;
        }
        if ( !table.isCellEditable( row, table.columnAtPoint( e.getPoint() ) ) )
        {
          editRowObject( row );
        }
      }
    } );

    table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
    {
      @Override
      public void valueChanged( ListSelectionEvent e )
      {
        if ( e.getValueIsAdjusting() )
        {
          return;
        }
        thisPanel.valueChanged( e );

        int[] rows = table.getSelectedRows();
        boolean enableDetach = rows.length > 0;
        int limit = remoteConfig.getKeyMoves().size();
        for ( int tableRow : rows )
        {
          int row = sorter.modelIndex( tableRow );

          if ( row >= limit )
          {
            editButton.setEnabled( false );
            editItem.setEnabled( false );
            cloneButton.setEnabled( false );
            cloneItem.setEnabled( false );
            deleteButton.setEnabled( false );
            deleteItem.setEnabled( false );
            upButton.setEnabled( false );
            downButton.setEnabled( false );
          }
          else if ( row == limit - 1 )
          {
            downButton.setEnabled( false );
          }
          if ( row < limit )
          {
            enableDetach = false;
          }
        }
        detach.setEnabled( enableDetach );
      }
    } );

    detach = new DetachAction();
    buttonPanel.add( new JButton( detach ) );
    detach.setEnabled( false );
    popup.add( detach );
  }

  @Override
  protected boolean showPopup( MouseEvent e )
  {
    if ( e.isPopupTrigger() )
    {
      finishEditing();
      popupRow = table.rowAtPoint( e.getPoint() );
      // int popupCol = table.columnAtPoint( e.getPoint() );
      int limit = remoteConfig.getKeyMoves().size();
      if ( table.isRowSelected( popupRow ) )
      {
        boolean canDetach = true;
        for ( int row : table.getSelectedRows() )
        {

          if ( sorter.modelIndex( row ) < limit )
          {
            canDetach = false;
          }
        }
        detach.setEnabled( canDetach );
      }
      else
      {
        int row = sorter.modelIndex( popupRow );
        detach.setEnabled( row >= limit );
      }
      popup.show( table, e.getX(), e.getY() );
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( KeyMoveTableModel )model ).set( remoteConfig );
    this.remoteConfig = remoteConfig;
    table.initColumns( model );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  @Override
  protected KeyMove createRowObject( KeyMove baseKeyMove )
  {
    return KeyMoveDialog.showDialog( ( JFrame )SwingUtilities.getRoot( this ), baseKeyMove,
        ( ( KeyMoveTableModel )model ).getRemoteConfig() );
  }

  @Override
  protected void newRowObject( KeyMove baseObject, int row, int modelRow, boolean select )
  {
    KeyMove km = createRowObject( baseObject );
    if ( km == null )
    {
      return;
    }
    int upgradeKeyMoveCount = ( ( KeyMoveTableModel )model ).getUpgradeKeyMoveCount();
    int effectiveEnd = model.getRowCount() - upgradeKeyMoveCount; // start of non-editable rows
    if ( row == -1 || modelRow >= effectiveEnd )
    {
      if ( upgradeKeyMoveCount == 0 )
      {
        model.addRow( km );
        row = model.getRowCount();
      }
      else
      {
        model.insertRow( effectiveEnd, km );
        row += 1;
      }
    }
    else
    {
      model.insertRow( modelRow, km );
    }

    if ( select )
    {
      table.setRowSelectionInterval( row, row );
    }
  }

  protected class DetachAction extends AbstractAction
  {
    public DetachAction()
    {
      super( "Detach" );
      putValue( SHORT_DESCRIPTION, "Detach from upgrade" );
    }

    public void actionPerformed( ActionEvent event )
    {
      AbstractButton source = ( AbstractButton )event.getSource();
      int[] rows =
      {
        popupRow
      };
      if ( source instanceof JButton )
      {
        rows = table.getSelectedRows();
      }
      else if ( source instanceof JMenuItem )
      {
        if ( table.isRowSelected( popupRow ) )
        {
          rows = table.getSelectedRows();
        }
      }

      List< KeyMove > keymoves = remoteConfig.getKeyMoves();
      List< KeyMove > toDetach = new ArrayList< KeyMove >( rows.length );
      List< KeyMove > alsoDetach = new ArrayList< KeyMove >();
      for ( int row : rows )
      {
        KeyMove keyMove = model.getRow( sorter.modelIndex( row ) );
        toDetach.add( keyMove );
        
        // Now see if there are other keymoves arising from the same device upgrade keymove as this one.
        DeviceUpgrade upgrade = remoteConfig.findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode() );
        for ( int i : remoteConfig.getDeviceButtonIndexList( upgrade ) )
        {
          if ( i == keyMove.getDeviceButtonIndex() )
          {
            // We have already added this one to the detach list.
            continue;
          }
          for ( int test = keymoves.size(); test < model.getData().size(); test++ )
          {
            KeyMove km = model.getData().get( test );
            if ( km.getDeviceButtonIndex() == i && km.getKeyCode() == keyMove.getKeyCode() )
            {
              alsoDetach.add( km );
              break;
            }
          }
        }
      }
      
      for ( Iterator< KeyMove > it = alsoDetach.iterator(); it.hasNext(); )
      {
        // Avoid duplicates by deleting any alsoDetach keymoves that are actually also selected.
        KeyMove km = it.next();
        if ( toDetach.contains( km ) )
        {
          it.remove();
        }
      }
      
      if ( !alsoDetach.isEmpty() && JOptionPane.showConfirmDialog( null,
          "At least one of the device upgrades of the attached key moves selected for\n"
          + "detachment is assigned to more than one device button.  The corresponding\n"
          + "key moves of the other device buttons will also be detached.  Are you sure\n"
          + "that you want to proceed?",
          "Detach Key Moves from Device Upgrades",
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.NO_OPTION )
      {
        return;
      }

      for ( KeyMove keyMove : toDetach )
      {
        DeviceUpgrade upgrade = remoteConfig.findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode() );
        keymoves.add( keyMove );
        upgrade.setFunction( keyMove.getKeyCode(), null );
      }
      keymoves.addAll( alsoDetach );

      ( ( KeyMoveTableModel )model ).refresh();
      model.fireTableDataChanged();
    }
  }

  private KeyMovePanel thisPanel = null;

  private RemoteConfiguration remoteConfig = null;

  protected Action detach = null;
}
