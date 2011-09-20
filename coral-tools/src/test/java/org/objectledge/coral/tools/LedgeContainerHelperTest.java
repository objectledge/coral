package org.objectledge.coral.tools;

import junit.framework.TestCase;

import org.picocontainer.MutablePicoContainer;

public class LedgeContainerHelperTest
    extends TestCase
{
    public void testSimpleContainer()
        throws Exception
    {
        MutablePicoContainer ledgeContainer = LedgeContainerFactory.newLedgeContainer(
            "src/test/resources/container", "simple");
        assertNotNull(ledgeContainer);
    }
}
