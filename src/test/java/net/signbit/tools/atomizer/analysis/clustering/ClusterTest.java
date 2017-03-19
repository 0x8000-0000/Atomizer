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