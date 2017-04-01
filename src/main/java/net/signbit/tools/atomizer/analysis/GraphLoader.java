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

package net.signbit.tools.atomizer.analysis;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.signbit.tools.atomizer.ClassRef;

public class GraphLoader
{
   private static final Logger logger = LoggerFactory.getLogger(GraphLoader.class);

   public static DirectedGraph<ClassRef, DefaultEdge> loadClassDependencies(String fileName) throws IOException
   {
      final long startTimeNano = System.nanoTime();

      ZipFile zipFile = new ZipFile(fileName);

      Map<String, ClassRef> allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      DirectedGraph<ClassRef, DefaultEdge> classGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

      for (ClassRef cr : allClasses.values())
      {
         classGraph.addVertex(cr);
      }

      for (ClassRef cr : allClasses.values())
      {
         for (ClassRef dep : cr.getDependencies())
         {
            classGraph.addEdge(cr, dep);
         }
      }

      final long endTimeNano = System.nanoTime();

      logger.info("Class graph loaded in {}ms", (endTimeNano - startTimeNano) / 1000000);

      return classGraph;
   }
}
