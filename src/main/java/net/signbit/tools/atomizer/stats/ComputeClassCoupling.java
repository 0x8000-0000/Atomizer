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

      System.out.println("Class,\"-> Same\",\"-> Other\",\"Same ->\",\"Other ->\"");

      for (ClassRef cr: allClasses.values())
      {
         StringBuilder sb = new StringBuilder();
         sb.append(cr.getClassName());
         final String thisPackage = cr.getPackageName();
         int dependsSamePackage = 0;
         int dependsOtherPackage = 0;
         for (ClassRef or: cr.getDependencies())
         {
            if (thisPackage.equals(or.getPackageName()))
            {
               dependsSamePackage ++;
            }
            else
            {
               dependsOtherPackage ++;
            }
         }
         sb.append(',');
         sb.append(dependsSamePackage);
         sb.append(',');
         sb.append(dependsOtherPackage);
         sb.append(',');
         sb.append(cr.getDependedOnBySamePackageCount());
         sb.append(',');
         sb.append(cr.getDependedOnByOutsidePackageCount());
         System.out.println(sb.toString());
      }
   }
}
