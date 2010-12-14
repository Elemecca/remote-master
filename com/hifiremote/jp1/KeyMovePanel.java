package com.hifiremote.jp1;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
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
      int firstRow = keymoves.size();
      int lastRow = firstRow;
      for ( int row : rows )
      {
        if ( row > lastRow )
        {
          lastRow = row;
        }
        KeyMove keyMove = model.getRow( sorter.modelIndex( row ) );
        toDetach.add( keyMove );
      }

      for ( KeyMove keyMove : toDetach )
      {
        DeviceUpgrade upgrade = remoteConfig.findDeviceUpgrade( keyMove.getDeviceType(), keyMove.getSetupCode() );
        keymoves.add( keyMove );
        upgrade.setFunction( keyMove.getKeyCode(), null );
      }
      ( ( KeyMoveTableModel )model ).refresh();
      model.fireTableDataChanged();
    }
  }

  private KeyMovePanel thisPanel = null;

  private RemoteConfiguration remoteConfig = null;

  protected Action detach = null;
}
