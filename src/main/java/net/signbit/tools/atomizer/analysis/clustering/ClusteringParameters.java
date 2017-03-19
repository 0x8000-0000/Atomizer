package net.signbit.tools.atomizer.analysis.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClusteringParameters
{
   private static final Logger logger = LoggerFactory.getLogger(ClusteringParameters.class);

   private static ClusteringParameters parameters = new ClusteringParameters();

   private Properties properties = new Properties();

   private ClusteringParameters()
   {
      try
      {
         final InputStream stream =
               this.getClass().getResourceAsStream("clustering.properties");
         properties.load(stream);
      }
      catch (IOException ioe)
      {

      }
   }

   public int getMaxClusterCount()
   {
      String value = properties.getProperty("MAX_CLUSTER_COUNT", "64");
      return Integer.valueOf(value).intValue();
   }

   public int getInitialClusterCount()
   {
      String value = properties.getProperty("INITIAL_CLUSTER_COUNT", "64");
      return Integer.valueOf(value).intValue();
   }
}
