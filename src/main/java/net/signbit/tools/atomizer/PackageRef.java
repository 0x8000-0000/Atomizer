/*
 * Copyright 2017 Florin Iucha <florin@signbit.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.signbit.tools.atomizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class PackageRef implements Comparable<PackageRef>
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

   public int compareTo(PackageRef pr)
   {
      if (null != pr)
      {
         return name.compareTo(pr.name);
      }
      else
      {
         return 0;
      }
   }

   public static void computePackageDependencies(Collection<ClassRef> allClasses)
   {
      for (ClassRef cr: allClasses)
      {
         PackageRef pr = cr.getPackage();

         for (ClassRef dep: cr.getDependencies())
         {
            pr.addDependency(dep.getPackage());
         }
      }
   }
}
