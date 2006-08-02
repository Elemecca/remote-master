package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class KeyMovePanel
  extends RMTablePanel< KeyMove >
{
  public KeyMovePanel()
  {
    super( new KeyMoveTableModel());
  }

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
  
  protected KeyMove createRowObject( KeyMove baseKeyMove )
  {
    return KeyMoveDialog.showDialog(( JFrame )SwingUtilities.getRoot( this ), baseKeyMove, (( KeyMoveTableModel )model ).getRemoteConfig());
  }
}
  
