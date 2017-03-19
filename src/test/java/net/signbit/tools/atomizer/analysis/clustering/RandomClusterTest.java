package net.signbit.tools.atomizer.analysis.clustering;

import static org.junit.Assert.assertEquals;

import org.jgrapht.ext.EdgeProvider;
import org.jgrapht.ext.GraphMLImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.ext.VertexProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

public class RandomClusterTest
{
   private DefaultDirectedGraph<String, DefaultEdge> supportGraph;
   private Cluster<String, DefaultEdge> cluster;

   private static class CustomVertexProvider implements VertexProvider<String>
   {
      public String buildVertex(String id, Map<String, String> attributes)
      {
         return new String(id);
      }
   }

   private static class CustomEdgeProvider implements EdgeProvider<String, DefaultEdge>
   {
      public DefaultEdge buildEdge(String from, String to, String label, Map<String, String> attributes)
      {
         return new DefaultEdge();
      }
   }

   @Before
   public void setupGraph()
   {
      final InputStream stream =
               this.getClass().getResourceAsStream("/net/signbit/tools/atomizer/analysis/clustering/test1.graphml");

      supportGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

      VertexProvider<String> vertexProvider = new CustomVertexProvider();
      EdgeProvider<String, DefaultEdge> edgeProvider = new CustomEdgeProvider();

      GraphMLImporter<String, DefaultEdge> importer = new GraphMLImporter<>(vertexProvider, edgeProvider);

      try
      {
         importer.importGraph(supportGraph, stream);
      }
      catch (ImportException e)
      {
         e.printStackTrace();
      }

      cluster = new Cluster<>(supportGraph);

      cluster.addMember("0");
      cluster.addMember("75");
      cluster.addMember("88");
   }

   @Test
   public void computeCohesion()
   {
      double cohesion = cluster.computeCohesion();
      assertEquals(0.444, cohesion, 0.001);
   }

   @Test
   public void computeCoupling()
   {
      double coupling = cluster.computeCoupling();
      assertEquals(4.333, coupling, 0.001);
   }
}
