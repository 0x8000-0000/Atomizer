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

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.math3.random.RandomDataGenerator;

import net.signbit.tools.atomizer.ClassRef;

// TODO: Convert to template form
public class ABCluster implements Comparable<ABCluster>
{
   private HashSet<ClassRef> A;
   private HashSet<ClassRef> B;

   private int A_to_B;
   private int B_to_A;

   private int A_to_A;
   private int B_to_B;

   public ABCluster(ArrayList<ClassRef> classSet, RandomDataGenerator rdg, double bias)
   {
      A = new HashSet<>();
      B = new HashSet<>();

      for (ClassRef cr : classSet)
      {
         double selector = rdg.nextUniform(0, 1, true);
         if (selector < bias)
         {
            A.add(cr);
         }
         else
         {
            B.add(cr);
         }
      }

      computeScores();
   }

   private ABCluster(final ABCluster other)
   {
      A = new HashSet<>(other.A);
      B = new HashSet<>(other.B);

      A_to_B = other.A_to_B;
      B_to_A = other.B_to_A;

      A_to_A = other.A_to_A;
      B_to_B = other.B_to_B;
   }

   private void computeScores()
   {
      A_to_B = 0;
      B_to_A = 0;

      A_to_A = 0;
      B_to_B = 0;

      for (ClassRef cr: A)
      {
         for (ClassRef dep: cr.getDependencies())
         {
            if (B.contains(dep))
            {
               A_to_B ++;
            }
            else
            {
               A_to_A ++;
            }
         }
      }

      for (ClassRef cr: B)
      {
         for (ClassRef dep: cr.getDependencies())
         {
            if (A.contains(dep))
            {
               B_to_A ++;
            }
            else
            {
               B_to_B ++;
            }
         }
      }
   }

   public int getScore()
   {
      if (A_to_B > B_to_A)
      {
         return B_to_A;
      }
      else
      {
         return A_to_B;
      }
   }

   public ABCluster mutate(ClassRef randomElement)
   {
      ABCluster newCluster = new ABCluster(this);
      newCluster.mutateInPlace(randomElement);
      return newCluster;
   }

   private void mutateInPlace(ClassRef randomElement)
   {
      if (A.contains(randomElement))
      {
         /*
          * moving element from A to B
          */
         for (ClassRef dep: randomElement.getDependencies())
         {
            if (B.contains(dep))
            {
               // A -> B   becomes   B -> B
               B_to_B ++;
               A_to_B --;
            }
            else
            {
               // A -> A   becomes   B -> A
               A_to_A --;
               B_to_A ++;
            }
         }

         for (ClassRef dep: randomElement.getReverseDependencies())
         {
            if (B.contains(dep))
            {
               // B -> A   becomes   B -> B
               B_to_B ++;
               B_to_A --;
            }
            else
            {
               // A -> A   becomes   A -> B
               A_to_A --;
               A_to_B ++;
            }
         }

         A.remove(randomElement);
         B.add(randomElement);
      }
      else
      {
         /*
          * moving element from B to A
          */
         for (ClassRef dep: randomElement.getDependencies())
         {
            if (B.contains(dep))
            {
               // B -> B   becomes   A -> B
               B_to_B --;
               A_to_B ++;
            }
            else
            {
               // B -> A   becomes   A -> A
               A_to_A ++;
               B_to_A --;
            }
         }

         for (ClassRef dep: randomElement.getReverseDependencies())
         {
            if (B.contains(dep))
            {
               // B -> B   becomes   B -> A
               B_to_B --;
               B_to_A ++;
            }
            else
            {
               // A -> B   becomes   A -> A
               A_to_A ++;
               A_to_B --;
            }
         }

         B.remove(randomElement);
         A.add(randomElement);
      }

      int fast_A_to_B = A_to_B;
      int fast_B_to_A = B_to_A;

      /*
       * enable for test
       *
      computeScores();

      assert fast_A_to_B == A_to_B;
      assert fast_B_to_A == B_to_A;
      */
   }

   @Override
   public int compareTo(ABCluster other)
   {
      return getScore() - other.getScore();
   }
}
