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

package net.signbit.tools.atomizer.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.tools.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DivideAndCompile
{
   private static final Logger logger = LoggerFactory.getLogger(DivideAndCompile.class);

   public static void main(String[] args)
   {
      try
      {
         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
         StandardJavaFileManager fileManager =
               compiler.getStandardFileManager(null, null, null);

         fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
               Arrays.asList(File.createTempFile("/tmp", "")));

         File[] inputFiles = new File[0];
         Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(inputFiles));

         DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

         compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();

         for (Diagnostic diagnostic : diagnostics.getDiagnostics())
         {
            System.out.format("Error on line %d in %s%n",
                  diagnostic.getLineNumber(),
                  diagnostic.getSource());
         }

         fileManager.close();
      }
      catch (IOException ioe)
      {
         logger.error("I/O Exception", ioe);
      }
   }
}
