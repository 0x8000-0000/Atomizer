package net.signbit.tools.atomizer.export;

import net.signbit.tools.atomizer.ClassRef;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.ExportException;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

public class VisualizeClasses
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException, ExportException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      DirectedGraph<ClassRef, DefaultEdge> classGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

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

      GraphMLExporter<ClassRef, DefaultEdge> exporter = new GraphMLExporter<>();
      exporter.setVertexLabelProvider(new StringComponentNameProvider<>());
      FileWriter writer = new FileWriter(args[1]);

      exporter.exportGraph(classGraph, writer);
   }
}
