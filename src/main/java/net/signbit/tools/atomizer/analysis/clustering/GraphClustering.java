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

      generateInitialClustering();
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

   public void generateInitialClustering()
   {
      // shuffle vertices

      // select N randomly between 1 and number of vertices / 4

      // for the first N vertices
      //    if vertex is not assigned to cluster
      //       create new cluster and assign vertex to it

      // for the rest of vertices
      //    assign vertex to the largest adjacent cluster
   }

   public void mutate()
   {
      // select two adjacent clusters with high attraction and join them

      // select one cluster with low cohesion and break it in two

      // take P clusters with lowest cohesion and disband them

      // take Q clusters that have high coupling with other clusters and disband them
   }

}
