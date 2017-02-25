package net.signbit.tools.atomizer;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.ComponentNameProvider;
import org.jgrapht.ext.ExportException;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

public class Analyzer
{

   private static class ClassRefName implements ComponentNameProvider<ClassRef>
   {
      @Override
      public String getName(ClassRef classRef)
      {
         return classRef.getClassName();
      }
   }

   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException, ExportException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      DirectedGraph<ClassRef, DefaultEdge> classGraph = new DefaultDirectedGraph<ClassRef, DefaultEdge>(DefaultEdge.class);

      for (ClassRef cr: allClasses.values())
      {
         classGraph.addVertex(cr);
      }

      for (ClassRef cr: allClasses.values())
      {
         for (ClassRef dep: cr.getDependencies())
         {
            classGraph.addEdge(cr, dep);
         }
      }

      GraphMLExporter exporter = new GraphMLExporter();
      exporter.setVertexLabelProvider(new ClassRefName());
      FileWriter writer = new FileWriter(args[1]);

      exporter.exportGraph(classGraph, writer);
   }
}
