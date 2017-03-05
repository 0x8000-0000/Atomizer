package net.signbit.tools.atomizer.export;

import net.signbit.tools.atomizer.ClassRef;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

/** Export in Tulip format
 *
 * http://tulip.labri.fr/TulipDrupal/?q=tlp-file-format
 */
public class Tulip
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      System.out.println("(tlp \"2.0\"");

      int vertexId = 1;

      System.out.print("(nodes ");

      for (ClassRef cr : allClasses.values())
      {
         cr.setVertexId(vertexId);

         System.out.print(vertexId);
         System.out.print(' ');

         vertexId ++;
      }

      System.out.println(")");

      int edgeId = 1;

      for (ClassRef cr : allClasses.values())
      {
         for (ClassRef or : cr.getDependencies())
         {
            StringBuilder sb = new StringBuilder();

            sb.append("(edge ");
            sb.append(edgeId ++);
            sb.append(' ');
            sb.append(cr.getVertexId());
            sb.append(' ');
            sb.append(or.getVertexId());
            sb.append(')');

            System.out.println(sb.toString());
         }
      }

      System.out.println("(property  0 string \"viewLabel\"");
      System.out.println("   (default \"\" \"\")");

      for (ClassRef cr : allClasses.values())
      {
         StringBuilder sb = new StringBuilder();

         sb.append("   (node ");
         sb.append(cr.getVertexId());
         sb.append(' ');
         sb.append('\"');
         sb.append(cr.getClassName());
         sb.append('\"');
         sb.append(')');
         System.out.println(sb.toString());
      }

      // end of node label property
      System.out.println(")");

      // end of file
      System.out.println(")");
   }
}
