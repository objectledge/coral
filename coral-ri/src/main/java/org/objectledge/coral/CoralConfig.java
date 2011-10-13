package org.objectledge.coral;

import java.util.EnumSet;
import java.util.Set;

import org.jcontainer.dna.Configuration;
import org.jcontainer.dna.ConfigurationException;

/**
 * Configuration of Coral system.
 * 
 * @author rafal.krzewski@objectledge.org
 */
public class CoralConfig
{
    /**
     * Enabled features.
     */
    private final Set<Feature> features;

    /**
     * Maximum number of idle sessions pooled per subject.
     * 
     * @see #DEFAULT_SESSION_POOL_SIZE_PER_SUBJECT
     */
    private final int sessionPoolSizePerUser;

    /**
     * Interval between idle object eviction runs, in seconds. A negative value disables eviction
     * completely.
     * 
     * @see #DEFAULT_SESSION_EVICTION_INTERVAL
     */
    private final int sessionEvictionInterval;

    /**
     * Minimum idle time after which session becomes eligible for eviction, in seconds.
     */
    private final int sessionEvictionThreashold;

    /**
     * Number of sessions checked for eviction eligibility per single eviction run.
     */
    private final int sessionTestsPerEvictionRun;

    /**
     * Default pool size per user.
     */
    private static final int DEFAULT_SESSION_POOL_SIZE_PER_SUBJECT = 8;

    /**
     * Default session eviction interval.
     */
    private static final int DEFAULT_SESSION_EVICTION_INTERVAL = 60;

    /**
     * Default session idle time threshold.
     */
    private static final int DEFAULT_SESSION_EVICTION_THRESHOLD = 300;

    /**
     * Default Number of tests per eviction run.
     */
    private static final int DEFAULT_SESSION_TESTS_PER_EVICTION_RUN = 1000;

    /**
     * Creates a default Coral configuration.
     */
    public CoralConfig()
    {
        features = EnumSet.noneOf(Feature.class);
        sessionPoolSizePerUser = DEFAULT_SESSION_POOL_SIZE_PER_SUBJECT;
        sessionEvictionInterval = DEFAULT_SESSION_EVICTION_INTERVAL;
        sessionEvictionThreashold = DEFAULT_SESSION_EVICTION_THRESHOLD;
        sessionTestsPerEvictionRun = DEFAULT_SESSION_TESTS_PER_EVICTION_RUN;
    }

    /**
     * Crate s a Coral configuration object.
     * 
     * @param features Enabled features.
     * @param sessionPoolSizePerUser Interval between idle object eviction runs, in seconds. A
     *        negative value disables eviction completely.
     * @param sessionEvictionInterval Minimum idle time after which session becomes eligible for
     *        eviction, in seconds.
     * @param sessionEvictionThreashold Minimum idle time after which session becomes eligible for
     *        eviction, in seconds.
     * @param sessionTestsPerEvictionRun Number of sessions checked for eviction eligibility per
     *        single eviction run.
     */
    public CoralConfig(Set<Feature> features, int sessionPoolSizePerUser,
        int sessionEvictionInterval, int sessionEvictionThreashold, int sessionTestsPerEvictionRun)
    {
        this.features = features;
        this.sessionPoolSizePerUser = sessionPoolSizePerUser;
        this.sessionEvictionInterval = sessionEvictionInterval;
        this.sessionEvictionThreashold = sessionEvictionThreashold;
        this.sessionTestsPerEvictionRun = sessionTestsPerEvictionRun;
    }

    /**
     * Creates Coral configuration from DNA configuration source.
     * 
     * @param config DNA Configuration object.
     * @throws ConfigurationException when configuration data is not valid.
     */
    public CoralConfig(Configuration config)
        throws ConfigurationException
    {
        Set<Feature> featureSet = EnumSet.noneOf(Feature.class);
        Configuration[] featuresConfigs = config.getChild("features").getChildren("feature");
        for(Configuration featureConfig : featuresConfigs)
        {
            String featureName = featureConfig.getValue();
            try
            {
                featureSet.add(Feature.valueOf(featureName));
            }
            catch(IllegalArgumentException e)
            {
                throw new ConfigurationException("unknown feature " + featureName,
                    featureConfig.getPath(), featureConfig.getLocation());
            }
        }
        features = featureSet;
        Configuration sessionPoolConfig = config.getChild("sessionPool");
        sessionPoolSizePerUser = sessionPoolConfig.getChild("sizePerSubject").getValueAsInteger(
            DEFAULT_SESSION_POOL_SIZE_PER_SUBJECT);
        sessionEvictionInterval = sessionPoolConfig.getChild("evictionInternal").getValueAsInteger(
            DEFAULT_SESSION_EVICTION_INTERVAL);
        sessionEvictionThreashold = sessionPoolConfig.getChild("evictionThreshold")
            .getValueAsInteger(DEFAULT_SESSION_EVICTION_THRESHOLD);
        sessionTestsPerEvictionRun = sessionPoolConfig.getChild("testPerEvictionRun")
            .getValueAsInteger(DEFAULT_SESSION_TESTS_PER_EVICTION_RUN);
    }

    /**
     * Returns enabled Coral features.
     * 
     * @return a {@link Feature} set.
     */
    public Set<Feature> getFeatures()
    {
        return features;
    }

    /**
     * Returns maximum number of idle sessions pooled per subject.
     * 
     * @return maximum number of idle sessions pooled per subject.
     */
    public int getSessionPoolSizePerUser()
    {
        return sessionPoolSizePerUser;
    }

    /**
     * Returns minimum idle time after which session becomes eligible for eviction, in seconds.
     * 
     * @return minimum idle time after which session becomes eligible for eviction, in seconds.
     */
    public int getSessionEvictionInterval()
    {
        return sessionEvictionInterval;
    }

    /**
     * Return minimum idle time after which session becomes eligible for eviction, in seconds.
     * 
     * @return return minimum idle time after which session becomes eligible for eviction, in
     *         seconds.
     */
    public int getSessionEvictionThreashold()
    {
        return sessionEvictionThreashold;
    }

    /**
     * Returns number of sessions checked for eviction eligibility per single eviction run.
     * 
     * @return number of sessions checked for eviction eligibility per single eviction run.
     */
    public int getSessionTestsPerEvictionRun()
    {
        return sessionTestsPerEvictionRun;
    }
}
