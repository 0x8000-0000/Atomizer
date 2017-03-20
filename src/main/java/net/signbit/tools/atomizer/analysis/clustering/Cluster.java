package net.signbit.tools.atomizer.analysis.clustering;

import org.jgrapht.DirectedGraph;

import static java.util.Comparator.comparingInt;

import java.util.*;

public class Cluster<V, E>
{

   private DirectedGraph<V, E> support;

   private HashMap<V, Integer> members;

   private double cohesion;
   private double coupling;
   private boolean cohesionIsValid = false;

   Cluster(DirectedGraph<V, E> support)
   {
      this.support = support;

      members = new HashMap<>();
   }

   Cluster(Cluster<V, E> other)
   {
      this.support = other.support;

      members = new HashMap<>(other.members);
   }

   void addMember(V vv)
   {
      cohesionIsValid = false;
      members.put(vv, null);
   }

   public void removeMember(V vv)
   {
      cohesionIsValid = false;
      members.remove(vv);
   }

   double computeCohesion()
   {
      updateCohesionCoupling();
      return cohesion;
   }

   double computeCoupling()
   {
      updateCohesionCoupling();
      return coupling;
   }

   private void updateCohesionCoupling()
   {
      if (! cohesionIsValid)
      {
         // edge contained in cluster
         int outgoingIn = 0;
         // edge originating in this cluster and terminating outside
         int outgoingOut = 0;
         // edge contained in this cluster
         int incomingIn = 0;
         // edge originating outside this cluster and terminating inside
         int incomingOut = 0;

         for (Map.Entry<V, Integer> vv : members.entrySet())
         {
            int attachmentIn = 0;
            int attachmentOut = 0;

            for (E ee : support.incomingEdgesOf(vv.getKey()))
            {
               V other = support.getEdgeSource(ee);
               if (members.containsKey(other))
               {
                  incomingIn++;
                  attachmentIn++;
               }
               else
               {
                  incomingOut++;
                  attachmentOut++;
               }
            }

            for (E ee : support.outgoingEdgesOf(vv.getKey()))
            {
               V other = support.getEdgeTarget(ee);
               if (members.containsKey(other))
               {
                  outgoingIn++;
                  attachmentIn++;
               }
               else
               {
                  outgoingOut++;
                  attachmentOut++;
               }
            }

            vv.setValue(attachmentIn - attachmentOut);
         }

         assert (outgoingIn == incomingIn);

         final double size = members.size();
         cohesion = (outgoingIn + incomingIn) / (size * size);

         coupling = (incomingOut + outgoingOut) / size;
      }
   }

   int getSize() { return members.size(); }

   private int countEdgesFrom(Cluster<V, E> other)
   {
      int edgeCount = 0;

      for (V vv: members.keySet())
      {
         for (E ee : support.incomingEdgesOf(vv))
         {
            V uu = support.getEdgeSource(ee);
            if (other.members.containsKey(uu))
            {
               edgeCount++;
            }
         }
      }

      return edgeCount;
   }

   private int countEdgesTo(Cluster<V, E> other)
   {
      int edgeCount = 0;

      for (V vv: members.keySet())
      {
         for (E ee : support.outgoingEdgesOf(vv))
         {
            V uu = support.getEdgeTarget(ee);
            if (other.members.containsKey(uu))
            {
               edgeCount++;
            }
         }
      }

      return edgeCount;
   }

   public boolean isAdjacent(Cluster<V, E> other)
   {
      int thisToOther = countEdgesTo(other);
      if (0 != thisToOther)
      {
         return true;
      }

      int otherToThis = countEdgesFrom(other);
      return 0 != otherToThis;
   }

   public double computeAttraction(Cluster<V, E> other)
   {
      int thisToOther = countEdgesTo(other);
      int otherToThis = countEdgesFrom(other);

      return (thisToOther + otherToThis) / ((double) (members.size() + other.members.size()));
   }

   Set<V> getMembers()
   {
      return members.keySet();
   }

   /**
    *
    * @return the vertex V such as V has more adjacent vertices outside cluster
    */
   V findLeastAttachedNode()
   {
      Comparator<Map.Entry<V, Integer>> comparator = comparingInt(Map.Entry::getValue);
      return Collections.min(members.entrySet(), comparator).getKey();
   }
}
