package com.hifiremote.jp1;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMovePanel.
 */
public class KeyMovePanel
  extends RMTablePanel< KeyMove >
{
  
  /**
   * Instantiates a new key move panel.
   */
  public KeyMovePanel()
  {
    super( new KeyMoveTableModel());
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    (( KeyMoveTableModel )model ).set( remoteConfig );
    JTableHeader th = table.getTableHeader();
    TableColumnModel tcm = th.getColumnModel();
    TableColumn tc = tcm.getColumn( 7 );
    if ( remoteConfig.getRemote().getEFCDigits() == 3 )
      tc.setHeaderValue( "<html>EFC or<br>Key Name</html>" );
    else
      tc.setHeaderValue( "<html>EFC-5 or<br>Key Name</html>" );
    th.repaint();
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  protected KeyMove createRowObject( KeyMove baseKeyMove )
  {
    return KeyMoveDialog.showDialog(( JFrame )SwingUtilities.getRoot( this ), baseKeyMove, (( KeyMoveTableModel )model ).getRemoteConfig());
  }
}
  
