package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class SpecialFunctionPanel
  extends RMTablePanel< SpecialProtocolFunction >
{
  public SpecialFunctionPanel()
  {
    super( new SpecialFunctionTableModel());
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( SpecialFunctionTableModel )model ).set( remoteConfig );
  }
  
  protected SpecialProtocolFunction createRowObject( SpecialProtocolFunction baseFunction )
  {
    return SpecialFunctionDialog.showDialog(( JFrame )SwingUtilities.getRoot( this ), baseFunction, (( SpecialFunctionTableModel )model ).getRemoteConfig());
  }
}
  
