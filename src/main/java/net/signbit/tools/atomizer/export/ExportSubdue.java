package net.signbit.tools.atomizer.export;

import net.signbit.tools.atomizer.ClassRef;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

/** Export class dependencies in SUBDUE format.
 *
 * http://ailab.wsu.edu/subdue/
 */
public class ExportSubdue
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      int vertexId = 1;

      for (ClassRef cr : allClasses.values())
      {
         StringBuilder sb = new StringBuilder();

         cr.setVertexId(vertexId);

         sb.append("v ");
         sb.append(vertexId);
         sb.append(' ');
         sb.append(cr.getClassName());

         System.out.println(sb.toString());

         vertexId ++;
      }

      int edgeId = 1;

      for (ClassRef cr : allClasses.values())
      {
         for (ClassRef or : cr.getDependencies())
         {
            StringBuilder sb = new StringBuilder();

            sb.append("d ");
            sb.append(cr.getVertexId());
            sb.append(' ');
            sb.append(or.getVertexId());
            sb.append(' ');
            sb.append(edgeId ++);

            System.out.println(sb.toString());
         }
      }
   }
}
