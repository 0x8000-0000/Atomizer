package net.signbit.tools.atomizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class PackageRef
{
   private String name;

   private static HashMap<String, PackageRef> allPackages = new HashMap<>();

   private HashMap<PackageRef, AtomicLong> dependencies = new HashMap<>();

   private PackageRef(String name)
   {
      this.name = name;
   }

   @Override
   public boolean equals(Object o)
   {
      if (null != o)
      {
         if (o instanceof PackageRef)
         {
            PackageRef pr = (PackageRef) o;
            if (name.equals(pr.name))
            {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public int hashCode()
   {
      return name.hashCode();
   }

   void addDependency(PackageRef pr)
   {
      AtomicLong count = dependencies.get(pr);

      if (null == count)
      {
         dependencies.put(pr, new AtomicLong(1));
      }
      else
      {
         count.incrementAndGet();
      }
   }

   public static PackageRef getPackage(String packageName)
   {
      PackageRef pr = allPackages.get(packageName);
      if (null == pr)
      {
         pr = new PackageRef(packageName);
         allPackages.put(packageName, pr);
      }
      return pr;
   }

   public Map<PackageRef, AtomicLong> getDependencyCounts()
   {
      return dependencies;
   }

   public static Map<String, PackageRef> getAllPackages()
   {
      return allPackages;
   }

   public String getName()
   {
      return name;
   }

   @Override
   public String toString()
   {
      return name;
   }
}
