package com.panda;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author panda
 */
@Mojo(name = "panda",defaultPhase = LifecyclePhase.PACKAGE)
public class PandaPlugin extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("hello panda");
    }
}
