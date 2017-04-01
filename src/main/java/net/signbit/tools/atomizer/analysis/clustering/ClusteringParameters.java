/*
 * Copyright 2017 Florin Iucha <florin@signbit.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
