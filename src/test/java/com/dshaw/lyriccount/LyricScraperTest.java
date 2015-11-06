package com.dshaw.lyriccount;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LyricScraperTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public LyricScraperTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( LyricScraperTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testLyricScraper()
    {
        assertTrue( true );
    }
}
