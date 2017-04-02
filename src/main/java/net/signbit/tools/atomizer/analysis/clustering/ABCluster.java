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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.math3.random.RandomDataGenerator;

import net.signbit.tools.atomizer.ClassRef;
import net.signbit.tools.atomizer.PackageRef;

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

   private void mutateInPlace(ClassRef selectedElement)
   {
      if (A.contains(selectedElement))
      {
         /*
          * moving element from A to B
          */
         for (ClassRef dep: selectedElement.getDependencies())
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

         for (ClassRef dep: selectedElement.getReverseDependencies())
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

         A.remove(selectedElement);
         B.add(selectedElement);
      }
      else
      {
         /*
          * moving element from B to A
          */
         for (ClassRef dep: selectedElement.getDependencies())
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

         for (ClassRef dep: selectedElement.getReverseDependencies())
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

         B.remove(selectedElement);
         A.add(selectedElement);
      }

      /*
       * enable for test
       *
      int fast_A_to_B = A_to_B;
      int fast_B_to_A = B_to_A;

      computeScores();

      assert fast_A_to_B == A_to_B;
      assert fast_B_to_A == B_to_A;
      */
   }

   public ABCluster mutateByPackage(ClassRef classRef)
   {
      ABCluster newCluster = new ABCluster(this);

      PackageRef pr = classRef.getPackage();

      int mostlyInA = 0;
      int mostlyInB = 0;

      for (ClassRef cr: pr.getClasses())
      {
         if (A.contains(cr))
         {
            mostlyInA ++;
         }
         else
         {
            mostlyInB ++;
         }
      }

      if (mostlyInA > mostlyInB)
      {
         // move all elements from A to B
         for (ClassRef cr: pr.getClasses())
         {
            if (A.contains(cr))
            {
               newCluster.mutateInPlace(cr);
            }
         }
      }
      else
      {
         // move all elements from B to A
         for (ClassRef cr: pr.getClasses())
         {
            if (B.contains(cr))
            {
               newCluster.mutateInPlace(cr);
            }
         }
      }

      return newCluster;
   }

   @Override
   public int compareTo(ABCluster other)
   {
      return getScore() - other.getScore();
   }

   @Override
   public int hashCode()
   {
      return (A.hashCode() ^ B.hashCode());
   }

   @Override
   public boolean equals(Object o)
   {
      if (o instanceof ABCluster)
      {
         ABCluster other = (ABCluster) o;

         return A.equals(other.A) || A.equals(other.B);
      }
      else
      {
         return false;
      }
   }

   public void writeTo(FileWriter writer) throws IOException
   {
      HashSet<PackageRef> packagesInA = new HashSet<>();
      ArrayList<String> namesInA = new ArrayList<>(A.size());
      for (ClassRef cr: A)
      {
         namesInA.add(cr.getClassName());
         packagesInA.add(cr.getPackage());
      }
      Collections.sort(namesInA);

      HashSet<PackageRef> packagesInB = new HashSet<>();
      ArrayList<String> namesInB = new ArrayList<>(B.size());
      for (ClassRef cr: B)
      {
         namesInB.add(cr.getClassName());
         packagesInB.add(cr.getPackage());
      }
      Collections.sort(namesInB);

      StringBuilder headerA = new StringBuilder();
      headerA.append("Cluster A: ");
      headerA.append(packagesInA.size());
      headerA.append(" packages and ");
      headerA.append(namesInA.size());
      headerA.append(" classes");
      headerA.append('\n');
      writer.append(headerA.toString());

      for (String name: namesInA)
      {
         writer.append(name);
         writer.append('\n');
      }

      StringBuilder headerB = new StringBuilder();
      headerB.append("Cluster B: ");
      headerB.append(packagesInB.size());
      headerB.append(" packages and ");
      headerB.append(namesInB.size());
      headerB.append(" classes");
      headerB.append('\n');
      writer.append(headerB.toString());

      for (String name: namesInB)
      {
         writer.append(name);
         writer.append('\n');
      }

      StringBuilder dependencies = new StringBuilder();
      dependencies.append("\n\n-----\n");
      if (A_to_B < B_to_A)
      {
         dependencies.append(A_to_B);
         dependencies.append(" from A to B (versus ");
         dependencies.append(B_to_A);
         dependencies.append(")\n");
         writer.append(dependencies.toString());
         for (ClassRef cr: A)
         {
            for (ClassRef dep : cr.getDependencies())
            {
               if (B.contains(dep))
               {
                  writer.append(cr.getClassName());
                  writer.append(" -> ");
                  writer.append(dep.getClassName());
                  dependencies.append('\n');
               }
            }
         }
      }
      else
      {
         dependencies.append(B_to_A);
         dependencies.append(" from B to A (versus ");
         dependencies.append(A_to_B);
         dependencies.append(")\n");
         writer.append(dependencies.toString());
         for (ClassRef cr: B)
         {
            for (ClassRef dep : cr.getDependencies())
            {
               if (A.contains(dep))
               {
                  writer.append(cr.getClassName());
                  writer.append(" -> ");
                  writer.append(dep.getClassName());
                  dependencies.append('\n');
               }
            }
         }
      }
   }

}
