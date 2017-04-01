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

package net.signbit.tools.atomizer.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipFile;

import org.jgrapht.ext.ExportException;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import net.signbit.tools.atomizer.ClassRef;
import net.signbit.tools.atomizer.PackageRef;

public class VisualizePackages
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException, ExportException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      PackageRef.computePackageDependencies(allClasses.values());

      DefaultDirectedWeightedGraph<PackageRef, DefaultWeightedEdge> packageGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

      for (PackageRef pr: PackageRef.getAllPackages().values())
      {
         packageGraph.addVertex(pr);
      }

      for (PackageRef pr: PackageRef.getAllPackages().values())
      {
         Map<PackageRef, AtomicLong> counts = pr.getDependencyCounts();

         for (Map.Entry<PackageRef, AtomicLong> entry: counts.entrySet())
         {
            DefaultWeightedEdge edge = packageGraph.addEdge(pr, entry.getKey());
            packageGraph.setEdgeWeight(edge, entry.getValue().get());
         }
      }

      GraphMLExporter<PackageRef, DefaultWeightedEdge>  exporter = new GraphMLExporter<>();
      exporter.setVertexLabelProvider(new StringComponentNameProvider<>());
      exporter.setExportEdgeWeights(true);
      FileWriter writer = new FileWriter(args[1]);

      exporter.exportGraph(packageGraph, writer);
   }
}
