package net.signbit.tools.atomizer.analysis.clustering;

import org.jgrapht.DirectedGraph;

import java.util.HashSet;
import java.util.Set;

public class Cluster<V, E>
{
   public static final double ISOLATED_CLUSTER = 2.0;

   private DirectedGraph<V, E> support;

   private HashSet<V> members;

   private double cohesion;
   private double coupling;
   private boolean cohesionIsValid = false;

   public Cluster(DirectedGraph<V, E> support)
   {
      this.support = support;

      members = new HashSet<>();
   }

   public Cluster(Cluster<V, E> other)
   {
      this.support = other.support;

      members = new HashSet<>(other.members);
   }

   public void addMember(V vv)
   {
      cohesionIsValid = false;
      members.add(vv);
   }

   public void removeMember(V vv)
   {
      cohesionIsValid = false;
      members.remove(vv);
   }

   public double computeCohesion()
   {
      updateCohesionCoupling();
      return cohesion;
   }

   public double computeCoupling()
   {
      updateCohesionCoupling();
      return cohesion;
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

         for (V vv : members)
         {
            for (E ee : support.incomingEdgesOf(vv))
            {
               V other = support.getEdgeSource(ee);
               if (members.contains(other))
               {
                  incomingIn++;
               }
               else
               {
                  incomingOut++;
               }
            }

            for (E ee : support.outgoingEdgesOf(vv))
            {
               V other = support.getEdgeTarget(ee);
               if (members.contains(other))
               {
                  outgoingIn++;
               }
               else
               {
                  outgoingOut++;
               }
            }
         }

         assert (outgoingIn == incomingIn);

         final double size = members.size();
         cohesion = (outgoingIn + incomingIn) / (size * size);

         coupling = (incomingOut + outgoingOut) / size;
      }
   }

   public int getSize() { return members.size(); }

   private int countEdgesFrom(Cluster<V, E> other)
   {
      int edgeCount = 0;

      for (V vv: members)
      {
         for (E ee : support.incomingEdgesOf(vv))
         {
            V uu = support.getEdgeSource(ee);
            if (other.members.contains(uu))
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

      for (V vv: members)
      {
         for (E ee : support.outgoingEdgesOf(vv))
         {
            V uu = support.getEdgeTarget(ee);
            if (other.members.contains(uu))
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
      if (0 != otherToThis)
      {
         return true;
      }

      return false;
   }

   public double computeAttraction(Cluster<V, E> other)
   {
      int thisToOther = countEdgesTo(other);
      int otherToThis = countEdgesFrom(other);

      return (thisToOther + otherToThis) / ((double) (members.size() + other.members.size()));
   }

   public Set<V> getMembers()
   {
      return members;
   }

   /**
    *
    * @return the vertex V such as V has more adjacent vertices outside cluster
    */
   V findLeastAttachedNode()
   {
      return null;
   }
}
