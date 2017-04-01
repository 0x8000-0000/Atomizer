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

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.ExportException;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultEdge;

import net.signbit.tools.atomizer.ClassRef;
import net.signbit.tools.atomizer.analysis.GraphLoader;

public class VisualizeClasses
{
   public static void main(String[] args) throws IOException, ExportException
   {
      DirectedGraph<ClassRef, DefaultEdge> classGraph = GraphLoader.loadClassDependencies(args[0]);

      GraphMLExporter<ClassRef, DefaultEdge> exporter = new GraphMLExporter<>();
      exporter.setVertexLabelProvider(new StringComponentNameProvider<>());
      FileWriter writer = new FileWriter(args[1]);

      exporter.exportGraph(classGraph, writer);
   }

}
