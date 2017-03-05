package net.signbit.tools.atomizer.export;

import net.signbit.tools.atomizer.ClassRef;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

/** Exports the dependency graph in ABC format (input for MCL)
 *
 * http://micans.org/mcl/
 */
public class ExportABC
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      for (ClassRef cr : allClasses.values())
      {
         for (ClassRef or : cr.getDependencies())
         {
            StringBuilder sb = new StringBuilder();

            sb.append(cr.getClassName());
            sb.append(' ');
            sb.append(or.getClassName());
            sb.append(" 1");

            System.out.println(sb.toString());
         }
      }
   }
}
