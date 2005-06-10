package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class FunctionImportDialog
  extends JDialog
  implements ActionListener
{
  public FunctionImportDialog( JFrame owner, DeviceUpgrade upgrade )
  {
    super( owner, "Import Functions", true );
    setLocationRelativeTo( owner );

    Container contentPane = getContentPane();

    JLabel instructions = new JLabel( "Select the functions to be imported." );
    contentPane.add( instructions, BorderLayout.NORTH );
    
    for ( Enumeration e = upgrade.getFunctions().elements(); e.hasMoreElements(); )
    {
      Function f = ( Function )e.nextElement();
      if (( f.getName() != null ) && ( f.getName().length() > 0 ) &&
          ( f.getHex() != null ) && ( f.getHex().length() > 0 ))
        data.add( new SelectHolder( f ));
    }
    for ( Enumeration e = upgrade.getExternalFunctions().elements(); e.hasMoreElements(); )
    {
      Function f = ( Function )e.nextElement();
      if (( f.getName() != null ) && ( f.getName().length() > 0 ) &&
          ( f.getHex() != null ) && ( f.getHex().length() > 0 ))
        data.add( new SelectHolder( f ));
    }

    model = new AbstractTableModel()
    {
      public String getColumnName(int col) 
      {
        if ( col == 0 ) return " ";
        else return "Function";
      }
      public Class getColumnClass( int col )
      {
        if ( col == 0 )
          return Boolean.class;
        else
          return String.class;
      }
      public int getRowCount() { return data.size(); }
      public int getColumnCount() { return 2; }
      public Object getValueAt( int row, int col ) 
      {
        SelectHolder h = ( SelectHolder )data.elementAt( row );
        if ( col == 0 )
        {
          if ( h.isSelected())
            return Boolean.TRUE;
          else
            return Boolean.FALSE;
        }
        else
          return h.getData().getName();
      }
    
      public boolean isCellEditable(int row, int col)
      { return ( col == 0 ); }

      public void setValueAt(Object value, int row, int col) 
      {
        SelectHolder h = ( SelectHolder )data.elementAt( row );
        h.setSelected((( Boolean )value ).booleanValue());
      }
    };

    JTable table = new JTable( model );
    table.setRowSelectionAllowed( false );
    table.setColumnSelectionAllowed( false );
    table.setShowGrid( false );
    TableColumnModel columnModel = table.getColumnModel();
    TableColumn column = columnModel.getColumn( 0 );
    JCheckBox box = new JCheckBox();
    box.setSelected( true );      
    column.setMaxWidth( box.getPreferredSize().width );
  
    contentPane.add( new JScrollPane( table ), BorderLayout.CENTER );
    Box buttonPanel = Box.createHorizontalBox();
    buttonPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

    selectAll = new JButton( "Select All" );
    selectAll.addActionListener( this );
    buttonPanel.add( selectAll );

    buttonPanel.add( Box.createHorizontalStrut( 5 ));

    selectNone = new JButton( "Select None" );
    selectNone.addActionListener( this );
    buttonPanel.add( selectNone );

    buttonPanel.add( Box.createHorizontalStrut( 5 ));

    selectToggle = new JButton( "Toggle" );
    selectToggle.addActionListener( this );
    buttonPanel.add( selectToggle );

    buttonPanel.add( Box.createHorizontalGlue());

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    buttonPanel.add( ok );

    buttonPanel.add( Box.createHorizontalStrut( 5 ));

    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    buttonPanel.add( cancel );

    contentPane.add( buttonPanel, BorderLayout.SOUTH );

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
  }

  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    
    if (( source == selectAll ) || ( source == selectNone ))
    {
      boolean flag = ( source == selectAll );
      for ( Enumeration enum = data.elements(); enum.hasMoreElements(); )
      {
        SelectHolder h = ( SelectHolder )enum.nextElement();
        h.setSelected( flag );
      }
      model.fireTableDataChanged();
    }
    else if ( source == selectToggle )
    {
      for ( Enumeration enum = data.elements(); enum.hasMoreElements(); )
      {
        SelectHolder h = ( SelectHolder )enum.nextElement();
        h.setSelected( !h.isSelected());
      }
      model.fireTableDataChanged();
    }
    else if ( source == cancel )
    {
      userAction = JOptionPane.CANCEL_OPTION;
      setVisible( false );
      dispose();
    }
    else if ( source == ok )
    {
      userAction = JOptionPane.OK_OPTION;
      setVisible( false );
      dispose();
    }
  }

  public Vector getSelectedFunctions()
  {
    Vector v = new Vector();
    for ( Enumeration e = data.elements(); e.hasMoreElements(); )
    {
      SelectHolder h = ( SelectHolder )e.nextElement();
      if ( h.isSelected())
        v.add( h.getData());
    }
    return v;
  }

  public int getUserAction()
  {
    return userAction;
  }

  private void transfer( JList fromList, JList toList )
  {
    DefaultListModel fromModel = ( DefaultListModel )fromList.getModel();
    int fromIndex = fromList.getMaxSelectionIndex();
    int first = fromList.getMinSelectionIndex();
    DefaultListModel toModel = ( DefaultListModel )toList.getModel();
    int toIndex = toModel.getSize();
    
    while ( fromIndex >= first )
    {
      if ( fromList.isSelectedIndex( fromIndex ))
      {
        Function f = ( Function )fromModel.getElementAt( fromIndex );
        fromModel.removeElementAt( fromIndex );

        while ( toIndex > 0 )
        {
          Function f2 = ( Function )toModel.elementAt( toIndex - 1 );
          int rc = f2.getName().compareTo( f.getName());
          
          if ( rc < 0 )
            break;
          --toIndex;
        }
        toModel.add( toIndex, f );
      }
      --fromIndex;
    }
  }

  public class SelectionRenderer
    extends JCheckBox
    implements ListCellRenderer
  {
    public SelectionRenderer()
    {
      setOpaque( true );
    }
    public Component getListCellRendererComponent(
      JList list, Object value, int index, boolean isSelected, boolean hasFocus )
    {
      setText( value.toString());
      setSelected( isSelected );
      return this;
    }
  }

  public class SelectHolder
  {
    public SelectHolder( Function f )
    {
      this.data = f;
    }
    public boolean isSelected(){ return selected; }
    public void setSelected( boolean flag ){ selected = flag; }
    public Function getData(){ return data; }
    private boolean selected = false;
    private Function data = null;
  }

  private Vector data = new Vector();
  private AbstractTableModel model = null;
  private JButton selectAll = null;
  private JButton selectNone = null;
  private JButton selectToggle = null;
  private JButton ok = null;
  private JButton cancel = null;
  private int userAction = JOptionPane.CANCEL_OPTION;
}
