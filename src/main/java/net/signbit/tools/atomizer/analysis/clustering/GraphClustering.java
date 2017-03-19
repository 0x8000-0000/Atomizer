package net.signbit.tools.atomizer.analysis.clustering;

import org.jgrapht.DirectedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

public class GraphClustering<V, E>
{
   private final DirectedGraph<V, E> support;

   private ArrayList<Cluster> clusters;

   private HashMap<V, Cluster> includedIn;

   public GraphClustering(DirectedGraph<V, E> support)
   {
      this.support = support;

      clusters = new ArrayList<>();
      includedIn = new HashMap<>();

      for (V vv: support.vertexSet())
      {
         Cluster cc = new Cluster(support);
         cc.addMember(vv);
         clusters.add(cc);
         includedIn.put(vv, cc);
      }
   }

   public GraphClustering(GraphClustering<V, E> other)
   {
      this.support = other.support;

      clusters = new ArrayList<>();
      includedIn = new HashMap<>();

      for (Cluster cc: other.clusters)
      {
         Cluster<V, E> clusterClone = new Cluster(cc);
         clusters.add(clusterClone);

         for (V vv: clusterClone.getMembers())
         {
            includedIn.put(vv, clusterClone);
         }
      }
   }

   public double evaluateCohesion()
   {
      double score = 0;

      for (Cluster cc: clusters)
      {
         score += cc.getSize() * cc.computeCohesion();
      }

      return score;
   }

   public static Object selectWeighted(Object[] element, double[] weight)
   {
      assert element.length == weight.length;
      TreeMap<Double, Object> map = new TreeMap<>();

      double totalWeight = 0;

      for (int ii = 0; ii < element.length; ii ++)
      {
         totalWeight += weight[ii];
         map.put(totalWeight, element[ii]);
      }

      double value = Math.random() * totalWeight;
      return map.ceilingEntry(value).getValue();
   }

   public void mutate()
   {
      // select two adjacent clusters with high attraction and join them

      // select one cluster with low cohesion and break it in two
   }

}
