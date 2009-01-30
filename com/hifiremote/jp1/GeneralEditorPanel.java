package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;
import javax.swing.text.MaskFormatter;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneralEditorPanel.
 */
public class GeneralEditorPanel
  extends ProtocolEditorPanel
  implements ActionListener, DocumentListener, PropertyChangeListener
{
  
  /**
   * Instantiates a new general editor panel.
   */
  public GeneralEditorPanel()
  {
    super( "General Settings" );
    JPanel contentPane = new JPanel( new BorderLayout());
    add( contentPane, BorderLayout.CENTER );

    SpringLayout layout = new SpringLayout();
    JPanel formPanel = new JPanel( layout );
    contentPane.add( formPanel, BorderLayout.NORTH );
    name = new JTextField( 20 );
    oldNames = new JTextField();
    MaskFormatter f = null;
    try
    {
      f = new MaskFormatter( "HH HH" );
    }
    catch (Exception e )
    {
      e.printStackTrace( System.err );
    }
    f.setValueClass( Hex.class );
    id = new JFormattedTextField( f );
    id.addPropertyChangeListener( "value", this );
    altId = new JFormattedTextField( f );
    altId.addPropertyChangeListener( "value", this );

    String[] labels = { "Name", "Old names", "ID", "Alternate ID" };
    JTextField[] fields = { name, oldNames, id, altId };
    String[] toolTipText = { "Enter the name of the protocol.  This is a required field.",
                             "Enter the names, separated by commas, that have been used for this protocol, or have been used by KM.",
                             "Enter the hex identifier for this protocol.  This is a required fields.",
                             "Enter the alternate ID for this protocol." };

    int numPairs = labels.length;
    for ( int i = 0; i < numPairs; i++)
    {
      JLabel l = new JLabel(labels[i], JLabel.TRAILING);
      formPanel.add( l );
      JTextField textField = fields[ i ];
      int height = textField.getPreferredSize().height;
      Dimension d = textField.getMaximumSize();
      d.height = height;
      textField.setMaximumSize( d );
      textField.getDocument().addDocumentListener( this );
      l.setLabelFor( textField );
      textField.setToolTipText( toolTipText[ i ]);
      formPanel.add( textField );
    }

    // Lay out the panel.
    SpringUtilities.makeCompactGrid( formPanel,
                                     numPairs, 2,  // rows, cols
                                     5, 5,      // initX, initY
                                     5, 5 );    // xPad, yPad

    JPanel panel = new JPanel( new BorderLayout());
    JPanel tablePanel = new JPanel( new BorderLayout());
    panel.add( tablePanel, BorderLayout.NORTH );
    contentPane.add( panel, BorderLayout.CENTER );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Protocol code" ));
    tableModel = new TableModel();
    JTableX table = new JTableX( tableModel );
    DefaultTableCellRenderer r = ( DefaultTableCellRenderer )table.getDefaultRenderer( String.class );
    r.setHorizontalAlignment( SwingConstants.CENTER );
    table.setDefaultEditor( Hex.class, new HexEditor());
    tablePanel.add( table.getTableHeader(), BorderLayout.NORTH );
    tablePanel.add( table, BorderLayout.CENTER );

    JLabel l = new JLabel( colNames [ 0 ]);
    l.setBorder( BorderFactory.createEmptyBorder( 0, 4, 0, 4 ));

    TableColumnModel columnModel = table.getColumnModel();
    TableColumn column = columnModel.getColumn( 0 );
    int width = l.getPreferredSize().width;

    for ( int i = 0; i < procNames.length; i++ )
    {
      l.setText( procNames[ i ]);
      width =  Math.max( width, l.getPreferredSize().width );
    }
    for ( int i = 0; i < procNames.length; i++ )
    {
      column.setMinWidth( width );
      column.setMaxWidth( width );
      column.setPreferredWidth( width );
    }
    table.doLayout();

//    int height = table.getRowCount() * ( table.getRowHeight() + table.getRowMargin() );
//    Dimension d = table.getPreferredSize();
//    d.height = height;
//    table.setMinimumSize( d );
//    table.setMaximumSize( d );
//
    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
    tablePanel.add( buttonPanel, BorderLayout.SOUTH );

    importButton = new JButton( "Import from clipboard" );
    importButton.addActionListener( this );
    importButton.setToolTipText( "Import protocol code from the clipboard" );
    buttonPanel.add( importButton );

    setText( "Enter the requested information about the protocol." );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorPanel#commit()
   */
  public void commit(){;}
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.ProtocolEditorPanel#update(com.hifiremote.jp1.ProtocolEditorNode)
   */
  public void update( ProtocolEditorNode newNode )
  {
    node = ( GeneralEditorNode )newNode;
    name.setText( node.getName());
    oldNames.setText( node.getOldNames());
    id.setText( node.getId().toString());
    altId.setText( node.getAltId().toString());
  }

  // ActionListener
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
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

  // DocumentListener methods
  /**
   * Doc changed.
   * 
   * @param e the e
   */
  public void docChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();
    if ( doc == name.getDocument() )
      node.setName( name.getText());
    else if ( doc == oldNames.getDocument())
      node.setOldNames( oldNames.getText());
  }

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  // PropertyChangeListener methods
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == id )
      node.setId( ( Hex )id.getValue());
    else if ( source == altId )
      node.setAltId( ( Hex )altId.getValue());
  }
  
  /**
   * The Class TableModel.
   */
  public class TableModel
    extends AbstractTableModel
  {
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount(){ return procNames.length; }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount(){ return colNames.length; }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName( int col ){ return colNames[ col ]; }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class<?> getColumnClass( int col ){ return classes[ col ]; }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable( int row, int col ){ return ( col == 1 ); }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int row, int col )
    {
      if ( col == 0 )
        return procNames[ row ];
      else
        return node.getCode( procNames[ row ]);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
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

  /**
   * Import protocol code.
   * 
   * @param string the string
   */
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
          StringTokenizer st2 = new StringTokenizer( text, "()=" );
          st2.nextToken(); // discard everything before the =
          String pidStr = st2.nextToken().trim();
          System.err.println( "Imported pid is " + pidStr );
          processor = st2.nextToken().trim();
          System.err.println( "processorName is " + processor );
          if ( processor.startsWith( "S3C8" ))
            processor = "S3C80";
          if ( st2.hasMoreTokens())
          {
            String importedName = st2.nextToken().trim();
            System.err.println( "importedName is " + importedName );
          }
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

  /** The node. */
  private GeneralEditorNode node = null;
  
  /** The name. */
  private JTextField name = null;
  
  /** The old names. */
  private JTextField oldNames = null;
  
  /** The id. */
  private JFormattedTextField id = null;
  
  /** The alt id. */
  private JFormattedTextField altId = null;
  
  /** The table model. */
  private TableModel tableModel = null;
  
  /** The import button. */
  private JButton importButton = null;
  
  /** The col names. */
  private static String[] colNames = { "Processor", "Protocol Code" };
  
  /** The classes. */
  private static Class<?>[] classes = { String.class, Hex.class };
  
  /** The proc names. */
  private static String[] procNames = { "S3C80", "740", "6805-C9", "6805-RC16/18", "S3F80" };

}
