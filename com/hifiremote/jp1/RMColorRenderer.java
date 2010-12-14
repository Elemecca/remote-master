package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class RMColorRenderer extends DefaultTableCellRenderer
{
  @Override
  public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int col )
  {     
    component = super.getTableCellRendererComponent( table, value, false, false, row, col );
    this.isSelected = isSelected;
    boolean editable = true;
    if ( table.getModel() instanceof TableSorter )
    {
      TableSorter sorter = ( TableSorter )table.getModel();
      if ( sorter.getTableModel() instanceof DeviceUpgradeTableModel )
      {
        DeviceUpgradeTableModel devModel = ( DeviceUpgradeTableModel )sorter.getTableModel();
        editable = devModel.isCellEditable( sorter.modelIndex( row ), col );
      }
    }
    color = editable ? ( Color )value : Color.WHITE;
    setText( editable ? "" : "n/a" );
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
