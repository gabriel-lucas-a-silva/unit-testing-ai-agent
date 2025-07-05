package br.com.unicat;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which says hello.
 */
@Mojo(name = "hello")
public class HelloMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("Olá, Mundo!");
    }
}
