package com.hifiremote.jp1;

public class Choice
{
  public Choice( int index, String text )
  {
    this( index, text, false );
  }

  public Choice( int index, String text, boolean hidden )
  {
    this.index = index;
    this.text = text;
    this.hidden = hidden;
  }

  public String toString(){ return text; }
  public int getIndex(){ return index; }
  public String getText(){ return text; }
  public void setText( String text )
  {
    this.text = text;
  }
  public boolean isHidden(){ return hidden; }
  public void setHidden( boolean flag )
  {
    this.hidden = flag;
  }

  private int index;
  private String text;
  private boolean hidden;
}
