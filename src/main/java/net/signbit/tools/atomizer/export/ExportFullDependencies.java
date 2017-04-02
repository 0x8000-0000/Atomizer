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

package net.signbit.tools.atomizer.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import static java.util.stream.Collectors.toList;

import net.signbit.tools.atomizer.ClassRef;
import net.signbit.tools.atomizer.PackageRef;

public class ExportFullDependencies
{
   private static Map<String, ClassRef> allClasses;

   public static void main(String[] args) throws IOException
   {
      ZipFile zipFile = new ZipFile(args[0]);

      allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      if (args.length > 2)
      {
         boolean filterInnerClasses = Boolean.valueOf(args[2]);

         if (filterInnerClasses)
         {
            allClasses = ClassRef.filterInnerClasses(allClasses);
            PackageRef.filterInnerClasses();
         }
      }

      FileWriter writer = new FileWriter(args[1]);

      /*
       * List classes
       */
      ArrayList<String> classNames = new ArrayList<>(allClasses.keySet());
      Collections.sort(classNames);
      for (String name: classNames)
      {
         writer.append(name);
         writer.append('\n');

         List<String> dependentNames = allClasses.get(name)
               .getDependencies()
               .stream()
               .map(ClassRef::getClassName)
               .sorted()
               .collect(toList());

         for (String depName: dependentNames)
         {
            writer.append("   ");
            writer.append(depName);
            writer.append('\n');
         }
      }

      writer.close();
   }
}
