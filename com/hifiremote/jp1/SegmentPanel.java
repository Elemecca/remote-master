package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public class SegmentPanel extends RMPanel implements ActionListener, ListSelectionListener
{
  private SegmentTableModel model = null;
  private SegmentTable table = null;
  private RemoteConfiguration config = null;
  private JLabel signatureLabel = new JLabel();  
  private JLabel processorLabel = new JLabel();  
  private JLabel interfaceLabel = new JLabel();  
  private JLabel infoLabel1 = new JLabel( "Data in black: RMIR data displayed" );
  private JLabel infoLabel2 = new JLabel( "Data in blue: Original data displayed" );
  private JLabel infoLabel3 = new JLabel( "Select between them with \"Preserve original data\"" );
  private JLabel infoLabel4 = new JLabel( "    on the Advanced menu." );
  private JButton applyButton = new JButton( "Apply" );
  private JButton undoButton = new JButton( "Undo" );
  private JButton upButton = new JButton( "Move up" );
  private JButton downButton = new JButton( "Move down" );
  private JButton insertButton = new JButton( "Insert" );
  private JButton deleteButton = new JButton( "Delete" );
  private JButton rmirButton = new JButton( "Sort into RMIR order" );
  
  protected class SegmentTable extends JP1Table
  {
    public SegmentTable( TableModel model )
    {
      super( model );
    }
    
    public String getToolTipText( MouseEvent e )
    {
      return null;
    }
  }
  
  public SegmentPanel()
  {    
    model = new SegmentTableModel();
    table = new SegmentTable( model );
    table.getSelectionModel().addListSelectionListener( this );
    model.setTable( table );
    table.setGridColor( Color.lightGray );
    JScrollPane scrollPane = new JScrollPane( table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    table.setPreferredScrollableViewportSize( d );
    add( scrollPane, BorderLayout.CENTER );
    
    String message = "When original data is displayed, segments can be edited, "
        + "inserted, deleted or re-ordered.  When RMIR data is displayed, they can "
        + "only be edited, and that only for those segment types not otherwise set "
        + "by RMIR.\n\n"
        + "Edits are not applied to the data concerned until the Apply button is "
        + "pressed.  Until that time they can be undone with the Undo button.\n\n"
        + "Upload will upload whichever of original or RMIR data is currently "
        + "selected.";
    JTextArea note = new JTextArea( message );
    note.setFont( ( new JLabel() ).getFont() );
    note.setBackground( ( new JLabel() ).getBackground() );
    note.setLineWrap( true );
    note.setWrapStyleWord( true );
    
    double b = 2;
    double c = 5;
    double pr = TableLayout.PREFERRED;
    double pf = TableLayout.FILL;
    double size[][] =
    {
        {
            2*c, pr, b, pr, pr, 2*c
        }, // cols
        {
            c, pr, pr, pr, pr, 2*c, pr, c, pr, c, pr, 8*c, pr, 2*c, pr, b, pr, pr,
            2*c, pr, pf
        }  // rows
    };
    
    JPanel buttonPanel = new JPanel( new TableLayout( size ) );
    buttonPanel.add( infoLabel1, "1, 1, 4, 1" );
    buttonPanel.add( infoLabel2, "1, 2, 4, 2" );
    buttonPanel.add( infoLabel3, "1, 3, 4, 3" );
    buttonPanel.add( infoLabel4, "1, 4, 4, 4" );
    buttonPanel.add( signatureLabel, "1, 6, 4, 6" );
    buttonPanel.add( processorLabel, "1, 8, 4, 8" );
    buttonPanel.add( interfaceLabel, "1, 10, 4, 10" );
    
    buttonPanel.add( applyButton, "1, 12" );
    buttonPanel.add( undoButton, "3, 12" );
    buttonPanel.add( insertButton, "1, 14" );
    buttonPanel.add( deleteButton, "3, 14" );
    buttonPanel.add( upButton, "1, 16"  );
    buttonPanel.add( downButton, "3, 16");
    buttonPanel.add( rmirButton, "1, 17, 3, 17" );
    buttonPanel.add( note, "1, 19, 4, 19" );

    add( buttonPanel, BorderLayout.LINE_END );
    applyButton.addActionListener( this );
    undoButton.addActionListener( this );
    insertButton.addActionListener( this );
    deleteButton.addActionListener( this );
    upButton.addActionListener( this );
    downButton.addActionListener( this );
    rmirButton.addActionListener( this );
    
    Font boldFont = ( new JLabel() ).getFont().deriveFont( Font.BOLD );
    infoLabel1.setFont( boldFont );
    infoLabel2.setFont( boldFont );
    infoLabel2.setForeground( Color.BLUE );
  }

  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    this.config = remoteConfig;
    Remote remote = null;
    setButtons();
    if ( remoteConfig != null && remoteConfig.hasSegments() && !( remote = remoteConfig.getRemote() ).isSSD() )
    {
      model.set( remoteConfig );
      String sig = remoteConfig.getSigString();
      if ( sig == null )
      {
        sig = remote.getSignature();
      }
      signatureLabel.setText( "Signature:  " + sig );
      processorLabel.setText( "Processor:  " + remote.getProcessorDescription() );
      interfaceLabel.setText( "Interface:  " + remote.getInterfaceType() );
    }
  }

  @Override
  public void addRMPropertyChangeListener( PropertyChangeListener l )
  {
    if ( model != null && l != null )
    {
      model.addPropertyChangeListener( l );
    }
  }

  @Override
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    Remote remote = config.getRemote();
    int row = table.getSelectedRow();
    finishEditing();
    if ( source == applyButton )
    {
      model.updateData();
      model.setChanged( false );
    }
    else if ( source == undoButton )
    {
      model.resetData();
      model.setSorted( false );
      model.setChanged( true );
    }
    else if ( source == upButton || source == downButton )
    {
      int index = row + ( source == upButton ? -1 : 1 );
      model.moveRow( Math.min( row, index ), Math.max( row, index ) );
      model.resetAddresses();
      if ( model.getRow( row ).get_Type() != model.getRow( index ).get_Type() )
      {
        model.setSorted( false );
      }
//      writeSavedData();
      model.setChanged( true );
      table.setRowSelectionInterval( index, index );
    }
    else if ( source == deleteButton )
    {
      model.removeRow( row );
      model.resetAddresses();
      if( row >= model.getRowCount() )
      {
        row--;
      }
//      writeSavedData();
      model.setChanged( true );
      table.setRowSelectionInterval( row, row );
    }
    else if ( source == insertButton )
    {
      Segment seg = new Segment( model.getRow( row ).get_Type(), 0xFF, new Hex( 0 ) );
      model.insertRow( row , seg );
      model.resetAddresses();
      model.setChanged( true );
//      writeSavedData();
      table.setRowSelectionInterval( row, row );
    }
    else if ( source == rmirButton )
    {
      LinkedHashMap< Integer, List<Segment> > segMap = new LinkedHashMap< Integer, List<Segment> >();
      for ( Segment seg : model.getData() )
      {
        int type = seg.get_Type();
        List< Segment > list = segMap.get( type );
        if ( list == null )
        {
          list = new ArrayList< Segment >();
          segMap.put( type, list );
        }
        list.add( seg );
      }
      model.getData().clear();
      for ( int key : remote.getSegmentTypes() )
      {
        List< Segment > list = segMap.get( key );
        if ( list != null )
        {
          model.getData().addAll( list );
        }
      }
      for ( int key : segMap.keySet() )
      {
        if ( remote.getSegmentTypes().contains( key ) )
        {
          continue;
        }
        List< Segment > list = segMap.get( key );
        if ( list != null )
        {
          model.getData().addAll( list );
        }
      }
      model.fireTableDataChanged();
    
//      int pos = remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
//      for ( int key : remote.getSegmentTypes() )
//      {
//        List< Segment > list = segMap.get( key );
//        if ( list != null )
//        {
//          pos = Segment.writeData( list, config.getSavedData(), pos );
//        }
//      }
//      for ( int key : segMap.keySet() )
//      {
//        if ( remote.getSegmentTypes().contains( key ) )
//        {
//          continue;
//        }
//        List< Segment > list = segMap.get( key );
//        pos = Segment.writeData( list, config.getSavedData(), pos );
//      }
//      Hex.put( 0xFFFF, config.getSavedData(), pos );
//      model.resetData();
      model.setSorted( true );
      model.setChanged( true );
    }
    setButtons();
  }
  
//  private void writeSavedData()
//  {
//    Remote remote = config.getRemote();
//    int pos = remote.usesEZRC() || remote.usesSimpleset() ? 20 : 2;
//    pos = Segment.writeData( model.getData(), config.getSavedData(), pos );
//    Hex.put( 0xFFFF, config.getSavedData(), pos );
//    model.resetData();
//  }
  
  protected void finishEditing()
  {
    int editRow = table.getEditingRow();
    if ( editRow != -1 )
    {
      TableCellEditor editor = table.getCellEditor( editRow, table.getEditingColumn() );
      if ( !editor.stopCellEditing() )
      {
        editor.cancelCellEditing();
      }
    }
  }
  
  private void setButtons()
  {
    int row = table.getSelectedRow();
    int count = table.getRowCount();
    boolean savedData = config.getOwner().useSavedData();
    upButton.setEnabled( savedData && row > 0 );
    downButton.setEnabled( savedData && row >= 0 && row < count - 1 );
    insertButton.setEnabled( savedData && row >= 0 );
    deleteButton.setEnabled( savedData && row >= 0 );
    applyButton.setEnabled( model.isChanged() );
    undoButton.setEnabled( model.isChanged() );
    rmirButton.setEnabled( savedData && !model.isSorted() );
  }

  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    setButtons();
  }
}
