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

package net.signbit.tools.atomizer;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Atomizer
{
   private static HashMap<String, ClassRef> localClasses;

   private static DirectedGraph<ClassRef, DefaultEdge> classGraph;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      loadClasses(zipFile);

      StrongConnectivityAlgorithm<ClassRef, DefaultEdge> scAlg =
            new KosarajuStrongConnectivityInspector<>(classGraph);
      List<Set<ClassRef>> stronglyConnectedSubgraphs =
            scAlg.stronglyConnectedSets();

      System.out.println("Strongly connected components:");
      for (Set<ClassRef> scc: stronglyConnectedSubgraphs)
      {
         if (scc.size() > 1)
         {
            System.out.println("   " + scc.size() + " " + scc);
         }
      }
      System.out.println();

      CycleDetector<ClassRef, DefaultEdge> cycleDetector = new CycleDetector<>(classGraph);
      Set<ClassRef> cycles = cycleDetector.findCycles();

      System.out.println("Classes that participate in dependency cycles:");
      for (ClassRef cr: cycles)
      {
         System.out.println("   " + cr);
      }
      System.out.println();

      displayAllClasses();
   }

   private static void displayAllClasses()
   {
      System.out.println("All classes:");
      for (ClassRef cr : localClasses.values())
      {
         System.out.println(cr.getClassName());
         for (String name : cr.getDependencies())
         {
            if (localClasses.containsKey(name))
            {
               System.out.println("   . " + name);
            }
            else
            {
               System.out.println("     " + name);
            }
         }
         System.out.println();
      }
   }

   private static void loadClasses(ZipFile zipFile) throws IOException
   {
      localClasses = new HashMap<String, ClassRef>();

      Enumeration<? extends ZipEntry> en = zipFile.entries();
      while (en.hasMoreElements())
      {
         ZipEntry e = en.nextElement();
         String entryName = e.getName();
         if (entryName.endsWith(".class"))
         {
            InputStream stream = zipFile.getInputStream(e);
            ClassRef cr = new ClassRef(stream);

            localClasses.put(cr.getClassName(), cr);
         }
      }

      classGraph = new DefaultDirectedGraph<ClassRef, DefaultEdge>(DefaultEdge.class);

      for (ClassRef cr : localClasses.values())
      {
         classGraph.addVertex(cr);
      }

      for (ClassRef cr : localClasses.values())
      {
         for (String dependentClassName : cr.getDependencies())
         {
            ClassRef dependentClass = localClasses.get(dependentClassName);
            if (null != dependentClass)
            {
               classGraph.addEdge(cr, dependentClass);
            }
         }
      }
   }

}
