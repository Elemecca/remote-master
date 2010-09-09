package com.hifiremote.jp1;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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
    ( ( KeyMoveTableModel )this.getModel() ).sorter = sorter;

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
        thisPanel.valueChanged( e );
        int row = table.getSelectedRow();
        if ( row != -1 )
        {
          row = sorter.modelIndex( row );
        }
        int limit = remoteConfig.getKeyMoves().size();
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
      }
    } );
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
    JTableHeader th = table.getTableHeader();
    TableColumnModel tcm = th.getColumnModel();
    TableColumn tc = tcm.getColumn( 7 );
    if ( remoteConfig != null && remoteConfig.getRemote().getEFCDigits() == 3 )
    {
      tc.setHeaderValue( "<html>EFC or<br>Key Name</html>" );
    }
    else
    {
      tc.setHeaderValue( "<html>EFC-5 or<br>Key Name</html>" );
    }
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

  private KeyMovePanel thisPanel = null;

  private RemoteConfiguration remoteConfig = null;
}
