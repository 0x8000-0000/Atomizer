package net.signbit.tools.atomizer;

import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.StringComponentNameProvider;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.ext.ExportException;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipFile;

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
