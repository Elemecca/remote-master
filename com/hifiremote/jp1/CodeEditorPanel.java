package com.hifiremote.jp1;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class CodeEditorPanel
  extends ProtocolEditorPanel
  implements ActionListener
{
  public CodeEditorPanel()
  {
    super( "Protocol Code" );

    JPanel panel = new JPanel( new BorderLayout());
    add( panel, BorderLayout.CENTER );

    tableModel = new TableModel();
    table = new JTable( tableModel );
    DefaultTableCellRenderer r = ( DefaultTableCellRenderer )table.getDefaultRenderer( String.class );
    r.setHorizontalAlignment( SwingConstants.CENTER );
    table.setDefaultEditor( Hex.class, new HexEditor());
    panel.add( new JScrollPane( table ), BorderLayout.CENTER );

    JLabel l = new JLabel( colNames [ 0 ]);
    l.setBorder( BorderFactory.createEmptyBorder( 0, 4, 0, 4 ));

    TableColumnModel columnModel = table.getColumnModel();
    TableColumn column = columnModel.getColumn( 0 );
    int width = l.getPreferredSize().width;

    for ( int i = 0; i < procNames.length; i++ )
    {
      l.setText( procNames[ i ]);
      width =  Math.max( width, l.getPreferredSize().width );
      column.setMaxWidth( width );
      column.setPreferredWidth( width );
    }

    table.doLayout();
     
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    panel.add( buttonPanel, BorderLayout.SOUTH );

    importButton = new JButton( "Import from clipboard" );
    importButton.addActionListener( this );
    importButton.setToolTipText( "Import protocol code from the clipboard" );    
    buttonPanel.add( importButton );

    setText( "Enter the requested information about the protocol.  Fields with names in red are required." );                                     
  }

  public void commit(){;}
  public void update( ProtocolEditorNode newNode )
  {
    node = ( CodeEditorNode )newNode;
  }

  // DocumentListener methods
  public void docChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();
  }

  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public void insertUpdate( DocumentEvent e )
  { 
    docChanged( e );
  }

  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  public class TableModel
    extends AbstractTableModel
  {
    public int getRowCount(){ return procNames.length; }
    public int getColumnCount(){ return colNames.length; }
    public String getColumnName( int col ){ return colNames[ col ]; }
    public Class getColumnClass( int col ){ return classes[ col ]; }
    public boolean isCellEditable( int row, int col ){ return ( col == 1 ); }
    public Object getValueAt( int row, int col )
    {
      if ( col == 0 )
        return procNames[ row ];
      else
        return node.getCode( procNames[ row ]);
    }
    public void setValueAt( Object value, int row, int col )
    {
      if ( col == 1 )
      {
        if ( value != null )
          node.addCode( procNames[ row ], ( Hex )value );
        else
          node.removeCode( procNames[ row ] );
        tableModel.fireTableRowsUpdated( row, row );
      }
    }
  }

  public void actionPerformed( ActionEvent e )
  {
    if ( e.getSource() == importButton )
    {
      System.err.println( "importButton pressed" );
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable clipData = clipboard.getContents( clipboard );
      if ( clipData != null )
      {
        try
        {
          if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
          {
            String s =
              ( String )( clipData.getTransferData( DataFlavor.stringFlavor ));
            System.err.println( "text is " + s );
            importProtocolCode( s );
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace( System.err );
        }
      }
    }
  }

  private void importProtocolCode( String string )
  {
    StringTokenizer st = new StringTokenizer( string, "\n" );
    String text = null;
    String processor = null;
    while( st.hasMoreTokens())
    {
      while ( st.hasMoreTokens())
      {
        text = st.nextToken().toUpperCase();
        System.err.println( "got '" + text );
        if ( text.startsWith( "UPGRADE PROTOCOL 0 =" ))
        {
          int pos = text.indexOf( '(' );
          int pos2 = text.indexOf( ')', pos );
          processor = text.substring( pos + 1, pos2 );
          System.err.println( "processorName is " + processor );
          if ( processor.startsWith( "S3C8" ))
            processor = "S3C80";
          break;
        }
      }
      if ( st.hasMoreTokens())
      {
        text = st.nextToken(); // 1st line of code
        while ( st.hasMoreTokens())
        {
          String temp = st.nextToken();
          if ( temp.equals( "End" ))
            break;
          text = text + ' ' + temp;
        }
        System.err.println( "getting processor with name " + processor );
        Processor p = ProcessorManager.getProcessor( processor );
        if ( p != null )
          processor = p.getFullName();
        System.err.println( "Adding code for processor " + processor );
        System.err.println( "Code is "  + text );
        node.addCode( processor, new Hex( text ));
        for ( int i = 0; i < procNames.length; i++ )
        {
          if ( procNames[ i ].equals( processor ))
            tableModel.fireTableRowsUpdated( i, i );
        }
      }
    }
  }

  private CodeEditorNode node = null;
  private JButton importButton = null;
  private TableModel tableModel = null;
  private JTable table = null;
  private static String[] colNames = { "Processor", "Protocol Code" };
  private static Class[] classes = { String.class, Hex.class };
  private static String[] procNames = { "S3C80", "740", "6805-C9", "6805-RC16/18" };
}
