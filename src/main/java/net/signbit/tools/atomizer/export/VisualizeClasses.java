package net.signbit.tools.atomizer.export;

import net.signbit.tools.atomizer.ClassRef;
import net.signbit.tools.atomizer.analysis.GraphLoader;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.ExportException;
import org.jgrapht.ext.GraphMLExporter;

import java.io.FileWriter;
import java.io.IOException;

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
