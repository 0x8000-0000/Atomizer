package net.signbit.tools.atomizer.analysis;

import net.signbit.tools.atomizer.ClassRef;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

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

      final long endTimeNano = System.nanoTime();

      logger.info("Class graph loaded in {}ms", (endTimeNano - startTimeNano) / 1000000);

      return classGraph;
   }
}
