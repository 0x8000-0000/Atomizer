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
