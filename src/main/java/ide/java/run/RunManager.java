package ide.java.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RunManager {

    public String runJavaFile(File file){
        StringBuilder output = new StringBuilder();

        try {
            File dir = file.getParentFile();

            ProcessBuilder compile = new ProcessBuilder(
                    "javac",
                    file.getName()
            );
            compile.directory(dir);

            Process compileProcess = compile.start();

            BufferedReader compileError = new BufferedReader(
                    new InputStreamReader(compileProcess.getErrorStream())
            );

            String line;
            while ((line = compileError.readLine()) != null){
                output.append(line).append("\n");
            }

            compileProcess.waitFor();

            String className = file.getName().replace(".java", "");

            ProcessBuilder run = new ProcessBuilder(
                    "java",
                    className
            );
            run.directory(dir);

            Process runProcess = run.start();

            BufferedReader runOutput = new BufferedReader(
                    new InputStreamReader(runProcess.getInputStream())
            );

            while ((line = runOutput.readLine()) != null){
                output.append(line).append("\n");
            }

            runProcess.waitFor();

        }catch (Exception e){
            output.append("Error: ").append(e.getMessage());
        }
        return output.toString();
    }
}
