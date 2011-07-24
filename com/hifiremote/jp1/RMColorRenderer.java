package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class RMColorRenderer extends DefaultTableCellRenderer
{
  @Override
  public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int col )
  {     
    component = super.getTableCellRendererComponent( table, value, false, false, row, col );
    this.isSelected = isSelected;
    boolean editable = true;
    String usage = "";
    
    TableModel model = table.getModel();
    if ( model instanceof TableSorter )
    {
      row = ( (  TableSorter )model ).modelIndex( row );
      model = ( (  TableSorter )model ).getTableModel();
    }
    if ( model instanceof JP1TableModel< ? > )
    {
      Object item = ( ( JP1TableModel< ? > )model ).getRow( row );
      if ( item instanceof Highlight )
      {
        usage = Integer.toString( ( ( Highlight )item ).getMemoryUsage() );
      }

      if ( model instanceof DeviceUpgradeTableModel )
      {
        editable = ( ( DeviceUpgradeTableModel )model ).isCellEditable( row, col );
        if ( ( ( DeviceUpgradeTableModel )model ).getEffectiveColumn( col ) == 10 )
        {
          usage = Integer.toString( ( ( DeviceUpgrade )item ).getProtocolMemoryUsage() );
        }
      }
      else if ( model instanceof SettingsTableModel )
      {
        usage += ( ( ( Highlight )item ).getMemoryUsage() == 1 ) ? " bit" : " bits";
      }
    }
    
    color = editable ? ( Color )value : Color.WHITE;
    setText( editable ? usage : "n/a" );
    setForeground( editable ? Color.BLACK : Color.GRAY );
    return component;
  }
  
  @Override
  public void paint( Graphics g )
  {
    Dimension d = component.getSize();
    if ( isSelected )
    {
      g.setColor( Color.BLACK );
      g.fillRect( 0, 0, d.width, d.height );
      g.setColor( color );
      g.fillRect( 2, 2, d.width-4, d.height-4 );
    }
    else
    {
      g.setColor( color );
      g.fillRect( 0, 0, d.width, d.height );
    }
    super.paint( g );
  }
  
  private Component component;
  private boolean isSelected;
  private Color color;

}
