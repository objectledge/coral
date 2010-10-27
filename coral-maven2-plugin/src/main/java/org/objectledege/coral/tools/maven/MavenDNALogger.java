package org.objectledege.coral.tools.maven;

import org.apache.maven.plugin.logging.Log;
import org.jcontainer.dna.Logger;

public class MavenDNALogger
    implements Logger
{
    final private Log delegate;

    public MavenDNALogger(Log delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isErrorEnabled()
    {       
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(String message)
    {
        delegate.error(message);
    }

    @Override
    public void error(String message, Throwable cause)
    {
        delegate.error(message, cause);
    }

    @Override
    public boolean isWarnEnabled()
    {       
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(String message)
    {
        delegate.warn(message);
    }

    @Override
    public void warn(String message, Throwable cause)
    {
        delegate.warn(message, cause);
    }

    @Override
    public boolean isInfoEnabled()
    {       
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(String message)
    {
        delegate.info(message);
    }

    @Override
    public void info(String message, Throwable cause)
    {
        delegate.info(message, cause);
    }  
    
    @Override
    public boolean isDebugEnabled()
    {       
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String message)
    {
        delegate.debug(message);
    }

    @Override
    public void debug(String message, Throwable cause)
    {
        delegate.debug(message, cause);
    }    

    @Override
    public boolean isTraceEnabled()
    {       
        return delegate.isDebugEnabled();
    }

    @Override
    public void trace(String message)
    {
        delegate.debug(message);
    }

    @Override
    public void trace(String message, Throwable cause)
    {
        delegate.debug(message, cause);
    }
    
    @Override
    public Logger getChildLogger(String name)
    {
        return this;
    }
}
