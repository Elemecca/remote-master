package com.hifiremote.jp1;

import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class OBCProtocol
  extends Protocol
{
  public OBCProtocol( String name, byte[] id, Properties props )
  {
    super( name, id, props );
    String temp = props.getProperty( "OBCTranslator" );
    if ( temp != null )
    obcTranslators = TranslatorFactory.createTranslators( temp );
  }

  public int getColumnCount()
  {
    return 1;
  }

  public Class getColumnClass( int column )
  {
    Class rc = null;
    if ( column == 0 )
      rc = Integer.class;
    return rc;
  }

  public String getColumnName( int column )
  {
    String rc = null;
    if ( column == 0 )
      rc = "OBC";
    return rc;
  }

  public TableCellEditor getColumnEditor( int column )
  {
    TableCellEditor rc = null;
    if ( column == 0 )
      rc = new ByteEditor();
    return rc;
  }

  public TableCellRenderer getColumnRenderer( int column )
  {
    TableCellRenderer rc = null;
    if ( column == 0 )
        rc = new ByteRenderer();
    return rc;
  }

  public Object getValueAt( int column, byte[] hex )
  {
    Object rc = null;
    if ( column == 0 )
    {
      int[] parms = new int[ getColumnCount()];
      for ( int i = 0; i < obcTranslators.length; i ++ )
        obcTranslators[ i ].out( hex, parms );
      rc = new Integer( parms[ column ]);
    }
    return rc;
  }

  public void setValueAt( int column, byte[] hex, Object value )
  {
    if ( column == 0 )
    {
      int[] parms = new int[ getColumnCount() ];
      parms[ column ] = (( Integer )value ).intValue();
      for ( int i = 0; i < obcTranslators.length; i ++ )
        obcTranslators[ column ].in( parms, hex );
    }
  }

  public boolean isEditable( int column )
  {
    return true;
  }

  private static Class[] columnClasses =
  { Integer.class };

  private static String[] columnTitles =
  { "OBC" };

  private JTextField mainDevice = null;
  private JTextField subDevice = null;
  private JTextField parm = null;
  private JTextField fixedDataField = null;
  private String[] names;
  private String[][] choices;
  private Translate[] obcTranslators = null;
}
