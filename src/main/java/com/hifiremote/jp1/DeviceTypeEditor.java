package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

public class DeviceTypeEditor extends DefaultCellEditor
{
  // The purpose of this class is to enforce the restrictions required of the
  // Device Type column in the Device Buttons table when a remote uses Soft
  // Home Theater.  The Soft Home Theater type can occur at most once, and if
  // it does not occur then at least one device type slot must be left empty,
  // with Soft Home Theater being the only permitted entry in the last empty
  // slot.
  
  private DefaultComboBoxModel model;
  private JComboBox comboBox;
  private SoftHomeTheater softHT;

  public DeviceTypeEditor( JComboBox comboBox, SoftHomeTheater softHT )
  {
    super(new JComboBox() );
    model = (DefaultComboBoxModel)((JComboBox)getComponent()).getModel();
    this.comboBox = comboBox;
    this.softHT = softHT;
  }
  
  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
  {
    if ( column == 2 )
    {
      int rows = table.getRowCount();
      int rowHT = -1;
      int rowLastEmpty = -1;
      int rowsFilled = 0;
      boolean softHTInUse = softHT.inUse();
      int softHTType = softHT.getDeviceType();
      DeviceType device = null;
      if ( softHTInUse )
      {
        for ( int i = 0; i < rows; i++ )
        {
          device = ( DeviceType )table.getValueAt( i, 2 );
          if ( device != null )
          {
            rowsFilled++;
            if ( device.getNumber() == softHTType )
            {
              rowHT = i;
            }
          }
          else
          {
            rowLastEmpty = i;
          }
        }
      }
      
      model.removeAllElements();      
      DefaultComboBoxModel modelIn = ( DefaultComboBoxModel )comboBox.getModel();      
      for ( int i = 0; i < modelIn.getSize(); i++ )
      {
        device = ( DeviceType )modelIn.getElementAt( i );
        if ( !softHTInUse || ( rowHT == -1 && rowsFilled < rows - 1 )
            || ( rowHT == -1 && rowsFilled == rows - 1  
              && ( row != rowLastEmpty || device.getNumber() == 0xFF || device.getNumber() == softHTType ) )
            || ( row == rowHT ) && ( rowsFilled < rows || device.getNumber() == 0xFF 
                || device.getNumber() == softHTType )
            || ( rowHT != -1 && row != rowHT && device.getNumber() != softHTType ) )
        {
          model.addElement( modelIn.getElementAt( i ) );
        }
      }
    }
    return super.getTableCellEditorComponent(table, value, isSelected, row, column);
  }
}
