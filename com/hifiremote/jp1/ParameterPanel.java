package com.hifiremote.jp1;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import layout.TableLayout;

public class ParameterPanel
  extends JPanel
{
  public ParameterPanel( Parameter[] parms )
  {
    double p = TableLayout.PREFERRED;
    double f = TableLayout.FILL;
    double ic = 10;
    double ir = 5;
    double sizes[][] =
    {
      { p, ic, f }, // cols
      {}            // rows
    };
    TableLayout tl = new TableLayout( sizes );
    setLayout( tl );

    parameters = parms;
    int row = 0;
    for ( int i = 0; i < parms.length; i++ )
    {
      if ( i != 0 )
        tl.insertRow( row++, ir );

      tl.insertRow( row, p );
      JLabel label = new JLabel( parms[ i ].getName() + ':', SwingConstants.RIGHT );
      add( label, "0, " + row );

      add( parms[ i ].getComponent(), "2, " + row++ );
    }
    tl.insertRow( row++, ir );
    tl.insertRow( row, p );
    JLabel label = new JLabel( "Fixed data:", SwingConstants.RIGHT );
    JTextField fixedData = new JTextField();
    fixedData.setEditable( false );
    add( label, "0, " + row );
    add( fixedData, "2, " + row );
  }

  public int[] getParms()
  {
    int[] rc = new int[ parameters.length ];

    for ( int i = 0; i < parameters.length; i++ )
    {
      rc[ i ] = parameters[ i ].getValue();
    }
    return rc;
  }

  public int[] getParmsWithDefaults()
  {
    int[] rc = new int[ parameters.length ];

    for ( int i = 0; i < parameters.length; i++ )
    {
      int val = parameters[ i ].getValue();
      if ( val == -1 )
        val = parameters[ i ].getDefaultValue();
      rc[ i ] = val;
    }
    return rc;
  }

  public void setParms( int[] parms )
  {
    if ( parms == null )
      return;

    for ( int i = 0; i < parameters.length; i++ )
    {
      parameters[ i ].setValue( parms[ i ]);
    }
  }

  private Parameter[] parameters = null;
}
