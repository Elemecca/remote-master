/**
 * 
 */
package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Greg
 */
public class RemoteComboBoxRenderer implements ListCellRenderer
{
  public RemoteComboBoxRenderer( ListCellRenderer delegate )
  {
    this.delegate = delegate;
  }

  public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
      boolean cellHasFocus )
  {
    if ( value instanceof Remote )
    {
      Remote r = ( Remote )value;
      if ( showRemoteSignature )
      {
        value = r.getName() + " (" + r.getSignature() + ')';
      }
      else
      {
        value = r.getName();
      }
    }

    return delegate.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
  }

  private ListCellRenderer delegate = null;

  private boolean showRemoteSignature = false;

  public void setShowRemoteSignature( boolean showRemoteSignature )
  {
    this.showRemoteSignature = showRemoteSignature;
  }
}
