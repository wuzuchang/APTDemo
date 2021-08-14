package com.wzc.findview.gradle.plugin.launch;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.LibraryPlugin;
import com.wzc.findview.gradle.plugin.launch.core.FindViewTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class FindViewPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class);
        boolean isLibrary = project.getPlugins().hasPlugin(LibraryPlugin.class);
        if (isApp) {
            AppExtension extension = project.getExtensions().findByType(AppExtension.class);
            extension.registerTransform(new FindViewTransform());
        } else if (isLibrary) {
            // LibraryExtension extension = project.getExtensions().findByType(LibraryExtension.class);
            // extension.registerTransform(new LibraryTransform());
        }
    }
}