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

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

import net.signbit.tools.atomizer.ClassRef;

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
