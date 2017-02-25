package net.signbit.tools.atomizer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipFile;

public class Statistician
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      computePackages();

      for (PackageRef pr: PackageRef.getAllPackages().values())
      {
         System.out.println("Dependencies for " + pr.getName());
         for (Map.Entry<PackageRef, AtomicLong> e: pr.getDependencyCounts().entrySet())
         {
            System.out.println("   " + e.getKey() + " : " + e.getValue().get());
         }
         System.out.println();
      }
   }

   private static void computePackages()
   {
      HashSet<PackageRef> packages = new HashSet<>();

      for (ClassRef cr: allClasses.values())
      {
         PackageRef pr = cr.getPackage();

         for (ClassRef dep: cr.getDependencies())
         {
            pr.addDependency(dep.getPackage());
         }
      }
   }
}
