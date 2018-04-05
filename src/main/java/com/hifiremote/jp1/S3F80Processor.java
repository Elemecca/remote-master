package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class S3F80Processor.
 */
public class S3F80Processor
  extends S3C80Processor
{
  
  /**
   * Instantiates a new s3 f80 processor.
   */
  public S3F80Processor()
  {
    super( "S3F80" );
    setRAMAddress( newRAMAddress );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Processor#getEquivalentName()
   */
  public String getEquivalentName()
  {
    return "S3C80";
  }
}
