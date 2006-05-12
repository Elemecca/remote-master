package com.hifiremote.jp1;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

public class FormattedRenderer
  extends DefaultTableCellRenderer
{
  private Format format;
  public FormattedRenderer( Format format )
  {
    super();
    this.format = format;
  }

  protected void setValue( Object value )
  {
    super.setValue( format.format( value ));
  }
}
