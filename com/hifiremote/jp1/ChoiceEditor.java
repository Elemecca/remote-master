package com.hifiremote.jp1;

import java.awt.Component;
import javax.swing.*;
import javax.swing.table.*;

public class ChoiceEditor
  extends DefaultCellEditor
{
  public ChoiceEditor( Choice[] choices )
  {
    this( choices, true );
  }

  public ChoiceEditor( Choice[] choices, boolean allowNull )
  {
    super( new JComboBox());
    setClickCountToStart( 1 );
    this.choices = choices;
    this.allowNull = allowNull;

    comboBox = ( JComboBox )getComponent();

    initialize();
  }

  public void initialize()
  {
    int visibleCount = 0;
    for ( int i = 0; i < choices.length; i++ )
    {
      if ( !choices[ i ].isHidden())
      {
        visibleCount++;
      }
    }

    if ( allowNull )
      visibleCount++;

    Choice[] temp = new Choice[ visibleCount ];
    int tempIndex = 0;
    if ( allowNull )
    {
      temp[ 0 ] = new Choice( -1, "" );
      tempIndex = 1;
    }
    for ( int i = 0; i < choices.length; i++ )
    {
      if ( !choices[ i ].isHidden())
      {
        temp[ tempIndex++ ] = choices[ i ];
      }
    }
    comboBox.setModel( new DefaultComboBoxModel( temp ));
  }

  public Object getCellEditorValue()
  {
    Choice temp = ( Choice )super.getCellEditorValue();
    if ( temp.getIndex() == -1 )
      return null;
    else
      return temp;
  }

  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int col )
  {
    if ( value != null )
      comboBox.setSelectedItem( value );
    else
      comboBox.setSelectedIndex( 0 );
    return comboBox;
  }

  private JComboBox comboBox = null;
  private Choice[] choices = null;
  private boolean allowNull;
}
