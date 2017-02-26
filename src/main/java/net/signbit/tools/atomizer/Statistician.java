package net.signbit.tools.atomizer;

import java.io.IOException;
import java.util.*;
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

      PackageRef.computePackageDependencies(allClasses.values());

      for (PackageRef pr: PackageRef.getAllPackages().values())
      {
         Map<PackageRef, AtomicLong> counts = pr.getDependencyCounts();
         ArrayList<PackageRef> depPkg = new ArrayList<>(counts.keySet());
         Collections.sort(depPkg);

         System.out.println("Dependencies for " + pr.getName());
         for (PackageRef dep: depPkg)
         {
            System.out.println("   " + dep + " : " + counts.get(dep).get());
         }
         System.out.println();
      }
   }
}
