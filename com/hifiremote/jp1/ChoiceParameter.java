package com.hifiremote.jp1;

import javax.swing.JComponent;
import javax.swing.JComboBox;

public class ChoiceParameter
  extends Parameter
{
  public ChoiceParameter( String name, int defaultValue, String[] choices )
  {
    super( name, defaultValue );
    comboBox = new JComboBox( choices );
    String helpText = "Select a value from the list.";
    if ( defaultValue != -1 )
      helpText += "  The default value is " + choices[ defaultValue + 1 ] + '.';
    comboBox.setToolTipText( helpText );
  }

  public JComponent getComponent(){ return comboBox; }
  public int getValue(){ return comboBox.getSelectedIndex() - 1; }
  public void setValue( int val )
  {
    comboBox.setSelectedIndex( val + 1 );
  }

  private JComboBox comboBox = null;
}
