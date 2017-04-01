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

import static org.junit.Assert.assertEquals;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Before;
import org.junit.Test;

public class ClusterTest
{
   private DefaultDirectedGraph<String, DefaultEdge> supportGraph;
   private Cluster<String, DefaultEdge> cluster;

   @Before
   public void setupGraph()
   {
      supportGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

      supportGraph.addVertex("alpha");
      supportGraph.addVertex("beta");
      supportGraph.addVertex("gamma");

      supportGraph.addEdge("alpha", "beta");
      supportGraph.addEdge("beta", "gamma");
      supportGraph.addEdge("gamma", "alpha");

      cluster = new Cluster<>(supportGraph);
      cluster.addMember("alpha");
      cluster.addMember("beta");
   }

   @Test
   public void computeCohesion()
   {
      double cohesion = cluster.computeCohesion();
      assertEquals(0.5, cohesion, 0.00001);
   }

   @Test
   public void computeCoupling()
   {
      double coupling = cluster.computeCoupling();
      assertEquals(1.0, coupling, 0.00001);
   }

}