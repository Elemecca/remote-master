package com.hifiremote.jp1;

public class AssemblerItem
{
  private int address = 0;
  private Hex hex = null;
  private String label = "";
  private String operation = "";
  private String argumentText = "";
  private String comments = "";
  
  public int getAddress()
  {
    return address;
  }
  public void setAddress( int address )
  {
    this.address = address;
  }
  public Hex getHex()
  {
    return hex;
  }
  public void setHex( Hex hex )
  {
    this.hex = hex;
  }
  public String getLabel()
  {
    return label;
  }
  public void setLabel( String label )
  {
    this.label = label;
  }
  public String getOperation()
  {
    return operation;
  }
  public void setOperation( String operation )
  {
    this.operation = operation;
  }
  public String getArgumentText()
  {
    return argumentText;
  }
  public void setArgumentText( String argumentText )
  {
    this.argumentText = argumentText;
  }
  public String getComments()
  {
    return comments;
  }
  public void setComments( String comments )
  {
    this.comments = comments;
  }
  
}
