package com.hifiremote.jp1;

import java.util.*;

public class ManualProtocol
  extends Protocol
{
  public ManualProtocol( String name )
  {
    super( name, null, null );
  }

  public void setId( Hex id )
  {
    this.id = id;
  }
}
