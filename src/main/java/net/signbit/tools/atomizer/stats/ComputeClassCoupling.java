package net.signbit.tools.atomizer.stats;

import net.signbit.tools.atomizer.ClassRef;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

public class ComputeClassCoupling
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      for (ClassRef cr: allClasses.values())
      {
         StringBuilder sb = new StringBuilder();
         sb.append(cr.getClassName());
         sb.append(',');
         sb.append(cr.getDependencies().size());
         sb.append(',');
         sb.append(cr.getDependedOnByCount());
         System.out.println(sb.toString());
      }
   }
}
